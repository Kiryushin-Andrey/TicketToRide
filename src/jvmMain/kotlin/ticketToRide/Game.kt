package ticketToRide

import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import core.ConnectionOutcome

data class GameFlowValue(val state: GameState, val responses: List<SendResponse>)

@FlowPreview
class Game private constructor(
    val id: GameId,
    initialCarsCount: Int,
    calculateScoresInProcess: Boolean,
    private val map: GameMap,
    private val redis: RedisStorage?,
    private val onAllPlayersLeft: (Game) -> Unit
) {

    companion object {

        fun start(
            scope: CoroutineScope,
            id: GameId,
            initialCarsCount: Int,
            calculateScoresInProcess: Boolean,
            map: GameMap,
            redis: RedisStorage?,
            onAllPlayersLeft: (Game) -> Unit
        ): Game {
            val game = Game(id, initialCarsCount, calculateScoresInProcess, map, redis, onAllPlayersLeft)
            scope.launch { game.runRequestProcessingLoop() }
            return game
        }

        fun restore(
            scope: CoroutineScope,
            state: GameState,
            map: GameMap,
            redis: RedisStorage?,
            onAllPlayersLeft: (Game) -> Unit
        ): Game {
            val game = Game(state.id, state.initialCarsCount, state.calculateScoresInProcess, map, redis, onAllPlayersLeft)
            game.state = state.restored()
            scope.launch { game.runRequestProcessingLoop() }
            return game
        }
    }

    private val logger = KotlinLogging.logger("Game-$id")

    private val requestsQueue = Channel<RequestQueueItem>(Channel.BUFFERED)

    private val players = mutableMapOf<PlayerName, PlayerConnection>()
    private val observers = mutableListOf<ObserverConnection>()

    var state = GameState.initial(id, initialCarsCount, calculateScoresInProcess, map)
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

    suspend fun joinPlayer(name: PlayerName, color: PlayerColor, ws: WebSocketSession): ConnectionOutcome<PlayerConnection, CannotJoinReason> {
        if (state.players.any { it.name == name }) {
            logger.info { "Player ${name.value} tried to join more than once" }
            return cannotJoin(CannotJoinReason.PlayerNameTaken)
        }

        if (state.players.any { it.color == color })
            return cannotJoin(CannotJoinReason.PlayerColorTaken)

        logger.info { "Player ${name.value} joined" }
        val conn = PlayerConnection(name, this, ws)
        players[name] = conn
        requestsQueue.send(RequestQueueItem.Connect(color, conn))
        return ConnectionOutcome.Success(conn)
    }

    suspend fun reconnectPlayer(name: PlayerName, ws: WebSocketSession): ConnectionOutcome<PlayerConnection, CannotJoinReason> =
        players[name]?.let { player ->
            ping(player)
            if (players.containsKey(name)) {
                logger.info { "Player ${name.value} tried to reconnect but was not disconnected" }
                return cannotJoin(CannotJoinReason.PlayerNameTaken)
            }

            logger.info { "Player ${name.value} reconnected" }
            val conn = PlayerConnection(name, this, ws)
            players[name] = conn
            requestsQueue.send(RequestQueueItem.Reconnect(conn))
            return ConnectionOutcome.Success(conn)
        } ?: cannotJoin(CannotJoinReason.NoSuchPlayer)

    fun joinObserver(conn: ObserverConnection) {
        observers += conn
    }

    suspend fun leavePlayer(conn: PlayerConnection) {
        logger.info { "$conn disconnected" }
        players -= conn.name
        onConnectionLost(conn)
    }

    suspend fun process(req: GameRequest, conn: PlayerConnection) {
        logger.info { "Received $req from ${conn.name.value}" }
        requestsQueue.send(RequestQueueItem.PlayerAction(req, conn))
    }

    private suspend fun send(playerName: PlayerName, resp: Response) {
        players[playerName]?.let { player ->
            try {
                player.send(resp)
            } catch (e: CancellationException) {
                leavePlayer(player)
            }
        }
    }

    private suspend fun ping(conn: PlayerConnection) {
        try {
            conn.ping()
        } catch (e: CancellationException) {
            leavePlayer(conn)
        }
    }

    suspend fun sendToAllPlayers(resp: (PlayerName) -> Response) = with(players.iterator()) {
        forEach {
            try {
                it.value.send(resp(it.value.name))
            } catch (e: CancellationException) {
                remove()
                onConnectionLost(it.value)
            }
        }
    }

    private suspend fun sendToAllObservers(resp: GameStateForObservers) = with(observers.iterator()) {
        forEach {
            try {
                it.send(resp)
            } catch (e: CancellationException) {
                remove()
            }
        }
    }

    private suspend fun onConnectionLost(conn: PlayerConnection) {
        if (players.isEmpty()) {
            requestsQueue.close()
            onAllPlayersLeft(this)
        } else {
            process(LeaveGameRequest, conn)
        }
    }

    private sealed class RequestQueueItem(val conn: PlayerConnection) {
        class Connect(val playerColor: PlayerColor, conn: PlayerConnection) : RequestQueueItem(conn)
        class Reconnect(conn: PlayerConnection) : RequestQueueItem(conn)
        class PlayerAction(val request: GameRequest, conn: PlayerConnection) : RequestQueueItem(conn)
    }
}