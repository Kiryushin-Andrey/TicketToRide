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

    private val players = mutableMapOf<PlayerName, ClientConnection>()

    val playerNames get() = players.keys.map { it.value }

    suspend fun start(firstPlayer: ClientConnection, map: GameMap) {
        players[firstPlayer.name] = firstPlayer
        logger.info { "New game started by ${firstPlayer.name.value}" }
        requestsQueue.send(RequestQueueItem.Connect(firstPlayer.name, firstPlayer))
        runRequestProcessingLoop(GameState.initial(id, initialCarsCount, map), map)
    }

    suspend fun restore(byPlayer: ClientConnection, state: GameState, map: GameMap) {
        players[byPlayer.name] = byPlayer
        logger.info { "Game restored from Redis by ${byPlayer.name.value}" }
        requestsQueue.send(RequestQueueItem.Connect(byPlayer.name, byPlayer))
        runRequestProcessingLoop(state.restored(byPlayer.name), map)
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

                        is RequestQueueItem.Connect -> {
                            state.connectPlayer(req.playerName, map)
                        }

                        is RequestQueueItem.PlayerAction -> {
                            val playerName = req.conn.name
                            try {
                                state.processRequest(req.request, map, playerName)
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
                        launch { saveGame(state) }
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

    suspend fun joinPlayer(playerName: PlayerName, conn: ClientConnection): Boolean {
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

    suspend fun leavePlayer(conn: ClientConnection) {
        logger.info { "Player ${conn.name.value} disconnected" }
        players -= conn.name
        handlePlayerLeave(conn)
    }

    suspend fun process(req: GameRequest, conn: ClientConnection) {
        logger.info { "Received $req from ${conn.name.value}" }
        requestsQueue.send(RequestQueueItem.PlayerAction(req, conn))
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

    private suspend fun ping(player: ClientConnection) {
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

    private suspend fun handlePlayerLeave(player: ClientConnection) {
        if (players.isEmpty()) {
            requestsQueue.close()
            onAllPlayersLeft(this)
        } else {
            process(LeaveGameRequest, player)
        }
    }

    private sealed class RequestQueueItem {
        class DumpState(val deferred: CompletableDeferred<GameState>) : RequestQueueItem()
        class Connect(val playerName: PlayerName, val conn: ClientConnection) : RequestQueueItem()
        class PlayerAction(val request: GameRequest, val conn: ClientConnection) : RequestQueueItem()
    }
}