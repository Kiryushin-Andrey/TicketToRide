package ticketToRide

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filter
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import mu.KotlinLogging

private val logger = KotlinLogging.logger("Server")

fun Application.webSocketGameTransport(path: String, rootScope: CoroutineScope, redis: RedisStorage?) {
    install(WebSockets)
    routing {
        webSocket(path) {
            val gameId = call.parameters["id"]?.let { GameId(it) }
                ?: throw Error("No game id specified for WebSocket connection")

            // delay for 5 minutes, then end the game if no one came back
            suspend fun endIfNoOneLeft(game: Game) {
                if (game.hasNoParticipants) {
                    delay(1000 * 60 * 5)
                    games.remove(game.id)
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
            when (val outcome = establishConnection(gameId, rootScope, redis)) {

                is ConnectionOutcome.Success -> {
                    val game = outcome.game
                    when (val conn = outcome.connection) {

                        is PlayerConnection -> {
                            // consume incoming requests from the connected client as a flow
                            // flow completion means the underlying websocket connection has been closed
                            incoming.consumeAsFlow()
                                .filter { handlePing(it) }
                                .collect { msg ->
                                    kotlin.runCatching { deserialize(msg, Request.serializer()) }.fold(

                                        onSuccess = { req ->
                                            game.process(req, conn)
                                            if (req is LeaveGameRequest)
                                                close(CloseReason(CloseReason.Codes.NORMAL, "Exit game"))
                                        },

                                        // request deserialization error - kick out the client, something is wrong with it
                                        onFailure = { e ->
                                            logger.warn(e) { "Failed to deserialize request: $msg" }
                                            close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Bad request"))
                                            game.process(LeaveGameRequest, conn)
                                        })
                                }
                            logger.info { "$conn disconnected" }
                            endIfNoOneLeft(game)
                        }

                        else -> {
                            // this is observing connection, just send out all game state updates to it, no requests expected
                            outcome.apply {
                                connection.send(game.state.forObservers(null), GameStateForObserver.serializer())
                                incoming.consumeAsFlow().collect { handlePing(it) }
                                endIfNoOneLeft(game)
                            }
                        }
                    }
                }

                // something went wrong in the connection handshake
                is ConnectionOutcome.Failure ->
                    close(CloseReason(CloseReason.Codes.NORMAL, "Failed to join game"))
            }
        }
    }
}

// connection handshake
private suspend fun WebSocketSession.establishConnection(gameId: GameId, rootScope: CoroutineScope, redis: RedisStorage?): ConnectionOutcome {
    val req = deserialize(incoming.receive(), ConnectRequest.serializer())

    val outcome = when (req) {
        is ConnectRequest.Start ->
            if (!gameExists(gameId, redis)) {
                val game = Game.start(
                    rootScope,
                    gameId,
                    req.carsCount,
                    req.calculateScoresInProcess,
                    req.map,
                    redis
                )
                games[gameId] = game
                redis?.saveMap(gameId, req.map)
                val conn = Connection.Player(req.playerName, this)
                game.joinPlayer(conn, req.playerColor)
            } else
                ConnectionOutcome.Failure(ConnectResponse.Failure.GameIdTaken)

        is ConnectRequest.Join ->
            loadGame(gameId, redis) {
                val conn = Connection.Player(req.playerName, this)
                it.joinPlayer(conn, req.playerColor)
            }

        is ConnectRequest.Reconnect ->
            loadGame(gameId, redis) {
                it.reconnectPlayer(Connection.Player(req.playerName, this))
            }

        is ConnectRequest.Observe ->
            loadGame(gameId, redis) {
                val conn = Connection.Observer(this)
                it.joinObserver(conn)
                ConnectionOutcome.Success(it, conn)
            }
    }

    val resp = when (outcome) {
        is ConnectionOutcome.Success ->
            ConnectResponse.Success(
                outcome.game.id,
                outcome.game.map,
                outcome.game.calculateScoresInProcess
            )
        is ConnectionOutcome.Failure -> outcome.reason
    }
    send(resp, ConnectResponse.serializer())

    return outcome
}

private sealed class Connection(private val webSocket: WebSocketSession) : ClientConnection {
    override suspend fun <T> send(msg: T, serializer: SerializationStrategy<T>) =
        webSocket.send(msg, serializer)

    override suspend fun isConnected() = try {
        webSocket.send(Response.Pong); true
    } catch (e: CancellationException) {
        false
    }

    class Player(override val name: PlayerName, webSocket: WebSocketSession) : Connection(webSocket),
        PlayerConnection {
        override fun toString() = name.value
    }

    class Observer(webSocket: WebSocketSession) : Connection(webSocket) {
        override fun toString() = "anonymous observer"
    }
}

// Serialization protocol

private suspend fun <T> WebSocketSession.send(msg: T, serializer: SerializationStrategy<T>) =
    send(json.stringify(serializer, msg))

private fun <T> deserialize(msg: Frame, deserializer: DeserializationStrategy<T>): T {
    val reqStr = (msg as Frame.Text).readText()
    return json.parse(deserializer, reqStr)
}