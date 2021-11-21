package ticketToRide

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.mIconButton
import kotlinext.js.jsObject
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.css.*
import org.w3c.notifications.Notification
import react.*
import styled.css
import ticketToRide.ConnectResponse.*
import ticketToRide.ConnectionState.*
import ticketToRide.playerState.PlayerState
import ticketToRide.screens.*

external interface AppProps: RProps {
    var onGameStarted: () -> Unit
}

external interface AppState: RState {
    var gameIdBoxed: IGameId?
    var screen: Screen
    var locale: Locale
    var calculateScoresInProcess: Boolean
    var chatMessages: MutableList<Response.ChatMessage>
    var errorMessage: String
    var connectionState: ConnectionState
    var showErrorMessage: Boolean
    var map: GameMap
}
val AppState.gameId get() = gameIdBoxed?.unboxed

private const val ErrorMessageTimeoutSecs = 4
private const val RetryTimeoutSecs = 5

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
class App : RComponent<AppProps, AppState>() {

    private val rootScope = CoroutineScope(Dispatchers.Default + Job())
    private val requests = Channel<Request>(Channel.CONFLATED)
    private var connection: IServerConnection? = null

    override fun componentWillUnmount() {
        rootScope.cancel()
    }

    override fun AppState.init() {
        gameIdBoxed =
            if (window.location.pathname.startsWith("/game/")) GameId(window.location.pathname.substringAfterLast('/'))
            else null

        locale = Locale.En
        screen = Screen.Welcome
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
                is Failure.NoSuchPlayer -> "A player with this name haven't joined this game"
            }
            showErrorMessage = true
        }
    }

    override fun RBuilder.render() {
        state.screen.let {
            when (it) {
                is Screen.Welcome ->
                    welcomeScreen {
                        gameIdBoxed = state.gameIdBoxed
                        locale = state.locale
                        onLocaleChanged = ::onLocaleChanged
                        onStartGame = { map, playerName, playerColor, carsCount, calculateScoresInProcess ->
                            startGame(map, playerName, playerColor, carsCount, calculateScoresInProcess)
                        }
                        onJoinGame = { name, color ->
                            joinGame(ConnectRequest.Join(name, color), name)
                        }
                        onJoinAsObserver = ::joinAsObserver
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
                                props.onGameStarted()
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
                        onSendMessage = { message -> requests.trySend(ChatMessage(message)).isSuccess }
                    }

                is Screen.GameOver -> {
                    val allPlayers = it.players.map { it.first }
                    finalScreen {
                        locale = state.locale
                        observing = it.observing
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
                        onSendMessage = { message -> requests.trySend(ChatMessage(message)).isSuccess }
                    }
                }

                is Screen.ObserveGameInProgress ->
                    observeGameScreen {
                        locale = state.locale
                        calculateScores = state.calculateScoresInProcess
                        connected = state.connectionState == Connected
                        gameMap = state.map
                        gameState = it.gameState
                        chatMessages = state.chatMessages
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
        ServerConnection(rootScope, GameId.random().webSocketUrl, Response.serializer()) {
            val request = ConnectRequest.Start(playerName, playerColor, map, carsCount, calculateScoresInProcess)
            when (val connectResponse = connect(request)) {
                is PlayerConnected -> runAsPlayer(playerName, connectResponse)
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
            ServerConnection(rootScope, gameId.webSocketUrl, Response.serializer()) {
                when (val connectResponse = connect(request)) {
                    is PlayerConnected -> runAsPlayer(playerName, connectResponse)
                    is Failure -> {
                        close()
                        cannotJoinGame(connectResponse)
                    }
                }
            }
        } ?: throw Error("Cannot join game without game id")
    }

    private fun joinAsObserver() {
        state.gameIdBoxed?.unboxed?.let { gameId ->
            ServerConnection(rootScope, gameId.webSocketUrl, GameStateForObserver.serializer()) {
                when (val connectResponse = connect(ConnectRequest.Observe)) {
                    is ObserverConnected -> runAsObserver(connectResponse)
                    is Failure -> {
                        close()
                        cannotJoinGame(connectResponse)
                    }
                }
            }
        } ?: throw Error("Cannot join game without game id")
    }

    private suspend fun ServerConnection<Response>.runAsPlayer(playerName: PlayerName, connectResponse: PlayerConnected) {
        onDisconnected = { reason -> handleLostConnection(playerName, reason) }
        window.addEventListener("onbeforeunload", { requests.trySend(LeaveGameRequest).isSuccess })
        runRequestSendingLoop(requests, Request.serializer())
        connectResponse.apply { runGame(id, map, Response.GameState(state), ::processResponse) }
    }

    private suspend fun ServerConnection<GameStateForObserver>.runAsObserver(connectResponse: ObserverConnected) {
        onDisconnected = { reason -> handleLostConnection(reason) }
        connectResponse.apply { runGame(id, map, state, ::processGameStateForObserver) }
    }

    private suspend fun <T> ServerConnection<T>.runGame(id: GameId, map: GameMap, initialState: T, processResponse: (T) -> Unit) {
        connection = this
        setState {
            gameIdBoxed = id
            this.map = map
            calculateScoresInProcess = calculateScoresInProcess
            showErrorMessage = false
        }
        processResponse(initialState)
        coroutineScope {
            launch { connectionState.collect { onConnectionStateChanged(it) } }
            launch { responses().collect { processResponse(it) } }
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

        is Response.GameState -> processGameState(msg.state, msg.action)

        is Response.GameEnd -> setState {
            (state.screen as? Screen.GameInProgress)?.let {
                screen = Screen.GameOver(it.gameId, state.map, false, msg.players)
                msg.action?.chatMessage(state.map, state.locale)?.let {
                    chatMessages = chatMessages.apply { add(it) }
                }
            }
        }
    }

    private fun processGameState(gameState: GameStateView, gameAction: PlayerAction?) {
        with(state.screen) {
            when (this) {

                is Screen.Welcome -> state.gameId?.let { gameId ->
                    setState {
                        screen = if (gameState.players.size == 1) {
                            val url = "${window.location.origin}/game/${gameId.value}"
                            window.history.pushState(null, window.document.title, url)
                            Screen.ShowGameId(gameId, gameState)
                        } else {
                            props.onGameStarted()
                            Screen.GameInProgress(
                                gameId,
                                gameState,
                                PlayerState.initial(state.map, gameState, requests)
                            )
                        }
                    }
                } ?: throw Error("Received game state while game id is not known")

                is Screen.ShowGameId -> setState {
                    screen = withGameState(gameState)
                    gameAction?.let {
                        chatMessages = chatMessages.apply { add(it.chatMessage(state.map, state.locale)) }
                    }
                }

                is Screen.GameInProgress -> {
                    if (!gameState.myTurn && gameState.myTurn) {
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
                        else PlayerState.initial(state.map, gameState, requests)
                    setState {
                        screen = copy(gameState = gameState, playerState = newPlayerState)
                        gameAction?.let {
                            chatMessages = chatMessages.apply { add(it.chatMessage(state.map, state.locale)) }
                        }
                    }
                }

                else -> {
                }
            }
        }
    }

    private fun processGameStateForObserver(gameState: GameStateForObserver) = state.gameId?.let { gameId ->
        setState {
            if (screen is Screen.Welcome) {
                props.onGameStarted()
            }

            screen =
                if (gameState.gameEnded) Screen.GameOver(gameId, state.map, true, gameState.players.zip(gameState.tickets))
                else Screen.ObserveGameInProgress(gameState)
            gameState.action?.let {
                chatMessages = chatMessages.apply { add(it.chatMessage(state.map, state.locale)) }
            }
        }
    }

    private fun ServerConnection<Response>.handleLostConnection(playerName: PlayerName, reason: String?) =
        handleLostConnection(ConnectRequest.Reconnect(playerName), reason)

    private fun ServerConnection<GameStateForObserver>.handleLostConnection(reason: String?) =
        handleLostConnection(ConnectRequest.Observe, reason)

    private fun <T> ServerConnection<T>.handleLostConnection(request: ConnectRequest, reason: String?) {
        rootScope.launch {
            for (countdown in RetryTimeoutSecs downTo 1) {
                setState { errorMessage = str.disconnected(reason to RetryTimeoutSecs) }
                delay(1000)
            }
            setState { errorMessage = str.reconnecting }
            val resp = reconnect(request)
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