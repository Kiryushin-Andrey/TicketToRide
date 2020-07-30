package ticketToRide

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.mIconButton
import kotlinext.js.jsObject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.css.*
import org.w3c.notifications.Notification
import react.*
import styled.css
import ticketToRide.ConnectResponse.Failure
import ticketToRide.ConnectResponse.Success
import ticketToRide.ConnectionState.*
import ticketToRide.playerState.PlayerState
import ticketToRide.screens.endScreen
import ticketToRide.screens.gameScreen
import ticketToRide.screens.showGameIdScreen
import ticketToRide.screens.welcomeScreen
import kotlin.browser.window

interface AppState : RState {
    var gameId: GameId?
    var screen: Screen
    var locale: Locale
    var calculateScoresInProcess: Boolean
    var chatMessages: MutableList<Response.ChatMessage>
    var errorMessage: String
    var connectionState: ConnectionState
    var showErrorMessage: Boolean
    var map: GameMap
}

private const val ErrorMessageTimeoutSecs = 4
private const val RetryTimeoutSecs = 5

class App : RComponent<RProps, AppState>() {

    private val rootScope = CoroutineScope(Dispatchers.Default + Job())
    private val requests = Channel<Request>(Channel.CONFLATED)
    private var connection: ServerConnection? = null

    override fun componentWillUnmount() {
        rootScope.cancel()
    }

    override fun AppState.init() {
        gameId =
            if (window.location.pathname.startsWith("/game/")) GameId(window.location.pathname.substringAfterLast('/'))
            else null

        locale = Locale.En
        screen = Screen.Welcome(gameId?.let { listOf<PlayerView>() } ?: listOf())
        chatMessages = mutableListOf()
        connectionState = NotConnected
        errorMessage = ""
    }

    private fun cannotJoinGame(reason: Failure) {
        setState {
            errorMessage = when (reason) {
                is Failure.GameIdTaken -> str.gameIdTaken
                is Failure.NoSuchGame -> str.noSuchGame
                is Failure.PlayerNameTaken -> str.playerNameTaken
                is Failure.PlayerColorTaken -> str.playerColorTaken
                is Failure.CannotConnect -> str.cannotConnect
                // an exceptional situation, should never occur by design
                is Failure.NoSuchPlayer -> throw Error("A player with this name haven't joined this game")
            }
            showErrorMessage = true
        }
    }

    override fun RBuilder.render() {
        state.screen.let {
            when (it) {
                is Screen.Welcome ->
                    welcomeScreen {
                        gameId = state.gameId
                        locale = state.locale
                        onLocaleChanged = ::onLocaleChanged
                        onStartGame = { map, playerName, playerColor, carsCount, calculateScoresInProcess ->
                            startGame(map, playerName, playerColor, carsCount, calculateScoresInProcess)
                        }
                        onJoinGame = { name, color ->
                            joinGame(ConnectRequest.Join(name, color), name)
                        }
                        onReconnect = { name ->
                            joinGame(ConnectRequest.Reconnect(name), name)
                        }
                    }

                is Screen.ShowGameId ->
                    showGameIdScreen {
                        gameId = it.gameId
                        locale = state.locale
                        onClosed = {
                            setState {
                                screen = Screen.GameInProgress(
                                    it.gameId,
                                    it.gameState,
                                    PlayerState.initial(state.map, it.gameState, requests)
                                )
                            }
                        }
                    }

                is Screen.GameInProgress ->
                    gameScreen {
                        locale = state.locale
                        calculateScores = state.calculateScoresInProcess
                        connected = state.connectionState == Connected
                        gameMap = state.map
                        gameState = it.gameState
                        playerState = it.playerState
                        onAction = { newState -> setState { screen = it.copy(playerState = newState) } }
                        chatMessages = state.chatMessages
                        onSendMessage = { message -> requests.offer(Request.ChatMessage(message)) }
                    }

                is Screen.GameOver -> {
                    val allPlayers = it.players.map { it.first }
                    endScreen {
                        locale = state.locale
                        gameMap = it.gameMap
                        players = it.players.map { (player, tickets) ->
                            PlayerScore(
                                player.name,
                                player.color,
                                player.occupiedSegments,
                                player.placedStations,
                                tickets,
                                allPlayers.filter { it != player }.flatMap { it.occupiedSegments },
                                state.map
                            )
                        }
                        chatMessages = state.chatMessages
                        onSendMessage = { message -> requests.offer(Request.ChatMessage(message)) }
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
                if (state.connectionState == CannotJoinGame)
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

                if (state.connectionState == CannotConnect) {
                    (state.screen as? Screen.InGame)?.me?.let { me ->
                        mTooltip(str.retryConnect) {
                            mIconButton("autorenew") {
                                attrs {
                                    css {
                                        marginLeft = 10.pt
                                    }
                                    onClick = { reconnect(me.name) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startGame(
        map: GameMap,
        playerName: PlayerName,
        playerColor: PlayerColor,
        carsCount: Int,
        calculateScoresInProcess: Boolean,
        retriesCount: Int = 0
    ) {
        ServerConnection(rootScope, GameId.random().webSocketUrl) {
            val request = ConnectRequest.Start(playerName, playerColor, map, carsCount, calculateScoresInProcess)
            when (val connectResponse = connect(request)) {
                is Success -> runGame(playerName)
                is Failure -> {
                    close()
                    if (connectResponse is Failure.GameIdTaken && retriesCount < ServerConnection.MaxRetriesCount)
                        startGame(map, playerName, playerColor, carsCount, calculateScoresInProcess, retriesCount + 1)
                    else
                        cannotJoinGame(connectResponse)
                }
            }
        }
    }

    private fun joinGame(request: ConnectRequest, playerName: PlayerName) {
        state.gameId?.let { gameId ->
            ServerConnection(rootScope, gameId.webSocketUrl) {
                when (val connectResponse = connect(request)) {
                    is Success -> runGame(playerName)
                    is Failure -> {
                        close()
                        cannotJoinGame(connectResponse)
                    }
                }
            }
        } ?: throw Error("Cannot join game without game id")
    }

    private suspend fun ServerConnection.runGame(playerName: PlayerName) {
        connection = this
        onDisconnected = { reason -> handleLostConnection(playerName, reason) }
        window.addEventListener("onbeforeunload", { requests.offer(LeaveGameRequest) })
        runRequestSendingLoop(requests, Request.serializer())
        coroutineScope {
            launch { connectionState.collect { onConnectionStateChanged(it) } }
            launch { responses(Response.serializer()).collect { processResponse(it) } }
        }
    }

    private fun processResponse(msg: Response) = when (msg) {

        is Response.ErrorMessage -> setState {
            errorMessage = msg.text
            showErrorMessage = true
        }

        is Response.ChatMessage -> setState {
            chatMessages = chatMessages.apply { add(msg) }
        }

        is Response.GameStateWithMap -> when (state.screen) {
            is Screen.Welcome -> setState {
                map = msg.map
                calculateScoresInProcess = msg.calculateScoresInProcess
                screen = if (msg.state.players.size == 1) {
                    val url = "${window.location.origin}/game/${msg.gameId.value}"
                    window.history.pushState(null, window.document.title, url)
                    Screen.ShowGameId(msg.gameId, msg.state)
                } else {
                    Screen.GameInProgress(
                        msg.gameId,
                        msg.state,
                        PlayerState.initial(msg.map, msg.state, requests)
                    )
                }
            }
            else -> throw Error("Unexpected response from server")
        }

        is Response.GameState -> with(state.screen) {
            when (this) {

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

                else -> {
                }
            }
        }

        is Response.GameEnd -> setState {
            (state.screen as? Screen.GameInProgress)?.let {
                screen = Screen.GameOver(
                    it.gameId,
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

    private fun ServerConnection.handleLostConnection(playerName: PlayerName, reason: String?) {
        rootScope.launch {
            for (countdown in RetryTimeoutSecs downTo 1) {
                setState { errorMessage = str.disconnected(reason to RetryTimeoutSecs) }
                delay(1000)
            }
            setState { errorMessage = str.reconnecting }
            val resp = reconnect(ConnectRequest.Reconnect(playerName))
            if (resp is Failure)
                cannotJoinGame(resp)
        }
    }

    private fun reconnect(playerName: PlayerName) {
        rootScope.launch {
            connection?.reconnect(ConnectRequest.Reconnect(playerName))
        }
    }

    private fun onConnectionStateChanged(newState: ConnectionState) = setState {
        connectionState = newState
        when (newState) {
            CannotConnect ->
                errorMessage = str.cannotConnect
            Reconnecting ->
                errorMessage = str.reconnecting
            else -> {
            }
        }
    }

    private val ConnectionState.showErrorMessage
        get() =
            this == Reconnecting || this == CannotConnect || this == CannotJoinGame

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

        val playerColorTaken by loc(
            Locale.En to "This color is taken by another player",
            Locale.Ru to "Цвет занят другим игроком"
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