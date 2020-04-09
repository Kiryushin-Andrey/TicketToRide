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

private data class Game(val requestsQueue: Channel<Request>, val notifyCallbacks: MutableList<PlayerCallback>)

private val games = mutableMapOf<GameId, Game>()
private val json = Json(JsonConfiguration.Default.copy(allowStructuredMapKeys = true))

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(WebSockets)
    routing {
        static {
            resource("ticket-to-ride.js")
            resource("favicon.ico")
        }
        static("icons") { resources("icons") }
        get("/") {
            call.push("/ticket-to-ride.js")
            call.respondHtml { indexHtml() }
        }
        webSocket("ws") {
            incoming.consumeAsFlow()
                .mapNotNull { (it as? Frame.Text)?.readText() }
                .map { json.parse(Request.serializer(), it) }
                .collect { req ->
                    when (req) {
                        is StartGameRequest -> {
                            val player = Player(req.playerName, Color.randomForPlayer())
                            startGame(player) { gameId, state -> send(GameStateResponse(gameId, state)) }
                        }
                        is JoinGameRequest -> {
                            val game = games[req.gameId]
                            if (game == null) send(FailureResponse(JoinGameFailure.GameNotExists))
                            game?.apply {
                                notifyCallbacks += { gameId, state -> send(GameStateResponse(gameId, state)) }
                                requestsQueue.send(req)
                            }
                        }
                        is GameRequest -> {
                            games[req.gameId]?.apply { requestsQueue.send(req) }
                        }
                    }
                }
        }
    }
}

suspend fun WebSocketSession.send(resp: Response) = send(json.stringify(Response.serializer(), resp))

fun CoroutineScope.startGame(firstPlayer: Player, firstPlayerCallback: PlayerCallback) {
    val requestsQueue = Channel<Request>()
    val subscriptions = mutableListOf(firstPlayerCallback)
    val gameId = GameId(Random.nextBytes(5).joinToString("") { "%02x".format(it) })
    launch {
        runGame(firstPlayer, requestsQueue.consumeAsFlow()).collect { state ->
            for (notify in subscriptions) notify(gameId, state)
        }
    }
    games[gameId] = Game(requestsQueue, subscriptions)
}