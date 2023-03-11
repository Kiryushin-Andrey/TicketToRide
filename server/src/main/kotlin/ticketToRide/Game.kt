package ticketToRide

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import mu.KotlinLogging

data class GameFlowValue(val state: GameState, val responses: List<SendResponse>)

//@FlowPreview
class Game private constructor(
    initialState: GameState,
    val map: GameMap,
    private val redis: RedisStorage?
) {

    companion object {

        fun start(
            scope: CoroutineScope,
            state: GameState,
            map: GameMap,
            redis: RedisStorage?
        ): Game {
            val game = Game(state, map, redis)
            scope.launch { game.runRequestProcessingLoop() }
            return game
        }
    }

    val id = initialState.id
    val currentState get() = stateFlow.value
    val currentStateForObservers get() = currentState.forObservers(null)
    val hasNoParticipants get() = players.isEmpty() && observers.isEmpty()

    private val requestsQueue = Channel<Pair<PlayerConnection, Request>>(Channel.BUFFERED)
    private val players = mutableMapOf<PlayerName, PlayerConnection>()
    private val observers = mutableListOf<ClientConnection>()
    private val logger = KotlinLogging.logger(id.toString())
    private val stateFlow = MutableStateFlow(initialState)

    suspend fun joinPlayer(conn: PlayerConnection, color: PlayerColor): ConnectionOutcome {
        val playerName = conn.name
        if (currentState.players.any { it.name == playerName }) {
            logger.info { "$playerName tried to join more than once" }
            return ConnectionOutcome.Failure(ConnectResponse.Failure.PlayerNameTaken)
        }

        if (currentState.players.any { it.color == color })
            return ConnectionOutcome.Failure(ConnectResponse.Failure.PlayerColorTaken)

        // wait for the join request to be processed by the game before returning control
        // and sending back connection handshake response to the client
        // this is necessary to send back the actual state of the game as part of handshake response
        val playerJoined = Job()
        requestsQueue.send(conn to JoinPlayer(color))
        requestsQueue.send(conn to Callback(playerJoined))

        // it is important to add new connection to players map after processing the join player request
        // otherwise we might try to send notifications to the new player before adding him to the game state
        playerJoined.join()
        players[playerName] = conn
        logger.info { "$playerName joined" }

        val response = ConnectResponse.PlayerConnected(id, map, currentState.toPlayerView(playerName))
        return ConnectionOutcome.PlayerConnected(this, conn, response)
    }

    suspend fun reconnectPlayer(conn: PlayerConnection): ConnectionOutcome {
        val playerName = conn.name
        if (!currentState.players.any { it.name == playerName })
            return ConnectionOutcome.Failure(ConnectResponse.Failure.NoSuchPlayer)

        players[playerName]?.let {
            if (it.isConnected()) {
                logger.info { "$playerName tried to reconnect but was not disconnected" }
                return ConnectionOutcome.Failure(ConnectResponse.Failure.PlayerNameTaken)
            }
        }

        logger.info { "$playerName reconnected" }
        players[playerName] = conn

        val action = PlayerAction.JoinGame(playerName)
        sendToAllPlayersExcept(playerName) { addressee -> stateToResponse(currentState, addressee, action) }
        sendToAllObservers(currentState.forObservers(action))

        val response = ConnectResponse.PlayerConnected(id, map, currentState.toPlayerView(playerName))
        return ConnectionOutcome.PlayerConnected(this, conn, response)
    }

    fun joinObserver(conn: ClientConnection) {
        observers += conn
    }

    suspend fun process(req: Request, conn: PlayerConnection) {
        requestsQueue.send(conn to req)
    }

    fun leave(conn: ClientConnection) {
        when (conn) {
            is PlayerConnection ->
                players -= conn.name
            else ->
                observers -= conn
        }
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
            .scan(GameFlowValue(currentState, emptyList())) { (state, _), (conn, request) ->
                runCatching {
                    val fromPlayerName = conn.name
                    if (request is Callback) {
                        request.job.complete()
                        return@runCatching GameFlowValue(state, emptyList())
                    }
                    val newState = state.processRequest(request, map, fromPlayerName, ::isAway)
                    val messages = when {
                        request is ChatMessage ->
                            listOf(SendResponse.ForAll { Response.ChatMessage(fromPlayerName, request.message) })
                        newState == state ->
                            listOf(
                                SendResponse.ForPlayer(fromPlayerName, stateToResponse(newState, fromPlayerName, null))
                            )
                        else -> {
                            val action = request.toAction(fromPlayerName)
                            listOf(
                                SendResponse.ForAll { to -> stateToResponse(newState, to, action) },
                                SendResponse.ForObservers(newState.forObservers(action))
                            )
                        }
                    }
                    GameFlowValue(newState, messages)
                }.getOrElse { e ->
                    processRequestHandleError(state, request, conn.name, e)
                }
            }
            .flatMapConcat { (state, messages) ->
                stateFlow.value = state
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

    private fun isAway(playerName: PlayerName) = !players.containsKey(playerName)

    private fun GameState.toPlayerView(myName: PlayerName): GameStateView {
        val me = players.single { it.name == myName }
        return GameStateView(
            players.map { it.toPlayerView(calculateScoresInProcess, isAway(it.name)) },
            openCards,
            turn,
            endsOnPlayer != null,
            myName,
            me.cards,
            me.ticketsOnHand,
            me.ticketsForChoice
        )
    }

    private fun GameState.forObservers(action: PlayerAction?) = GameStateForObserver(
        players.map { it.toPlayerView(true, isAway(it.name)) },
        if (endsOnPlayer == turn) players.map { it.ticketsOnHand } else emptyList(),
        openCards,
        turn,
        endsOnPlayer != null,
        endsOnPlayer == turn,
        action
    )

    private fun stateToResponse(state: GameState, playerName: PlayerName, action: PlayerAction?) = with(state) {
        if (turn != endsOnPlayer)
            Response.GameState(state.toPlayerView(playerName), action)
        else
            Response.GameEnd(
                players.map { it.toPlayerView(false, isAway(it.name)) to it.ticketsOnHand },
                action
            )
    }

    private suspend fun send(playerName: PlayerName, resp: Response) {
        players[playerName]?.let { player ->
            try {
                player.send(resp, Response.serializer())
            } catch (e: CancellationException) {
                leave(player)
                process(LeaveGameRequest, player)
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

    private suspend fun sendToAllPlayersExcept(playerName: PlayerName, resp: (PlayerName) -> Response) =
        with(players.iterator()) {
            forEach {
                if (it.key != playerName) {
                    try {
                        it.value.send(resp(it.value.name), Response.serializer())
                    } catch (e: CancellationException) {
                        remove()
                        process(LeaveGameRequest, it.value)
                    }
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
}