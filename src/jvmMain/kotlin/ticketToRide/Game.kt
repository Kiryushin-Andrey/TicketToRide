package ticketToRide

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import mu.KotlinLogging

@FlowPreview
class Game(
    val id: GameId,
    private val initialCarsCount: Int,
    private val redis: RedisCredentials?,
    private val onAllPlayersLeft: (Game) -> Unit
) {

    private val logger = KotlinLogging.logger("Game-$id")

    private val requestsQueue = Channel<RequestQueueItem>(Channel.BUFFERED)

    private val players = mutableMapOf<PlayerName, ClientConnection.Player>()
    private val observers = mutableListOf<ClientConnection.Observer>()

    val playerNames get() = players.keys.map { it.value }

    suspend fun start(firstPlayer: ClientConnection.Player, map: GameMap) {
        players[firstPlayer.name] = firstPlayer
        logger.info { "New game started by ${firstPlayer.name.value}" }
        requestsQueue.send(RequestQueueItem.Connect(firstPlayer.name, firstPlayer))
        runRequestProcessingLoop(GameState.initial(id, initialCarsCount, map), map)
    }

    suspend fun restore(conn: ClientConnection, state: GameState, map: GameMap) {
        logger.info { "Game restored from Redis by $conn" }
        when (conn) {
            is ClientConnection.Player -> {
                players[conn.name] = conn
                requestsQueue.send(RequestQueueItem.Connect(conn.name, conn))
            }
            is ClientConnection.Observer -> {
                observers += conn
            }
        }
        runRequestProcessingLoop(state.restored(), map)
    }

    private fun processRequestHandleError(state: GameState, req: Any, playerName: PlayerName, e: Throwable) = let {
        val message =
            if (e is InvalidActionError) e.message!! else "Server error - ${e.message}"
        state to listOf(SendResponse.ForPlayer(playerName, Response.ErrorMessage(message)))
    }.also {
        if (e is InvalidActionError) logger.info { "${e.message} (request $req from ${playerName.value})" }
        else logger.warn(e) { "Error while processing request $req from ${playerName.value}" }
    }

    private suspend fun runRequestProcessingLoop(
        initialState: GameState,
        map: GameMap,
        initialMessages: List<SendResponse> = emptyList()
    ) =
        supervisorScope {
            requestsQueue.consumeAsFlow()
                .scan(initialState to initialMessages) { (state, _), req ->
                    when (req) {

                        is RequestQueueItem.DumpState -> {
                            req.deferred.complete(state)
                            state to emptyList()
                        }

                        is RequestQueueItem.Connect ->
                            runCatching {
                                state.connectPlayer(req.playerName, map)
                            }.getOrElse { e ->
                                processRequestHandleError(state, req, req.conn.name, e)
                            }

                        is RequestQueueItem.PlayerAction ->
                            runCatching {
                                state.processRequest(req.request, map, req.conn.name)
                            }.getOrElse { e ->
                                processRequestHandleError(state, req, req.conn.name, e)
                            }
                    }
                }
                .flatMapConcat { (state, messages) ->
                    redis?.apply {
                        launch { saveGame(state) }
                    }
                    messages.asFlow()
                }
                .map { message ->
                    when (message) {
                        is SendResponse.ForAll -> sendToAllPlayers(message.resp)
                        is SendResponse.ForObservers -> sendToAllObservers(message.resp)
                        is SendResponse.ForPlayer -> send(message.to, message.resp)
                    }
                }
                .catch {
                    logger.warn(it) { "Error while sending out response to connected players" }
                }
                .collect()
        }

    suspend fun getState() = with(CompletableDeferred<GameState>()) {
        requestsQueue.send(RequestQueueItem.DumpState(this))
        await()
    }

    suspend fun joinPlayer(playerName: PlayerName, conn: ClientConnection.Player): Boolean {
        players[playerName]?.let { ping(it) }
        if (playerNames.contains(playerName.value)) {
            logger.info { "Player ${conn.name.value} tried to join again" }
            return false
        }

        logger.info { "Player ${conn.name.value} joined" }
        players[playerName] = conn
        requestsQueue.send(RequestQueueItem.Connect(playerName, conn))
        return true
    }

    fun joinObserver(conn: ClientConnection.Observer) {
        observers += conn
    }

    suspend fun leavePlayer(conn: ClientConnection) {
        logger.info { "$conn disconnected" }
        when (conn) {
            is ClientConnection.Player -> players -= conn.name
            is ClientConnection.Observer -> observers -= conn
        }
        onConnectionLost(conn)
    }

    suspend fun process(req: GameRequest, conn: ClientConnection.Player) {
        logger.info { "Received $req from ${conn.name.value}" }
        requestsQueue.send(RequestQueueItem.PlayerAction(req, conn))
    }

    private suspend fun send(playerName: PlayerName, resp: Response) {
        players[playerName]?.let { player ->
            try {
                player.send(resp, Response.serializer())
            } catch (e: CancellationException) {
                leavePlayer(player)
            }
        }
    }

    private suspend fun ping(conn: ClientConnection) {
        try {
            conn.ping()
        } catch (e: CancellationException) {
            leavePlayer(conn)
        }
    }

    suspend fun sendToAllPlayers(resp: (PlayerName) -> Response) = with(players.iterator()) {
        forEach {
            try {
                it.value.send(resp(it.value.name), Response.serializer())
            } catch (e: CancellationException) {
                remove()
                onConnectionLost(it.value)
            }
        }
    }

    suspend fun sendToAllObservers(resp: GameStateForObservers) = with(observers.iterator()) {
        forEach {
            try {
                it.send(resp, GameStateForObservers.serializer())
            } catch (e: CancellationException) {
                remove()
                onConnectionLost(it)
            }
        }
    }

    private suspend fun onConnectionLost(conn: ClientConnection) {
        if (players.isEmpty() && observers.isEmpty()) {
            requestsQueue.close()
            onAllPlayersLeft(this)
        } else if (conn is ClientConnection.Player) {
            process(LeaveGameRequest, conn)
        }
    }

    private sealed class RequestQueueItem {
        class DumpState(val deferred: CompletableDeferred<GameState>) : RequestQueueItem()
        class Connect(val playerName: PlayerName, val conn: ClientConnection.Player) : RequestQueueItem()
        class PlayerAction(val request: GameRequest, val conn: ClientConnection.Player) : RequestQueueItem()
    }
}