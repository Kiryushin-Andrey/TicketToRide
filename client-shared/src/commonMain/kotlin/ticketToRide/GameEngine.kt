package ticketToRide

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import ticketToRide.ConnectResponse.*
import ticketToRide.ConnectionState.*
import ticketToRide.localization.AppStrings

private val rootScope = CoroutineScope(Dispatchers.Default + Job())
private val requests = Channel<Request>(Channel.CONFLATED)
private var connection: IServerConnection? = null

interface AppState {
    val serverHost: String
    val screen: Screen
    val locale: Locale
    val map: GameMap
    val chatMessages: Collection<Response.ChatMessage>
    val connectionState: ConnectionState
    val errorMessage: String
    val showErrorMessage: Boolean

    fun log(message: String)
    fun initMap(map: GameMap)
    fun setConnectionState(state: ConnectionState, errorMessage: String? = null)
    fun updateScreen(screen: Screen)
    fun showErrorMessage(message: String)
    fun appendChatMessage(message: Response.ChatMessage)
    fun notifyOnYourTurn()
}

fun sendToServer(req: Request) {
    requests.trySend(req).isSuccess
}

fun connectToServer(
    appState: AppState,
    appStrings: AppStrings,
    gameId: GameId,
    connectRequest: ConnectRequest,
    addExitAppListener: (handler: () -> Unit) -> Unit = {},
    handler: suspend ServerConnection.(ConnectResponse) -> Unit
): ServerConnection {
    return ServerConnection(
        rootScope,
        appState.serverHost,
        gameId.webSocketUrl,
        connectRequest,
        appStrings,
        appState::showErrorMessage,
        appState::log,
        addExitAppListener
    ) {
        connect(handler)
    }
}

fun startGame(
    playerName: PlayerName,
    playerColor: PlayerColor,
    carsCount: Int,
    calculateScoresInProcess: Boolean,
    appState: AppState,
    appStrings: AppStrings,
    retriesCount: Int = 0
) {
    connectToServer(
        appState,
        appStrings,
        GameId.random(),
        ConnectRequest.Start(playerName, playerColor, appState.map, carsCount, calculateScoresInProcess)
    ) { connectResponse ->
        when (connectResponse) {
            is PlayerConnected ->
                runAsPlayer(connectResponse, appState, appStrings)

            is Failure -> {
                if (connectResponse is Failure.GameIdTaken && retriesCount < MAX_CONNECT_RETRIES_COUNT)
                    startGame(
                        playerName,
                        playerColor,
                        carsCount,
                        calculateScoresInProcess,
                        appState,
                        appStrings,
                        retriesCount + 1
                    )
                else {
                    appState.showErrorMessage(connectResponse.getErrorMessage(appStrings))
                }
            }

            else -> {}
        }
    }
}

fun joinGame(
    gameId: GameId,
    request: ConnectRequest,
    appState: AppState,
    appStrings: AppStrings
) {
    connectToServer(appState, appStrings, gameId, request) { connectResponse ->
        when (connectResponse) {
            is PlayerConnected ->
                runAsPlayer(connectResponse, appState, appStrings)

            is Failure -> {
                appState.showErrorMessage(connectResponse.getErrorMessage(appStrings))
            }

            else -> {}
        }
    }
}

fun joinAsObserver(gameId: GameId, appState: AppState, appStrings: AppStrings) {
    connectToServer(appState, appStrings, gameId, ConnectRequest.Observe) { connectResponse ->
        when (connectResponse) {
            is ObserverConnected ->
                runAsObserver(connectResponse, appState, appStrings)

            is Failure -> {
                appState.showErrorMessage(connectResponse.getErrorMessage(appStrings))
            }

            is PlayerConnected ->
                error("Unexpected PlayerConnected as response to join observer request")
        }
    }
}

fun Failure.getErrorMessage(str: AppStrings) =
    when (this) {
        is Failure.GameIdTaken -> str.gameIdTaken
        is Failure.NoSuchGame -> str.noSuchGame
        is Failure.PlayerNameTaken -> str.playerNameTaken
        is Failure.PlayerColorTaken -> str.playerColorTaken
        is Failure.CannotConnect -> str.cannotConnect
        // an exceptional situation, should never occur by design
        is Failure.NoSuchPlayer -> "A player with this name haven't joined this game"
    }

private fun ServerConnection.runAsPlayer(
    connectResponse: PlayerConnected,
    appState: AppState,
    str: AppStrings
) {
    addExitAppListener { requests.trySend(LeaveGameRequest).isSuccess }
    connectResponse.apply {
        runGame<Response>(map, appState, str, Response.GameState(state)) { msg ->
            processResponse(id, msg, appState)
        }
    }
}

private fun ServerConnection.runAsObserver(
    connectResponse: ObserverConnected,
    appState: AppState,
    str: AppStrings
) {
    connectResponse.apply {
        runGame(map, appState, str, state) { gameState ->
            processGameStateForObserver(id, gameState, appState)
        }
    }
}

private inline fun <reified T> ServerConnection.runGame(
    gameMap: GameMap,
    appState: AppState,
    str: AppStrings,
    initialGameState: T,
    crossinline processResponse: (T) -> Unit
) {
    connection = this
    appState.initMap(gameMap)
    processResponse(initialGameState)
    run<T>(
        requests = requests,
        onConnectionStateChange = { onConnectionStateChanged(it, appState, str) },
        onServerResponse = { processResponse(it) }
    )
}

private fun processResponse(gameId: GameId, msg: Response, appState: AppState) = when (msg) {

    is Response.ErrorMessage -> appState.showErrorMessage(msg.text)

    is Response.ChatMessage -> appState.appendChatMessage(msg)

    is Response.GameState -> processGameState(gameId, appState, msg.state, msg.action)

    is Response.GameEnd -> {
        appState.updateScreen(Screen.GameOver(gameId, observing = false, msg.players))
        msg.action?.let {
            appState.appendChatMessage(it.chatMessage(appState.map, appState.locale))
        }
    }
}

private fun processGameState(gameId: GameId, appState: AppState, newGameState: GameStateView, gameAction: PlayerAction?) {
    with(appState.screen) {
        when (this) {

            is Screen.Welcome ->
                appState.updateScreen(
                    if (newGameState.players.size == 1)
                        Screen.ShowGameId(gameId, newGameState)
                    else
                        Screen.GameInProgress(
                            gameId,
                            newGameState,
                            PlayerState.initial(appState.map, newGameState)
                        )
                )

            is Screen.ShowGameId -> {
                appState.updateScreen(withGameState(newGameState))
                gameAction?.let {
                    appState.appendChatMessage(it.chatMessage(appState.map, appState.locale))
                }
            }

            is Screen.GameInProgress -> {
                if (!this.gameState.myTurn && newGameState.myTurn) {
                    appState.notifyOnYourTurn()
                }
                val newPlayerState =
                    if (playerState is PlayerState.ChoosingTickets && newGameState.myPendingTicketsChoice != null) playerState
                    else PlayerState.initial(appState.map, newGameState)
                appState.updateScreen(copy(gameState = newGameState, playerState = newPlayerState))
                gameAction?.let {
                    appState.appendChatMessage(it.chatMessage(appState.map, appState.locale))
                }
            }

            else -> {
            }
        }
    }
}

private fun processGameStateForObserver(gameId: GameId, gameState: GameStateForObserver, appState: AppState) {
    appState.updateScreen(
        if (gameState.gameEnded)
            Screen.GameOver(gameId, observing = true, gameState.players.zip(gameState.tickets))
        else
            Screen.ObserveGameInProgress(gameState)
    )
    gameState.action?.let {
        appState.appendChatMessage(it.chatMessage(appState.map, appState.locale))
    }
}

fun reconnect() {
    rootScope.launch {
        connection?.reconnect()
    }
}

private fun onConnectionStateChanged(newState: ConnectionState, appState: AppState, str: AppStrings) {
    appState.setConnectionState(
        newState,
        errorMessage = when (newState) {
            CannotConnect -> str.cannotConnect
            Reconnecting -> str.reconnecting
            else -> null
        }
    )
}

val ConnectionState.showErrorMessage
    get() =
        this == Reconnecting || this == CannotConnect || this == CannotJoinGame

