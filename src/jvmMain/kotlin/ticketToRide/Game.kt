package ticketToRide

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import kotlin.random.Random


@FlowPreview
class Game(
    private val initialCarsCount: Int,
    private val redis: RedisCredentials?,
    private val onAllPlayersLeft: (Game) -> Unit
) {

    val id = GameId(Random.nextBytes(5).joinToString("") { "%02x".format(it) })

    private val logger = KotlinLogging.logger("Game-$id")

    private val requestsQueue = Channel<RequestQueueItem>(Channel.BUFFERED)

    private val players = mutableMapOf<PlayerName, PlayerConnection>()

    val playerNames get() = players.keys.map { it.value }

    suspend fun start(firstPlayer: PlayerConnection) {
        players[firstPlayer.name] = firstPlayer
        logger.info { "New game started by ${firstPlayer.name.value}" }
        requestsQueue.send(RequestQueueItem.Req(JoinGameRequest(id, firstPlayer.name), firstPlayer))
        runRequestProcessingLoop(GameState.initial(id, initialCarsCount), false)
    }

    suspend fun restore(byPlayer: PlayerConnection, state: GameState) {
        players[byPlayer.name] = byPlayer
        logger.info { "Game restored from Redis by ${byPlayer.name.value}" }
        runRequestProcessingLoop(state.restored(byPlayer.name), true)
    }

    private suspend fun runRequestProcessingLoop(initialState: GameState, sendState: Boolean) =
        supervisorScope {
            val messages =
                if (sendState) listOf<SendResponse>(initialState.responseMessage(null))
                else emptyList()
            requestsQueue.consumeAsFlow()
                .scan(initialState to messages) { (state, _), req ->
                    when (req) {
                        is RequestQueueItem.DumpState -> {
                            req.deferred.complete(state)
                            state to emptyList()
                        }
                        is RequestQueueItem.Req -> {
                            val playerName = req.conn.name
                            try {
                                state.processRequest(req.request, playerName)
                            } catch (e: Throwable) {
                                if (e is InvalidActionError) logger.info { "${e.message} (request $req from ${playerName.value})" }
                                else logger.warn(e) { "Error while processing request $req from ${playerName.value}" }
                                val message =
                                    if (e is InvalidActionError) e.message!! else "Server error - ${e.message}"
                                state to listOf(SendResponse.ForPlayer(playerName, Response.ErrorMessage(message)))
                            }
                        }
                    }
                }
                .flatMapConcat { (state, messages) ->
                    redis?.apply {
                        launch { saveToRedis(state) }
                    }
                    messages.asFlow()
                }
                .map { message ->
                    when (message) {
                        is SendResponse.ForAll -> sendToAll(message.resp)
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

    suspend fun joinPlayer(req: JoinGameRequest, conn: PlayerConnection): Boolean {
        players[req.playerName]?.let { ping(it) }
        if (playerNames.contains(req.playerName.value)) {
            logger.info { "Player ${conn.name.value} tried to join again" }
            return false
        }

        logger.info { "Player ${conn.name.value} joined" }
        players[req.playerName] = conn
        requestsQueue.send(RequestQueueItem.Req(req, conn))
        return true
    }

    suspend fun leavePlayer(conn: PlayerConnection) {
        logger.info { "Player ${conn.name.value} disconnected" }
        players -= conn.name
        handlePlayerLeave(conn)
    }

    suspend fun process(req: GameRequest, conn: PlayerConnection) {
        logger.info { "Received $req from ${conn.name.value}" }
        requestsQueue.send(RequestQueueItem.Req(req, conn))
    }

    private suspend fun send(playerName: PlayerName, resp: Response) {
        players[playerName]?.let { player ->
            try {
                player.send(resp)
            } catch (e: CancellationException) {
                players -= playerName
                handlePlayerLeave(player)
            }
        }
    }

    private suspend fun ping(player: PlayerConnection) {
        try {
            player.ping()
        } catch (e: CancellationException) {
            players -= player.name
            handlePlayerLeave(player)
        }
    }

    suspend fun sendToAll(resp: (PlayerName) -> Response) {
        with(players.iterator()) {
            forEach {
                try {
                    it.value.send(resp(it.key))
                } catch (e: CancellationException) {
                    remove()
                    handlePlayerLeave(it.value)
                }
            }
        }
    }

    private suspend fun handlePlayerLeave(player: PlayerConnection) {
        if (players.isEmpty()) {
            requestsQueue.close()
            onAllPlayersLeft(this)
        } else {
            process(LeaveGameRequest, player)
        }
    }

    private sealed class RequestQueueItem {
        class DumpState(val deferred: CompletableDeferred<GameState>) : RequestQueueItem()
        class Req(val request: GameRequest, val conn: PlayerConnection) : RequestQueueItem()
    }
}