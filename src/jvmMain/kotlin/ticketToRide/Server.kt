package ticketToRide

import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.html.respondHtml
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.http.push
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import mu.KotlinLogging

class PlayerConnection(val gameId: GameId, val name: PlayerName, private val ws: WebSocketSession) {
    suspend fun send(resp: Response) = ws.send(json.stringify(Response.serializer(), resp))
}

private val games = mutableMapOf<GameId, Game>()
private val rootScope = CoroutineScope(Dispatchers.Default + Job())
private val json = Json(JsonConfiguration.Default.copy(allowStructuredMapKeys = true))
private val logger = KotlinLogging.logger("Server")

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

@FlowPreview
fun Application.module() {
    val googleApiKey = environment.config.property("google-api-key").getString()
    install(WebSockets)
    install(ContentNegotiation) { json(json) }
    routing {
        static {
            resource("ticket-to-ride.js")
            resource("favicon.ico")
        }
        static("icons") { resources("icons") }
        static("cards") { resources("cards") }

        get("/") {
            call.push("/ticket-to-ride.js")
            call.respondHtml { indexHtml(googleApiKey) }
        }
        get("/game/{gameId}") {
            call.push("/ticket-to-ride.js")
            call.respondHtml { indexHtml(googleApiKey) }
        }

        route("/internal") {
            get("/games") {
                call.respond(games.entries.associate { it.key.value to it.value.playerNames })
            }
            get("/game/{id}") {
                call.parameters["id"]?.let { id ->
                    games[GameId(id)]?.let { game ->
                        call.respond(game.getState())
                    }
                }
            }
        }

        webSocket("ws") {
            var connection: PlayerConnection? = null
            incoming.consumeAsFlow()
                .mapNotNull { (it as? Frame.Text)?.readText() }
                .mapNotNull { req ->
                    kotlin.runCatching { json.parse(Request.serializer(), req) }.also {
                        logger.info { "Received $req from ${connection?.name?.value}" }
                        it.exceptionOrNull()?.let { e ->
                            logger.warn(e) { "Failed to deserialize request: $req" }
                            connection?.let { conn -> games[conn.gameId]?.leavePlayer(conn) }
                            close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Bad request"))
                        }
                    }.getOrNull()
                }
                .collect { req ->
                    when (req) {

                        is StartGameRequest ->
                            Game { games.remove(it.id) }.let { game ->
                                games[game.id] = game
                                connection = PlayerConnection(game.id, req.playerName, this).also { conn ->
                                    rootScope.launch { game.start(conn) }
                                }
                            }

                        is JoinGameRequest -> {
                            connection = PlayerConnection(req.gameId, req.playerName, this).also { conn ->
                                games[req.gameId]?.joinPlayer(req, conn)
                                    ?: conn.send(Response.ErrorMessage("No such game"))
                            }
                        }

                        is LeaveGameRequest -> {
                            connection?.let { games[it.gameId]?.leavePlayer(it) }
                            close(CloseReason(CloseReason.Codes.NORMAL, "Exit game"))
                        }

                        is ChatMessageRequest ->
                            connection?.let { conn ->
                                games[conn.gameId]?.let {
                                    it.sendToAll { Response.ChatMessage(conn.name, req.message) }
                                }
                            }

                        is GameRequest ->
                            connection?.let { games[it.gameId]?.process(req, it) }
                    }
                }
        }
    }
}