package ticketToRide

import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import mu.KotlinLogging

data class GameFlowValue(val state: GameState, val responses: List<SendResponse>)

@FlowPreview
class Game private constructor(
    val id: GameId,
    initialCarsCount: Int,
    private val map: GameMap,
    private val redis: RedisCredentials?,
    private val onAllPlayersLeft: (Game) -> Unit
) {

    companion object {

        fun start(
            scope: CoroutineScope,
            id: GameId,
            initialCarsCount: Int,
            map: GameMap,
            redis: RedisCredentials?,
            onAllPlayersLeft: (Game) -> Unit
        ): Game {
            val game = Game(id, initialCarsCount, map, redis, onAllPlayersLeft)
            scope.launch { game.runRequestProcessingLoop() }
            return game
        }

        fun restore(
            scope: CoroutineScope,
            state: GameState,
            map: GameMap,
            redis: RedisCredentials?,
            onAllPlayersLeft: (Game) -> Unit
        ): Game {
            val game = Game(state.id, state.initialCarsCount, map, redis, onAllPlayersLeft)
            game.state = state.restored()
            scope.launch { game.runRequestProcessingLoop() }
            return game
        }
    }

    private val logger = KotlinLogging.logger("Game-$id")

    private val requestsQueue = Channel<RequestQueueItem>(Channel.BUFFERED)

    private val players = mutableMapOf<PlayerName, ClientConnection.Player>()
    private val observers = mutableListOf<ClientConnection.Observer>()

    var state = GameState.initial(id, initialCarsCount, map)
        private set

    private fun processRequestHandleError(
        state: GameState,
        req: Any,
        playerName: PlayerName,
        e: Throwable
    ): GameFlowValue {
        if (e is InvalidActionError) logger.info { "${e.message} (request $req from ${playerName.value})" }
        else logger.warn(e) { "Error while processing request $req from ${playerName.value}" }
        val message =
            if (e is InvalidActionError) e.message!! else "Server error - ${e.message}"
        return GameFlowValue(state, listOf(SendResponse.ForPlayer(playerName, Response.ErrorMessage(message))))
    }

    private suspend fun runRequestProcessingLoop() =
        supervisorScope {
            logger.info { "Request loop for game $id  started" }
            requestsQueue.consumeAsFlow()
                .scan(GameFlowValue(state, emptyList())) { (state, _), req ->
                    runCatching {
                        when (req) {
                            is RequestQueueItem.Connect ->
                                state.connectPlayer(req.conn.name, req.playerColor, map)

                            is RequestQueueItem.Reconnect ->
                                state.reconnectPlayer(req.conn.name, map)

                            is RequestQueueItem.PlayerAction ->
                                state.processRequest(req.request, map, req.conn.name)
                        }
                    }.getOrElse { e ->
                        processRequestHandleError(state, req, req.conn.name, e)
                    }
                }
                .flatMapConcat { (state, messages) ->
                    this@Game.state = state
                    redis?.apply { launch { saveGame(state) } }
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

    suspend fun joinPlayer(name: PlayerName, color: PlayerColor, ws: WebSocketSession): ConnectionOutcome {
        if (state.players.any { it.name == name }) {
            logger.info { "Player ${name.value} tried to join more than once" }
            return ConnectionOutcome.Failure(ConnectResponse.Failure.PlayerNameTaken)
        }

        if (state.players.any { it.color == color })
            return ConnectionOutcome.Failure(ConnectResponse.Failure.PlayerColorTaken)

        logger.info { "Player ${name.value} joined" }
        val conn = ClientConnection.Player(name, ws)
        players[name] = conn
        requestsQueue.send(RequestQueueItem.Connect(color, conn))
        return ConnectionOutcome.Success(this, conn)
    }

    suspend fun reconnectPlayer(name: PlayerName, ws: WebSocketSession): ConnectionOutcome =
        players[name]?.let { player ->
            ping(player)
            if (players.containsKey(name)) {
                logger.info { "Player ${name.value} tried to reconnect but was not disconnected" }
                return ConnectionOutcome.Failure(ConnectResponse.Failure.PlayerNameTaken)
            }

            logger.info { "Player ${name.value} reconnected" }
            val conn = ClientConnection.Player(name, ws)
            players[name] = conn
            requestsQueue.send(RequestQueueItem.Reconnect(conn))
            return ConnectionOutcome.Success(this, conn)
        } ?: ConnectionOutcome.Failure(ConnectResponse.Failure.NoSuchPlayer)

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

    private suspend fun sendToAllObservers(resp: GameStateForObservers) = with(observers.iterator()) {
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

    private sealed class RequestQueueItem(val conn: ClientConnection.Player) {
        class Connect(val playerColor: PlayerColor, conn: ClientConnection.Player) : RequestQueueItem(conn)
        class Reconnect(conn: ClientConnection.Player) : RequestQueueItem(conn)
        class PlayerAction(val request: GameRequest, conn: ClientConnection.Player) : RequestQueueItem(conn)
    }
}