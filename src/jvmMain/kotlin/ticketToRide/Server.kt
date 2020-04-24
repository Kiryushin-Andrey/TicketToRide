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

private typealias PlayerCallback = suspend (GameId, GameState) -> Unit

private data class Game(val requestsQueue: Channel<Pair<Request, Connection>>, val notifyCallbacks: MutableList<PlayerCallback>)
data class Connection(val gameId: GameId, val playerName: PlayerName)

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
                .fold<Request, Connection?>(null) {
                    conn, req ->
                    when (req) {
                        is StartGameRequest -> {
                            val gameId = startGame(req.playerName) { id, state ->
                                send(GameStateResponse(id, state.toPlayerView(req.playerName)))
                            }
                            Connection(gameId, req.playerName)
                        }
                        is JoinGameRequest -> {
                            val game = games[req.gameId]
                            if (game == null) send(FailureResponse(JoinGameFailure.GameNotExists))
                            val connection = Connection(req.gameId, req.playerName)
                            game?.apply {
                                notifyCallbacks += { gameId, state ->
                                    send(
                                        GameStateResponse(
                                            gameId,
                                            state.toPlayerView(req.playerName)
                                        )
                                    )
                                }
                                requestsQueue.send(req to connection)
                            }
                            connection
                        }
                        else -> {
                            games[conn!!.gameId]?.apply { requestsQueue.send(req to conn) }
                            conn
                        }
                    }
                }
        }
    }
}

suspend fun WebSocketSession.send(resp: Response) = send(json.stringify(Response.serializer(), resp))

fun CoroutineScope.startGame(firstPlayerName: PlayerName, firstPlayerCallback: PlayerCallback) : GameId {
    val requestsQueue = Channel<Pair<Request, Connection>>()
    val subscriptions = mutableListOf(firstPlayerCallback)
    val gameId = GameId(Random.nextBytes(5).joinToString("") { "%02x".format(it) })
    launch {
        runGame(firstPlayerName, requestsQueue.consumeAsFlow()).collect { state ->
            for (notify in subscriptions) notify(gameId, state)
        }
    }
    games[gameId] = Game(requestsQueue, subscriptions)
    return gameId
}