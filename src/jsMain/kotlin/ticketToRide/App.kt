package ticketToRide

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.button.variant
import kotlinext.js.jsObject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.css.*
import org.w3c.notifications.Notification
import react.*
import styled.css
import ticketToRide.playerState.PlayerState
import ticketToRide.screens.*
import ticketToRide.ConnectResponse.*
import kotlin.browser.window

enum class ConnectionState {
    NotConnected {
        override val showErrorMessage = false
    },
    Connected {
        override val showErrorMessage = false
    },
    Reconnecting {
        override val showErrorMessage = true
    },
    CannotJoinGame {
        override val showErrorMessage = true
    },
    CannotConnect {
        override val showErrorMessage = true
    };

    abstract val showErrorMessage: Boolean
}

interface AppState : RState {
    var screen: Screen
    var locale: Locale
    var chatMessages: MutableList<Response.ChatMessage>
    var errorMessage: String
    var connectionState: ConnectionState
    var showErrorMessage: Boolean
    var map: GameMap
}

private const val ErrorMessageTimeoutSecs = 4

class App : RComponent<RProps, AppState>(), AppComponent {
    private val rootScope = CoroutineScope(Dispatchers.Default + Job())

    override fun componentWillUnmount() {
        rootScope.cancel()
    }

    override fun AppState.init() {
        locale = Locale.En
        screen = Screen.Welcome
        chatMessages = mutableListOf()
        connectionState = ConnectionState.NotConnected
        errorMessage = ""
    }

    override fun onConnected(connection: ServerConnection) {
        setState {
            connectionState = ConnectionState.Connected
            (state.screen as? Screen.InGame)?.let {
                screen = it.onReconnect(connection.requests)
            }
        }
    }

    override fun onReconnecting(reason: String?, secsToReconnect: Int) {
        setState {
            connectionState = ConnectionState.Reconnecting
            errorMessage =
                if (secsToReconnect > 0) str.disconnected(reason to secsToReconnect)
                else str.reconnecting
        }
    }

    override fun cannotJoinGame(reason: Failure) = setState {
        connectionState =
            if (reason == Failure.CannotConnect) ConnectionState.CannotConnect
            else ConnectionState.CannotJoinGame
        errorMessage = when (reason) {
            is Failure.GameIdTaken -> str.gameIdTaken
            is Failure.NoSuchGame -> str.noSuchGame
            is Failure.PlayerNameTaken -> str.playerNameTaken
            is Failure.CannotConnect -> str.cannotConnect
        }
    }

    override val me
        get() = (state.screen as? Screen.InGame)?.me

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
                                    it.requests,
                                    it.gameState,
                                    PlayerState.initial(state.map, it.gameState, it.requests)
                                )
                            }
                        }
                    }

                is Screen.GameInProgress ->
                    gameScreen {
                        locale = state.locale
                        connected = state.connectionState == ConnectionState.Connected
                        gameMap = state.map
                        gameState = it.gameState
                        playerState = it.playerState
                        onAction = { newState -> setState { screen = it.copy(playerState = newState) } }
                        chatMessages = state.chatMessages
                        onSendMessage = { message -> it.requests.offer(Request.ChatMessage(message)) }
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
                        onSendMessage = { message -> it.requests.offer(Request.ChatMessage(message)) }
                    }
                }
            }
        }

        errorMessage()
    }

    private fun RBuilder.errorMessage() {
        mSnackbar(state.errorMessage) {
            attrs {
                open = state.showErrorMessage || state.connectionState.showErrorMessage
                onClose = { _, _ -> setState { showErrorMessage = false } }
                if (state.connectionState == ConnectionState.CannotJoinGame)
                    autoHideDuration = ErrorMessageTimeoutSecs * 1000
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
                    alignItems = Align.center
                }
                mIcon("error_outline") {
                    css { marginRight = 12.px }
                }
                mTypography(state.errorMessage, MTypographyVariant.body1)

                if (state.connectionState == ConnectionState.CannotConnect) {
                    (state.screen as? Screen.InGame)?.let { inGameScreen ->
                        mButton(str.retryConnect) {
                            attrs {
                                css {
                                    marginLeft = 10.pt
                                    color = Color.white
                                }
                                variant = MButtonVariant.text
                                onClick = {
                                    setState {
                                        connectionState = ConnectionState.Reconnecting
                                        errorMessage = str.reconnecting
                                    }
                                    rootScope.joinGame(this@App, inGameScreen.gameId, inGameScreen.me.name)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun processMessageFromServer(msg: Response, requests: SendChannel<Request>) = when (msg) {

        is Response.ErrorMessage -> setState {
            errorMessage = msg.text
            showErrorMessage = true
        }

        is Response.ChatMessage -> setState {
            chatMessages = chatMessages.apply { add(msg) }
        }

        is Response.GameMap -> setState { map = msg.map }

        is Response.GameState -> with(state.screen) {
            when (this) {

                is Screen.Welcome -> setState {
                    screen = if (msg.state.players.size == 1) {
                        val url = "${window.location.origin}/game/${msg.gameId.value}"
                        window.history.pushState(null, window.document.title, url)
                        Screen.ShowGameId(msg.gameId, requests, msg.state)
                    } else {
                        Screen.GameInProgress(
                            msg.gameId,
                            requests,
                            msg.state,
                            PlayerState.initial(state.map, msg.state, requests)
                        )
                    }
                    msg.action?.let {
                        chatMessages = chatMessages.apply { add(it.chatMessage(state.locale)) }
                    }
                }

                is Screen.ShowGameId -> setState {
                    screen = withGameState(msg.state)
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
                        else PlayerState.initial(state.map, msg.state, requests)
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
                    it.requests,
                    state.map,
                    msg.players
                )
                msg.action?.chatMessage(state.locale)?.let {
                    chatMessages = chatMessages.apply { add(it) }
                }
            }
        }

    }

    private fun startGame(map: GameMap, playerName: PlayerName, carsCount: Int) =
        rootScope.startGame(this@App, ConnectRequest.StartGame(playerName, map, carsCount), 0)

    private fun joinGame(gameId: GameId, playerName: PlayerName) =
        rootScope.joinGame(this@App, gameId, playerName)

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

        val reconnecting by loc(
            Locale.En to "Trying to reconnect...",
            Locale.Ru to "Устанавливаю соединение..."
        )

        val yourTurn by loc(
            Locale.En to "It's your turn to make a move!",
            Locale.Ru to "Ваш ход!"
        )

        val gameIdTaken by loc(
            Locale.En to "Could not start game (could not generate unique game id)",
            Locale.Ru to "Не удалось запустить игру (сгенерировать уникальный id)"
        )

        val noSuchGame by loc(
            Locale.En to "Game with this id does not exist",
            Locale.Ru to "Игра по ссылке не найдена"
        )

        val playerNameTaken by loc(
            Locale.En to "This name is taken by another player",
            Locale.Ru to "Имя уже занято другим игроком"
        )

        val cannotConnect by loc(
            Locale.En to "Cannot establish WebSocket connection with the server",
            Locale.Ru to "Не удалось установить WebSocket-соединение с сервером"
        )

        val retryConnect by loc(
            Locale.En to "Retry",
            Locale.Ru to "Попытаться снова"
        )
    }

    private val str = Strings()
}