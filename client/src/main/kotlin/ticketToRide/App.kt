package ticketToRide

import csstype.*
import hookstate.Hookstate
import hookstate.HookstateRoot
import hookstate.getNoProxy
import hookstate.useHookstate
import js.core.jso
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mui.icons.material.Autorenew
import mui.icons.material.ErrorOutline
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import org.w3c.notifications.Notification
import org.w3c.notifications.NotificationOptions
import react.*
import ticketToRide.ConnectResponse.*
import ticketToRide.ConnectionState.*
import ticketToRide.playerState.PlayerState
import ticketToRide.screens.*

external interface AppProps: Props {
    var onGameStarted: () -> Unit
}

private const val ERROR_MESSAGE_TIMEOUT_SECS = 4
private const val RETRY_TIMEOUT_SECS = 5

private val rootScope = CoroutineScope(Dispatchers.Default + Job())
private val requests = Channel<Request>(Channel.CONFLATED)
private var connection: IServerConnection? = null

external interface AppState : HookstateRoot<AppStateValue> {
    var screen: Hookstate<Screen>
    var locale: Hookstate<Locale>
    var map: Hookstate<GameMap>
    var chatMessages: Hookstate<Array<Response.ChatMessage>>
    var connectionState: Hookstate<ConnectionState>
    var errorMessage: Hookstate<String>
    var showErrorMessage: Hookstate<Boolean>
}

external interface AppStateValue {
    var screen: Screen
    var locale: Locale
    var map: GameMap
    var chatMessages: Array<Response.ChatMessage>
    var connectionState: ConnectionState
    var errorMessage: String
    var showErrorMessage: Boolean
    var onGameStarted: () -> Unit
}

val App = FC<AppProps> { props ->
    val appState: AppState = useHookstate(jso<AppStateValue> {
        screen = Screen.Welcome
        locale = Locale.En
        connectionState = NotConnected
        errorMessage = ""
        chatMessages = emptyArray()
        onGameStarted = props.onGameStarted
    })

    val str = useMemo(appState.locale) { AppStrings(appState.locale.get()) }
    val screen = appState.screen

    screen.get().let {
        when (it) {
            is Screen.Welcome -> {
                WelcomeScreen {
                    locale = appState.locale.get()
                    onLocaleChanged = { appState.locale.set(it) }
                    onStartGame = { map, playerName, playerColor, carsCount, calculateScoresInProcess ->
                        startGame(map, playerName, playerColor, carsCount, calculateScoresInProcess, appState, str)
                    }
                    onJoinGame = { gameId, name, color ->
                        joinGame(gameId, ConnectRequest.Join(name, color), name, appState, str)
                    }
                    onJoinAsObserver = { gameId ->
                        joinAsObserver(gameId, appState, str)
                    }
                    onReconnect = { gameId, name ->
                        joinGame(gameId, ConnectRequest.Reconnect(name), name, appState, str)
                    }
                }
            }

            is Screen.ShowGameId ->
                ShowGameIdScreen {
                    gameId = it.gameId
                    locale = appState.locale.get()
                    onClosed = {
                        props.onGameStarted()
                        screen.set(
                            Screen.GameInProgress(
                                it.gameId,
                                it.gameState,
                                PlayerState.initial(appState.map.get(), it.gameState, requests)
                            )
                        )
                    }
                    playerName = it.me.name
                    playerColor = it.me.color
                }

            is Screen.GameInProgress ->
                GameScreen {
                    locale = appState.locale.get()
                    connected = appState.connectionState.get() == Connected
                    gameMap = appState.map.get()
                    gameState = it.gameState
                    playerState = it.playerState
                    act = { action -> appState.screen.set(it.copy(playerState = it.playerState.action())) }
                    chatMessages = appState.chatMessages.get()
                    onSendMessage = { message -> requests.trySend(ChatMessage(message)).isSuccess }
                }

            is Screen.GameOver -> {
                val allPlayers = it.players.map { it.first }
                FinalScreen {
                    locale = appState.locale.get()
                    gameMap = appState.map.get()
                    chatMessages = appState.chatMessages.get()
                    observing = it.observing
                    players = it.players.map { (player, tickets) ->
                        PlayerScore(
                            player.name,
                            player.color,
                            player.occupiedSegments,
                            player.placedStations,
                            tickets,
                            allPlayers.filter { it != player }.flatMap { it.occupiedSegments },
                            appState.map.get()
                        )
                    }
                    onSendMessage = { message -> requests.trySend(ChatMessage(message)).isSuccess }
                }
            }

            is Screen.ObserveGameInProgress ->
                ObserveGameScreen {
                    locale = appState.locale.get()
                    gameMap = appState.map.get()
                    chatMessages = appState.chatMessages.get()
                    connected = appState.connectionState.get() == Connected
                    gameState = it.gameState
                }
        }
    }

    errorMessage(appState, str)
}

private fun startGame(
    map: ConnectRequest.StartGameMap,
    playerName: PlayerName,
    playerColor: PlayerColor,
    carsCount: Int,
    calculateScoresInProcess: Boolean,
    appState: AppState,
    str: AppStrings,
    retriesCount: Int = 0
) {
    ServerConnection(rootScope, GameId.random().webSocketUrl, Response.serializer()) {
        val request = ConnectRequest.Start(playerName, playerColor, map, carsCount, calculateScoresInProcess)
        when (val connectResponse = connect(request)) {
            is PlayerConnected ->
                runAsPlayer(playerName, connectResponse, appState, str)
            is Failure -> {
                close()
                if (connectResponse is Failure.GameIdTaken && retriesCount < ServerConnection.MaxRetriesCount)
                    startGame(map, playerName, playerColor, carsCount, calculateScoresInProcess, appState, str,retriesCount + 1)
                else
                    cannotJoinGame(connectResponse, appState, str)
            }
            else -> {}
        }
    }
}

private fun joinGame(gameId: GameId, request: ConnectRequest, playerName: PlayerName, appState: AppState, str: AppStrings) {
    ServerConnection(rootScope, gameId.webSocketUrl, Response.serializer()) {
        when (val connectResponse = connect(request)) {
            is PlayerConnected ->
                runAsPlayer(playerName, connectResponse, appState, str)

            is Failure -> {
                close()
                cannotJoinGame(connectResponse, appState, str)
            }

            else -> {}
        }
    }
}

private fun joinAsObserver(gameId: GameId, appState: AppState, str: AppStrings) {
    ServerConnection(rootScope, gameId.webSocketUrl, GameStateForObserver.serializer()) {
        when (val connectResponse = connect(ConnectRequest.Observe)) {
            is ObserverConnected ->
                runAsObserver(connectResponse, appState, str)

            is Failure -> {
                close()
                cannotJoinGame(connectResponse, appState, str)
            }

            is PlayerConnected ->
                error("Unexpected PlayerConnected as response to join observer request")
        }
    }
}

private fun cannotJoinGame(reason: Failure, state: AppState, str: AppStrings) {
    state.merge(jso<AppStateValue> {
        showErrorMessage = true
        errorMessage = when (reason) {
            is Failure.GameIdTaken -> str.gameIdTaken
            is Failure.NoSuchGame -> str.noSuchGame
            is Failure.PlayerNameTaken -> str.playerNameTaken
            is Failure.PlayerColorTaken -> str.playerColorTaken
            is Failure.CannotConnect -> str.cannotConnect
            // an exceptional situation, should never occur by design
            is Failure.NoSuchPlayer -> "A player with this name haven't joined this game"
        }
    })
}


private fun ChildrenBuilder.errorMessage(state: AppState, str: AppStrings) {
    Snackbar {
        anchorOrigin = jso {
            horizontal = SnackbarOriginHorizontal.center
            vertical = SnackbarOriginVertical.bottom
        }
        open = state.showErrorMessage.get() || state.connectionState.get().showErrorMessage
        onClose = { _, _ -> state.showErrorMessage.set(false) }
        if (state.connectionState.get() == CannotJoinGame)
            autoHideDuration = ERROR_MESSAGE_TIMEOUT_SECS * 1000

        Paper {
            elevation = 6
            sx {
                display = Display.flex
                flexDirection = FlexDirection.row
                padding = 10.px
                color = NamedColor.white
                backgroundColor = NamedColor.orangered
                minWidth = 300.px
                maxWidth = 600.px
                alignItems = AlignItems.center
            }

            ErrorOutline {
                sx { marginRight = 12.px }
            }
            Typography {
                variant = TypographyVariant.body1
                +state.errorMessage.get()
            }

            if (state.connectionState.get() == CannotConnect) {
                (state.screen.get() as? Screen.InGame)?.me?.let { me ->
                    Tooltip {
                        title = ReactNode(str.retryConnect)
                        IconButton {
                            sx { marginLeft = 10.pt }
                            onClick = { reconnect(me.name, state, str) }
                            Autorenew()
                        }
                    }
                }
            }
        }
    }
}

private suspend fun ServerConnection<Response>.runAsPlayer(
    playerName: PlayerName,
    connectResponse: PlayerConnected,
    appState: AppState,
    str: AppStrings
) {
    onDisconnected = { reason -> handleLostConnection(playerName, reason, appState, str) }
    window.addEventListener("onbeforeunload", { requests.trySend(LeaveGameRequest).isSuccess })
    runRequestSendingLoop(requests, Request.serializer())
    connectResponse.apply {
        runGame(id, map, appState, str, Response.GameState(state)) { gameId, msg ->
            processResponse(gameId, msg, appState, str)
        }
    }
}

private suspend fun ServerConnection<GameStateForObserver>.runAsObserver(
    connectResponse: ObserverConnected,
    appState: AppState,
    str: AppStrings
) {
    onDisconnected = { reason -> handleLostConnection(reason, appState, str) }
    connectResponse.apply {
        runGame(id, map, appState, str, state) { gameId, gameState ->
            processGameStateForObserver(gameId, gameState, appState)
        }
    }
}

private suspend fun <T> ServerConnection<T>.runGame(
    gameId: GameId, gameMap: GameMap, appState: AppState, str: AppStrings, initialGameState: T, processResponse: (GameId, T) -> Unit
) {
    connection = this
    appState.merge(jso<AppStateValue> {
        this.map = gameMap
        this.showErrorMessage = false
    })
    processResponse(gameId, initialGameState)
    coroutineScope {
        launch { connectionState.collect { onConnectionStateChanged(it, appState, str) } }
        launch { responses().collect { processResponse(gameId, it) } }
    }
}

private fun processResponse(gameId: GameId, msg: Response, appState: AppState, str: AppStrings) = when (msg) {

    is Response.ErrorMessage -> appState.merge(jso<AppStateValue> {
        errorMessage = msg.text
        showErrorMessage = true
    })

    is Response.ChatMessage -> appState.chatMessages.merge(arrayOf(msg))

    is Response.GameState -> processGameState(gameId, appState, msg.state, msg.action, str)

    is Response.GameEnd -> {
        appState.screen.set(Screen.GameOver(gameId, observing = false, msg.players))
        msg.action?.let {
            appState.chatMessages.merge(arrayOf(it.chatMessage(appState.map.get(), appState.locale.get())))
        }
    }
}

private fun processGameState(gameId: GameId, appState: AppState, gameState: GameStateView, gameAction: PlayerAction?, str: AppStrings) {
    with(appState.screen.get()) {
        when (this) {

            is Screen.Welcome ->
                appState.screen.set(
                    if (gameState.players.size == 1) {
                        val url = "${window.location.origin}/game/${gameId.value}"
                        window.history.pushState(null, window.document.title, url)
                        Screen.ShowGameId(gameId, gameState)
                    } else {
                        appState.getNoProxy().onGameStarted()
                        Screen.GameInProgress(
                            gameId,
                            gameState,
                            PlayerState.initial(appState.map.get(), gameState, requests)
                        )
                    }
                )

            is Screen.ShowGameId -> {
                appState.screen.set(withGameState(gameState))
                gameAction?.let {
                    appState.chatMessages.merge(arrayOf(it.chatMessage(appState.map.get(), appState.locale.get())))
                }
            }

            is Screen.GameInProgress -> {
                if (!gameState.myTurn && gameState.myTurn) {
                    Notification("Ticket to Ride", object : NotificationOptions {
                        override var body: String? = str.yourTurn
                        override var icon: String? = "/favicon.ico"
                        override var silent: Boolean? = true
                        override var renotify: Boolean? = false
                        override var tag: String? = "your-turn"
                    })
                }
                val newPlayerState =
                    if (playerState is PlayerState.ChoosingTickets) playerState
                    else PlayerState.initial(appState.map.get(), gameState, requests)
                appState.screen.set(copy(gameState = gameState, playerState = newPlayerState))
                gameAction?.let {
                    appState.chatMessages.merge(arrayOf(it.chatMessage(appState.map.get(), appState.locale.get())))
                }
            }

            else -> {
            }
        }
    }
}

private fun processGameStateForObserver(gameId: GameId, gameState: GameStateForObserver, appState: AppState) {
    if (appState.screen.get() is Screen.Welcome) {
        appState.getNoProxy().onGameStarted()
    }

    appState.screen.set(
        if (gameState.gameEnded)
            Screen.GameOver(gameId, observing = true, gameState.players.zip(gameState.tickets))
        else
            Screen.ObserveGameInProgress(gameState)
    )
    gameState.action?.let {
        appState.chatMessages.merge(arrayOf(it.chatMessage(appState.map.get(), appState.locale.get())))
    }
}

private fun ServerConnection<Response>.handleLostConnection(playerName: PlayerName, reason: String?, appState: AppState, str: AppStrings) =
    handleLostConnection(ConnectRequest.Reconnect(playerName), reason, appState, str)

private fun ServerConnection<GameStateForObserver>.handleLostConnection(reason: String?, appState: AppState, str: AppStrings) =
    handleLostConnection(ConnectRequest.Observe, reason, appState, str)

private fun <T> ServerConnection<T>.handleLostConnection(request: ConnectRequest, reason: String?, appState: AppState, str: AppStrings) {
    rootScope.launch {
        for (countdown in RETRY_TIMEOUT_SECS downTo 1) {
            appState.errorMessage.set(str.disconnected(reason to countdown))
            delay(1000)
        }
        appState.errorMessage.set(str.reconnecting)
        val resp = reconnect(request)
        if (resp is Failure)
            cannotJoinGame(resp, appState, str)
    }
}

private fun reconnect(playerName: PlayerName, appState: AppState, str: AppStrings) {
    rootScope.launch {
        connection?.reconnect(ConnectRequest.Reconnect(playerName))?.let {
            if (it is Failure)
                cannotJoinGame(it, appState, str)
        }
    }
}

private fun onConnectionStateChanged(newState: ConnectionState, appState: AppState, str: AppStrings) {
    appState.merge(
        jso<AppStateValue> {
            connectionState = newState
            when (newState) {
                CannotConnect ->
                    errorMessage = str.cannotConnect
                Reconnecting ->
                    errorMessage = str.reconnecting
                else -> {}
            }
        }
    )
}

private val ConnectionState.showErrorMessage
    get() =
        this == Reconnecting || this == CannotConnect || this == CannotJoinGame

private class AppStrings(locale: Locale) : LocalizedStrings({ locale }) {

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
