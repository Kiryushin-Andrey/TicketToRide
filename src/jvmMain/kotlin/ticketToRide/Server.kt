package ticketToRide

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.html.respondHtml
import io.ktor.http.cio.websocket.Frame
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json.Default.parse
import kotlinx.serialization.json.Json.Default.stringify
import kotlin.random.Random

private typealias PlayerCallback = suspend (GameState) -> Unit

private data class Game(val requestsQueue: Channel<ApiRequest>, val notifyCallbacks: MutableList<PlayerCallback>)

private val games = mutableMapOf<String, Game>()

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(WebSockets)
    routing {
        static {
            resource("ticket-to-ride.js")
            resources("icons")
        }
        get("/") {
            call.push("/ticket-to-ride.js")
            call.respondHtml { indexHtml() }
        }
        webSocket("ws") {
            incoming.consumeAsFlow()
                .mapNotNull { (it as? Frame.Text)?.readText() }
                .map { parse(ApiRequest.serializer(), it) }
                .collect { req ->
                    when (req) {
                        is StartGame -> {
                            val player = Player(req.playerId, req.playerName, Color.random())
                            startGame(player) { state -> send(stringify(GameState.serializer(), state)) }
                        }
                        is JoinGame -> {
                            val game = games[req.gameId.value]
                            if (game == null) send(NoGameFound)
                            game?.apply {
                                notifyCallbacks += { state -> send(stringify(GameState.serializer(), state)) }
                                requestsQueue.send(req)
                            }
                        }
                        is GameRequest -> {
                            games[req.gameId.value]?.apply { requestsQueue.send(req) }
                        }
                    }
                }
        }
    }
}

fun CoroutineScope.startGame(firstPlayer: Player, firstPlayerCallback: PlayerCallback): String {
    val requestsQueue = Channel<ApiRequest>()
    val subscriptions = mutableListOf(firstPlayerCallback)
    launch {
        runGame(firstPlayer, requestsQueue.consumeAsFlow()).collect { state ->
            for (notify in subscriptions) notify(state)
        }
    }
    val gameId = Random.nextBytes(5).asUByteArray().joinToString { "%02x".format(it) }
    games[gameId] = Game(requestsQueue, subscriptions)
    return gameId
}