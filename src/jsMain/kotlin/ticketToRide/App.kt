package ticketToRide

import com.ccfraser.muirwik.components.*
import kotlinext.js.jsObject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.css.*
import kotlinx.serialization.json.*
import org.w3c.dom.WebSocket
import org.w3c.notifications.Notification
import react.*
import styled.css
import ticketToRide.playerState.PlayerState
import ticketToRide.screens.*
import kotlin.browser.*

interface AppState : RState {
    var screen: Screen
    var locale: Locale
    var chatMessages: MutableList<Response.ChatMessage>
    var errorMessage: String
    var showErrorMessage: Boolean
    var secsToReconnect: Int
    var map: GameMap
}

private const val ErrorMessageTimeoutSecs = 4
private val json = Json(JsonConfiguration.Default.copy(allowStructuredMapKeys = true))

class App() : RComponent<RProps, AppState>() {
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private val requests = Channel<Request>(Channel.CONFLATED).also {
        scope.connectToServer(it)
        window.onunload = { _ -> it.offer(LeaveGameRequest) }
    }

    override fun componentWillUnmount() {
        scope.cancel()
    }

    override fun AppState.init() {
        locale = Locale.En
        screen = Screen.Welcome
        chatMessages = mutableListOf()
        errorMessage = ""
        secsToReconnect = 0
        map = GameMap
    }

    private fun CoroutineScope.connectToServer(requests: Channel<Request>, reconnecting: Boolean = false) {
        val protocol = if (window.location.protocol == "https:") "wss:" else "ws:"
        with(WebSocket("$protocol//" + window.location.host + "/ws")) {
            var job: Job? = null
            var pingHandle: Int? = null

            onopen = {
                job = launch {
                    if (reconnecting) {
                        (state.screen as? Screen.InGame)?.apply {
                            requests.send(JoinGameRequest(gameId, me.name))
                        }
                    }
                    for (req in requests) {
                        send(json.stringify(Request.serializer(), req))
                    }
                }

                pingHandle = window.setInterval({ send(Request.Ping) }, 10000)

                Unit
            }

            onmessage = { msg ->
                (msg.data as? String)?.let { reqStr ->
                    if (reqStr != Response.Pong)
                        processMessageFromServer(json.parse(Response.serializer(), reqStr))
                }
            }

            onclose = { e ->
                val reason = e.asDynamic().reason as? String
                pingHandle?.let { window.clearInterval(it) }
                job?.cancel()
                var reconnectTimeoutHandle = 0
                reconnectTimeoutHandle = window.setInterval({
                    setState {
                        secsToReconnect -= 1
                        errorMessage = str.disconnected(reason to secsToReconnect)
                        if (secsToReconnect == 0) {
                            showErrorMessage = false
                            window.clearInterval(reconnectTimeoutHandle)
                            connectToServer(requests, true)
                        }
                    }
                }, 1000)
                setState {
                    secsToReconnect = ErrorMessageTimeoutSecs
                    errorMessage = str.disconnected(reason to secsToReconnect)
                    showErrorMessage = true
                }
            }
        }
    }

    override fun RBuilder.render() {
        state.screen.let {
            when (it) {
                is Screen.Welcome ->
                    welcomeScreen {
                        locale = state.locale
                        onLocaleChanged = ::onLocaleChanged
                        onStartGame = ::startGame
                        onJoinGame = ::joinGame
                    }

                is Screen.ShowGameId ->
                    showGameIdScreen {
                        gameId = it.gameId
                        locale = state.locale
                        onClosed = {
                            setState {
                                screen = Screen.GameInProgress(
                                    it.gameId,
                                    state.map,
                                    it.gameState,
                                    PlayerState.initial(state.map, it.gameState, requests)
                                )
                            }
                        }
                    }

                is Screen.GameInProgress ->
                    gameScreen {
                        locale = state.locale
                        gameMap = it.gameMap
                        gameState = it.gameState
                        playerState = it.playerState
                        onAction = { newState -> setState { screen = it.copy(playerState = newState) } }
                        chatMessages = state.chatMessages
                        onSendMessage = { message -> requests.offer(ChatMessageRequest(message)) }
                    }

                is Screen.GameOver -> {
                    val allPlayers = it.players.map { it.first }
                    endScreen {
                        locale = state.locale
                        gameMap = it.gameMap
                        players = it.players.map { (player, tickets) ->
                            PlayerFinalStats(
                                player,
                                tickets,
                                allPlayers,
                                state.map
                            )
                        }
                        chatMessages = state.chatMessages
                        onSendMessage = { message -> requests.offer(ChatMessageRequest(message)) }
                    }
                }
            }
        }

        errorMessage()
    }

    private fun RBuilder.errorMessage() {
        mSnackbar(state.errorMessage, state.showErrorMessage, autoHideDuration = 4000) {
            attrs {
                autoHideDuration = ErrorMessageTimeoutSecs * 1000
                onClose = { _, _ -> setState { showErrorMessage = false } }
            }
            mPaper(elevation = 6) {
                css {
                    display = Display.flex
                    flexDirection = FlexDirection.row
                    padding = 10.px.toString()
                    color = Color.white
                    backgroundColor = Color.orangeRed
                    minWidth = 300.px
                    maxWidth = 600.px
                }
                mIcon("error_outline") {
                    css { marginRight = 12.px }
                }
                mTypography(state.errorMessage, MTypographyVariant.body1)
            }
        }
    }

    private fun processMessageFromServer(msg: Response) = when (msg) {
        is Response.ErrorMessage -> setState {
            errorMessage = msg.text
            showErrorMessage = true
        }

        is Response.ChatMessage -> setState {
            chatMessages = chatMessages.apply { add(msg) }
        }

        is Response.GameState -> with(state.screen) {
            when (this) {

                is Screen.Welcome -> {
                    setState {
                        screen = if (msg.state.players.size == 1) {
                            val url = "${window.location.origin}/game/${msg.gameId.value}"
                            window.history.pushState(null, window.document.title, url)
                            Screen.ShowGameId(msg.gameId, msg.state)
                        } else {
                            Screen.GameInProgress(
                                msg.gameId,
                                state.map,
                                msg.state,
                                PlayerState.initial(state.map, msg.state, requests)
                            )
                        }
                        msg.action?.let {
                            chatMessages = chatMessages.apply { add(it.chatMessage(state.locale)) }
                        }
                    }
                }

                is Screen.ShowGameId -> setState {
                    screen = copy(gameState = msg.state)
                    msg.action?.let {
                        chatMessages = chatMessages.apply { add(it.chatMessage(state.locale)) }
                    }
                }

                is Screen.GameInProgress -> {
                    if (!gameState.myTurn && msg.state.myTurn) {
                        Notification("Ticket to Ride", jsObject {
                            body = str.yourTurn
                            icon = "/favicon.ico"
                            silent = true
                            renotify = false
                            tag = "your-turn"
                        })
                    }
                    val newPlayerState =
                        if (playerState is PlayerState.ChoosingTickets) playerState
                        else PlayerState.initial(gameMap, msg.state, requests)
                    setState {
                        screen = copy(gameState = msg.state, playerState = newPlayerState)
                        msg.action?.let {
                            chatMessages = chatMessages.apply { add(it.chatMessage(state.locale)) }
                        }
                    }
                }

                is Screen.GameOver -> {
                }
            }

        }

        is Response.GameEnd -> setState {
            (state.screen as? Screen.GameInProgress)?.let {
                screen = Screen.GameOver(
                    msg.gameId,
                    it.gameState.me,
                    state.map,
                    msg.players
                )
                msg.action?.chatMessage(state.locale)?.let {
                    chatMessages = chatMessages.apply { add(it) }
                }
            }
        }
    }

    private fun startGame(playerName: PlayerName, carsNumber: Int) {
        requests.offer(StartGameRequest(playerName, carsNumber))
    }

    private fun joinGame(gameId: GameId, playerName: PlayerName) {
        requests.offer(JoinGameRequest(gameId, playerName))
    }

    private fun onLocaleChanged(value: Locale) = setState { locale = value }

    private inner class Strings : LocalizedStrings({ state.locale }) {

        val disconnected by locWithParam<Pair<String?, Int>>(
            Locale.En to { (reason, secsToReconnect) ->
                val additionalInfo = reason?.takeIf { it.isNotBlank() }?.let { ": $it" } ?: ""
                "Disconnected from server$additionalInfo. Trying to reconnect in $secsToReconnect seconds..."
            },
            Locale.Ru to { (reason, secsToReconnect) ->
                val additionalInfo = reason?.takeIf { it.isNotBlank() }?.let { ": $it" } ?: ""
                "Потеряно соединение$additionalInfo. Попытка переподключения через $secsToReconnect секунд..."
            }
        )

        val yourTurn by loc(
            Locale.En to "It's your turn to make a move!",
            Locale.Ru to "Ваш ход!"
        )
    }

    private val str = Strings()
}