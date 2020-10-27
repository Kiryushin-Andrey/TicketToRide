package ticketToRide

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import mu.KotlinLogging

data class GameFlowValue(val state: GameState, val responses: List<SendResponse>)

@FlowPreview
class Game private constructor(
    val id: GameId,
    initialCarsCount: Int,
    val calculateScoresInProcess: Boolean,
    val map: GameMap,
    private val redis: RedisStorage?
) {

    companion object {

        fun start(
            scope: CoroutineScope,
            id: GameId,
            initialCarsCount: Int,
            calculateScoresInProcess: Boolean,
            map: GameMap,
            redis: RedisStorage?
        ): Game {
            val game = Game(id, initialCarsCount, calculateScoresInProcess, map, redis)
            scope.launch { game.runRequestProcessingLoop() }
            return game
        }

        fun restore(
            scope: CoroutineScope,
            state: GameState,
            map: GameMap,
            redis: RedisStorage?
        ): Game {
            val game = Game(state.id, state.initialCarsCount, state.calculateScoresInProcess, map, redis)
            game.state = state.restored()
            scope.launch { game.runRequestProcessingLoop() }
            return game
        }
    }

    private val logger = KotlinLogging.logger(id.toString())

    private val requestsQueue = Channel<RequestQueueItem>(Channel.BUFFERED)

    private val players = mutableMapOf<PlayerName, PlayerConnection>()
    private val observers = mutableListOf<ClientConnection>()

    var state = GameState.initial(id, initialCarsCount, calculateScoresInProcess, map)
        private set

    suspend fun joinPlayer(conn: PlayerConnection, color: PlayerColor): ConnectionOutcome {
        if (state.players.any { it.name == conn.name }) {
            logger.info { "${conn.name} tried to join more than once" }
            return ConnectionOutcome.Failure(ConnectResponse.Failure.PlayerNameTaken)
        }

        if (state.players.any { it.color == color })
            return ConnectionOutcome.Failure(ConnectResponse.Failure.PlayerColorTaken)

        logger.info { "${conn.name} joined" }
        players[conn.name] = conn
        requestsQueue.send(RequestQueueItem.Connect(color, conn))
        return ConnectionOutcome.Success(this, conn)
    }

    suspend fun reconnectPlayer(conn: PlayerConnection): ConnectionOutcome {
        val name = conn.name
        if (!state.players.any { it.name == name })
            return ConnectionOutcome.Failure(ConnectResponse.Failure.NoSuchPlayer)

        players[name]?.let {
            if (it.isConnected()) {
                logger.info { "Player ${name.value} tried to reconnect but was not disconnected" }
                return ConnectionOutcome.Failure(ConnectResponse.Failure.PlayerNameTaken)
            }
        }

        logger.info { "Player ${name.value} reconnected" }
        players[name] = conn
        requestsQueue.send(RequestQueueItem.Reconnect(conn))
        return ConnectionOutcome.Success(this, conn)
    }

    fun joinObserver(conn: ClientConnection) {
        observers += conn
    }

    val hasNoParticipants = players.isEmpty() && observers.isEmpty()

    suspend fun process(req: Request, conn: PlayerConnection) {
        requestsQueue.send(RequestQueueItem.PlayerAction(req, conn))
        if (req is LeaveGameRequest)
            players -= conn.name
    }

    private fun processRequestHandleError(
        state: GameState,
        req: Any,
        playerName: PlayerName,
        e: Throwable
    ): GameFlowValue {
        if (e is InvalidActionError)
            logger.info { "${e.message} (request $req from ${playerName.value})" }
        else
            logger.warn(e) { "Error while processing request $req from ${playerName.value}" }
        val message = if (e is InvalidActionError) e.message!! else "Server error - ${e.message}"
        return GameFlowValue(state, listOf(SendResponse.ForPlayer(playerName, Response.ErrorMessage(message))))
    }

    private suspend fun runRequestProcessingLoop() = supervisorScope {
        logger.info { "Request loop for game $id  started" }
        requestsQueue.consumeAsFlow()
            .scan(GameFlowValue(state, emptyList())) { (state, _), req ->
                runCatching {
                    when (req) {
                        is RequestQueueItem.Connect ->
                            state.connectPlayer(req.conn.name, req.playerColor, map)

                        is RequestQueueItem.Reconnect ->
                            state.reconnectPlayer(req.conn.name)

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

    private suspend fun leave(conn: ClientConnection) {
        when (conn) {
            is PlayerConnection -> {
                players -= conn.name
                process(LeaveGameRequest, conn)
            }
            else ->
                observers -= conn
        }
    }

    private suspend fun send(playerName: PlayerName, resp: Response) {
        players[playerName]?.let { player ->
            try {
                player.send(resp, Response.serializer())
            } catch (e: CancellationException) {
                leave(player)
            }
        }
    }

    private suspend fun sendToAllPlayers(resp: (PlayerName) -> Response) = with(players.iterator()) {
        forEach {
            try {
                it.value.send(resp(it.value.name), Response.serializer())
            } catch (e: CancellationException) {
                remove()
                process(LeaveGameRequest, it.value)
            }
        }
    }

    private suspend fun sendToAllObservers(resp: GameStateForObserver) = with(observers.iterator()) {
        forEach {
            try {
                it.send(resp, GameStateForObserver.serializer())
            } catch (e: CancellationException) {
                remove()
            }
        }
    }

    private sealed class RequestQueueItem(val conn: PlayerConnection) {
        class Connect(val playerColor: PlayerColor, conn: PlayerConnection) : RequestQueueItem(conn)
        class Reconnect(conn: PlayerConnection) : RequestQueueItem(conn)
        class PlayerAction(val request: Request, conn: PlayerConnection) : RequestQueueItem(conn)
    }
}