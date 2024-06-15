package ticketToRide

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filter
import kotlinx.serialization.SerializationStrategy
import mu.KotlinLogging

private val logger = KotlinLogging.logger("Server")

fun Application.webSocketGameTransport(
    path: String,
    rootScope: CoroutineScope,
    formatter: Formatter,
    redis: RedisStorage?
) {
    install(WebSockets)
    routing {
        webSocket(path, formatter.type.name) {

            val gameId = call.parameters["id"]?.let { GameId(it) }
                ?: throw Error("No game id specified for WebSocket connection")

            // delay for 5 minutes, then end the game if no one came back
            suspend fun endIfNoOneLeft(game: Game) {
                if (game.hasNoParticipants) {
                    val delayMinutes = 5L
                    logger.info { "No one left in game ${game.id}. Waiting for $delayMinutes minutes" }
                    delay(1000 * 60 * delayMinutes)
                    if (game.hasNoParticipants) {
                        logger.info { "No one left in game ${game.id}, dropping game from server" }
                        games.remove(game.id)
                    }
                }
            }

            // handle ping requests with pong responses
            suspend fun WebSocketSession.handlePing(msg: Frame): Boolean {
                return if ((msg as? Frame.Text)?.readText() != Request.Ping) true
                else {
                    send(Response.Pong); false
                }
            }

            // initial handshake
            when (val outcome = establishConnection(gameId, rootScope, formatter, redis)) {

                // if handshake resulted in game launch,
                // then start consuming incoming requests from the connected client as a flow
                // flow completion means the underlying websocket connection has been closed
                is ConnectionOutcome.PlayerConnected -> outcome.apply {
                    incoming.consumeAsFlow()
                        .filter { handlePing(it) }
                        .collect { msg ->
                            kotlin.runCatching { formatter.deserialize(msg, Request.serializer()) }.fold(
                                onSuccess = { req ->
                                    game.process(req, connection)
                                    if (req is LeaveGameRequest)
                                        close(CloseReason(CloseReason.Codes.NORMAL, "Exit game"))
                                },
                                onFailure = { e ->
                                    // request deserialization error - kick out the client, something is wrong with it
                                    logger.warn(e) { "Failed to deserialize request: $msg" }
                                    close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Bad request"))
                                    game.process(LeaveGameRequest, connection)
                                })
                        }
                    logger.info { "$connection disconnected" }
                    game.leave(connection)
                    endIfNoOneLeft(game)
                }

                // this is observing connection, just send out all game state updates to it, no requests expected
                is ConnectionOutcome.ObserverConnected -> outcome.apply {
                    incoming.consumeAsFlow().collect { handlePing(it) }
                    game.leave(connection)
                    endIfNoOneLeft(game)
                }

                // something went wrong in the connection handshake
                is ConnectionOutcome.Failure ->
                    close(CloseReason(CloseReason.Codes.NORMAL, "Failed to join game"))
            }
        }
    }
}

// connection handshake
private suspend fun WebSocketSession.establishConnection(
    gameId: GameId,
    rootScope: CoroutineScope,
    formatter: Formatter,
    redis: RedisStorage?
): ConnectionOutcome {
    val outcome = when (val req = formatter.deserialize(incoming.receive(), ConnectRequest.serializer())) {
        is ConnectRequest.Start ->
            if (!gameExists(gameId, redis)) {
                val gameMap = when (val map = req.map) {
                    is ConnectRequest.StartGameMap.Custom ->
                        map.map
                    is ConnectRequest.StartGameMap.BuiltIn ->
                        loadBuiltInMap(map.path.joinToString("/"))
                }
                val initialState = GameState.initial(gameId, req.carsCount, req.calculateScoresInProcess, gameMap)
                val game = Game.start(rootScope, initialState, gameMap, redis)
                games[gameId] = game
                redis?.saveMap(gameId, gameMap)
                val conn = Connection.Player(req.playerName, this, formatter)
                game.joinPlayer(conn, req.playerColor)
            } else
                ConnectionOutcome.Failure(ConnectResponse.Failure.GameIdTaken)

        is ConnectRequest.Join ->
            loadGame(gameId, redis) {
                val conn = Connection.Player(req.playerName, this, formatter)
                it.joinPlayer(conn, req.playerColor)
            }

        is ConnectRequest.Reconnect ->
            loadGame(gameId, redis) {
                it.reconnectPlayer(Connection.Player(req.playerName, this, formatter))
            }

        is ConnectRequest.Observe ->
            loadGame(gameId, redis) {
                val conn = Connection.Observer(this, formatter)
                it.joinObserver(conn)
                val response = ConnectResponse.ObserverConnected(gameId, it.map, it.currentStateForObservers)
                ConnectionOutcome.ObserverConnected(it, conn, response)
            }
    }

    formatter.send(this, outcome.response, ConnectResponse.serializer())

    return outcome
}

private sealed class Connection(
    private val webSocket: WebSocketSession,
    private val formatter: Formatter
) : ClientConnection {
    override suspend fun <T> send(msg: T, serializer: SerializationStrategy<T>) =
        formatter.send(webSocket, msg, serializer)

    override suspend fun isConnected() = try {
        webSocket.send(Response.Pong); true
    } catch (e: CancellationException) {
        false
    }

    class Player(override val name: PlayerName, webSocket: WebSocketSession, formatter: Formatter) :
        Connection(webSocket, formatter),
        PlayerConnection {
        override fun toString() = name.value
    }

    class Observer(webSocket: WebSocketSession, formatter: Formatter) :
        Connection(webSocket, formatter),
        ObserverConnection {
        override fun toString() = "anonymous observer"
    }
}

private fun loadBuiltInMap(path: String): GameMap {
    return Game::class.java.classLoader.getResourceAsStream("maps/${path.trimStart('/')}.map")?.use { stream ->
        when (val result = GameMap.parse(String(stream.readAllBytes()))) {
            is Try.Success ->
                result.value
            is Try.Error ->
                error("Could not parse built-in map at path \"$path\"")
        }
    } ?: throw Error("Could not load built-in map by path \"$path\"")
}