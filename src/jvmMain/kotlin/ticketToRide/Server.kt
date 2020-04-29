package ticketToRide

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.html.respondHtml
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import io.ktor.http.content.resource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.http.push
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import kotlin.random.Random

private data class Game(val requestsQueue: Channel<Pair<GameRequest, Connection>>, val players: MutableList<Connection>)

data class Connection(
    val gameId: GameId,
    val playerName: PlayerName,
    val send: suspend (Response) -> Unit
)

private val games = mutableMapOf<GameId, Game>()
private val json = Json(JsonConfiguration.Default.copy(allowStructuredMapKeys = true))

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val googleApiKey = environment.config.property("google-api-key").getString()
    install(WebSockets)
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
        webSocket("ws") {
            incoming.consumeAsFlow()
                .mapNotNull { (it as? Frame.Text)?.readText() }
                .map { json.parse(Request.serializer(), it) }
                .fold<Request, Connection?>(null) { conn, req ->
                    when (req) {

                        is StartGameRequest -> {
                            val gameId = GameId(Random.nextBytes(5).joinToString("") { "%02x".format(it) })
                            val connection = Connection(gameId, req.playerName) { resp ->
                                send(resp)
                            }
                            games[gameId] = startGame(gameId, connection)
                            connection
                        }

                        is JoinGameRequest -> {
                            val game = games[req.gameId]
                            if (game == null) send(Response.Error(JoinGameFailure.GameNotExists))
                            val connection = Connection(req.gameId, req.playerName) { resp -> send(resp) }
                            game?.apply {
                                players += connection
                                requestsQueue.send(req to connection)
                            }
                            connection
                        }

                        is ChatMessageRequest -> {
                            conn!!.also { conn ->
                                games[conn.gameId]!!.players.forEach {
                                    it.send(Response.ChatMessage(conn.playerName, req.message))
                                }
                            }
                        }

                        is GameRequest -> {
                            conn!!.also {
                                games[conn.gameId]?.apply { requestsQueue.send(req to conn) }
                            }
                        }
                    }
                }
        }
    }
}

suspend fun WebSocketSession.send(resp: Response) = send(json.stringify(Response.serializer(), resp))

private fun CoroutineScope.startGame(gameId: GameId, firstPlayer: Connection): Game {
    val requestsQueue = Channel<Pair<GameRequest, Connection>>(Channel.BUFFERED)
    requestsQueue.offer(JoinGameRequest(gameId, firstPlayer.playerName) to firstPlayer)
    val players = mutableListOf(firstPlayer)
    launch {
        runGame(gameId, requestsQueue.consumeAsFlow())
            .collect { message ->
                when (message) {
                    is SendResponse.ForAll ->
                        players.forEach { conn -> conn.send(message(conn.playerName)) }
                    is SendResponse.ForPlayer ->
                        players.find { it.playerName == message.to }?.send?.invoke(message.resp)
                }
            }
    }
    return Game(requestsQueue, players)
}