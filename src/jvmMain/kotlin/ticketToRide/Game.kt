package ticketToRide

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import kotlin.random.Random

@FlowPreview
class Game(private val onAllPlayersLeft: (Game) -> Unit) {

    val id = GameId(Random.nextBytes(5).joinToString("") { "%02x".format(it) })

    private val logger = KotlinLogging.logger("Game-$id")

    private val requestsQueue = Channel<RequestQueueItem>(Channel.BUFFERED)

    private val players = mutableListOf<PlayerConnection>()

    val playerNames get() = players.map { it.name.value }

    suspend fun start(firstPlayer: PlayerConnection) {
        logger.info { "New game started by ${firstPlayer.name.value}" }
        players.add(firstPlayer)
        requestsQueue.offer(RequestQueueItem.Req(JoinGameRequest(id, firstPlayer.name), firstPlayer))
        requestsQueue.consumeAsFlow()
            .scan(GameState.initial(id) to emptyList<SendResponse>()) { (state, _), req ->
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
                            val message = if (e is InvalidActionError) e.message!! else "Server error - ${e.message}"
                            state to listOf(SendResponse.ForPlayer(playerName, Response.ErrorMessage(message)))
                        }
                    }
                }
            }
            .flatMapConcat { (_, messages) -> messages.asFlow() }
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
        requestsQueue.offer(RequestQueueItem.DumpState(this))
        await()
    }

    suspend fun joinPlayer(req: JoinGameRequest, conn: PlayerConnection) {
        logger.info { "Player ${conn.name.value} joined" }
        players += conn
        requestsQueue.send(RequestQueueItem.Req(req, conn))
    }

    suspend fun leavePlayer(conn: PlayerConnection) {
        logger.info { "Player ${conn.name.value} disconnected" }
        players -= conn
        handlePlayerLeave(conn)
    }

    suspend fun process(req: GameRequest, conn: PlayerConnection) {
        logger.info { "Received $req from ${conn.name.value}" }
        requestsQueue.send(RequestQueueItem.Req(req, conn))
    }

    private suspend fun send(playerName: PlayerName, resp: Response) {
        players.find { it.name == playerName }?.let { player ->
            try {
                player.send(resp)
            } catch (e: CancellationException) {
                players.removeIf { it.name == playerName }
                handlePlayerLeave(player)
            }
        }
    }

    suspend fun sendToAll(resp: (PlayerName) -> Response) {
        with(players.iterator()) {
            forEach {
                try {
                    it.send(resp(it.name))
                } catch (e: CancellationException) {
                    remove()
                    handlePlayerLeave(it)
                }
            }
        }
    }

    private suspend fun handlePlayerLeave(player: PlayerConnection) {
        if (players.size == 0) {
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