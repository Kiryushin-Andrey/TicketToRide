package ticketToRide

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import ticketToRide.serialization.json
import java.time.Duration

private val logger = KotlinLogging.logger("Server")

fun Application.webSocketGameTransport(
    path: String,
    rootScope: CoroutineScope,
    redis: RedisStorage?
) {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(10)
        contentConverter = KotlinxWebsocketSerializationConverter(json)
    }
    routing {
        webSocket(path) {

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

            // initial handshake
            when (val outcome = establishConnection(gameId, rootScope, redis)) {

                // if handshake resulted in game launch,
                // then start consuming incoming requests from the connected client as a flow
                // flow completion means the underlying websocket connection has been closed
                is ConnectionOutcome.PlayerConnected -> outcome.apply {
                    incoming.consumeAsFlow().collect { msg ->
                        kotlin.runCatching { converter!!.deserialize<Request>(msg) }.fold(
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
                    // suspend until connection is closed
                    incoming.consumeAsFlow().collect {}
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
private suspend fun WebSocketServerSession.establishConnection(
    gameId: GameId,
    rootScope: CoroutineScope,
    redis: RedisStorage?
): ConnectionOutcome {
    val outcome = when (val req = converter!!.deserialize<ConnectRequest>(incoming.receive())) {
        is ConnectRequest.Start ->
            if (!gameExists(gameId, redis)) {
                val gameMap = when (val map = req.map) {
                    is ConnectRequest.StartGameMap.Custom ->
                        map.map
                    is ConnectRequest.StartGameMap.BuiltIn ->
                        loadBuiltInMap(map.path.joinToString("/"))
                }
                val initialState = GameState.initial(gameId, req.playerName.value, req.carsCount, req.calculateScoresInProcess, gameMap)
                val game = Game.start(rootScope, initialState, gameMap, redis)
                games[gameId] = game
                redis?.saveMap(gameId, gameMap)
                val conn = ClientConnection.Player(req.playerName, this)
                game.joinPlayer(conn, req.playerColor)
            } else
                ConnectionOutcome.Failure(ConnectResponse.Failure.GameIdTaken)

        is ConnectRequest.Join ->
            loadGame(gameId, redis) {
                val conn = ClientConnection.Player(req.playerName, this)
                it.joinPlayer(conn, req.playerColor)
            }

        is ConnectRequest.Reconnect ->
            loadGame(gameId, redis) {
                it.reconnectPlayer(ClientConnection.Player(req.playerName, this))
            }

        is ConnectRequest.Observe ->
            loadGame(gameId, redis) {
                val conn = ClientConnection.Observer(this)
                it.joinObserver(conn)
                val response = ConnectResponse.ObserverConnected(gameId, it.map, it.currentStateForObservers)
                ConnectionOutcome.ObserverConnected(it, conn, response)
            }
    }

    sendSerialized(outcome.response)

    return outcome
}

sealed class ClientConnection(val webSocket: WebSocketServerSession) {
    suspend inline fun <reified T> send(msg: T) =
        webSocket.sendSerialized(msg)

    suspend fun isConnected() = try {
        webSocket.send(Response.Pong); true
    } catch (e: CancellationException) {
        false
    }

    class Player(val name: PlayerName, webSocket: WebSocketServerSession) : ClientConnection(webSocket) {
        override fun toString() = name.value
    }

    class Observer(webSocket: WebSocketServerSession) : ClientConnection(webSocket) {
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

typealias PlayerConnection = ClientConnection.Player
typealias ObserverConnection = ClientConnection.Observer
