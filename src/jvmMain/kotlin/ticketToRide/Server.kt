package ticketToRide

import io.ktor.application.*
import io.ktor.html.respondHtml
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.http.push
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import kotlin.random.Random

private data class Game(
    private val id: GameId,
    private val requestsQueue: Channel<Pair<GameRequest, PlayerConnection>>,
    private val players: MutableList<PlayerConnection>
) {
    suspend fun joinPlayer(req: JoinGameRequest, conn: PlayerConnection) {
        players += conn
        requestsQueue.send(req to conn)
    }

    suspend fun leavePlayer(conn: PlayerConnection) {
        players -= conn
        handlePlayerLeave(conn)
    }

    suspend fun process(req: GameRequest, conn: PlayerConnection) {
        requestsQueue.send(req to conn)
    }

    suspend fun send(playerName: PlayerName, resp: Response) {
        players.find { it.name == playerName }?.let { player ->
            try {
                player.send(resp)
            } catch (e: CancellationException) {
                players.removeIf { it.name == playerName }
                handlePlayerLeave(player)
            }
        }
    }

    suspend fun sendToAll(resp: (PlayerName) -> Response) {
        with(players.iterator()) {
            forEach {
                try {
                    it.send(resp(it.name))
                } catch (e: CancellationException) {
                    remove()
                    handlePlayerLeave(it)
                }
            }
        }
    }

    private suspend fun handlePlayerLeave(player: PlayerConnection) {
        if (players.size == 0) {
            requestsQueue.close()
            games.remove(id)
        } else {
            process(LeaveGameRequest, player)
        }
    }
}

class PlayerConnection(val gameId: GameId, val name: PlayerName, private val ws: WebSocketSession) {
    suspend fun send(resp: Response) = ws.send(json.stringify(Response.serializer(), resp))
}

private val games = mutableMapOf<GameId, Game>()
private val rootScope = CoroutineScope(Dispatchers.Default + Job())
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
            lateinit var connection: PlayerConnection
            incoming.consumeAsFlow()
                .mapNotNull { (it as? Frame.Text)?.readText() }
                .map { json.parse(Request.serializer(), it) }
                .collect { req ->
                    when (req) {

                        is StartGameRequest ->
                            GameId(Random.nextBytes(5).joinToString("") { "%02x".format(it) }).also { gameId ->
                                connection = PlayerConnection(gameId, req.playerName, this).apply {
                                    games[gameId] = rootScope.startGame(gameId, this)
                                }
                            }

                        is JoinGameRequest -> {
                            connection = PlayerConnection(req.gameId, req.playerName, this)
                            games[req.gameId]?.let { game ->
                                game.joinPlayer(req, connection)
                            } ?: connection.send(Response.Error(JoinGameFailure.GameNotExists))
                        }

                        is LeaveGameRequest ->
                            games[connection.gameId]?.leavePlayer(connection)

                        is ChatMessageRequest ->
                            games[connection.gameId]?.let {
                                it.sendToAll { Response.ChatMessage(connection.name, req.message) }
                            }

                        is GameRequest ->
                            games[connection.gameId]?.process(req, connection)
                    }
                }
        }
    }
}

private fun CoroutineScope.startGame(gameId: GameId, firstPlayer: PlayerConnection): Game {
    val requestsQueue = Channel<Pair<GameRequest, PlayerConnection>>(Channel.BUFFERED).also {
        it.offer(JoinGameRequest(gameId, firstPlayer.name) to firstPlayer)
    }
    return Game(gameId, requestsQueue, mutableListOf(firstPlayer)).also { game ->
        launch {
            runGame(gameId, requestsQueue.consumeAsFlow())
                .collect { message ->
                    when (message) {
                        is SendResponse.ForAll ->
                            game.sendToAll(message.resp)
                        is SendResponse.ForPlayer ->
                            game.send(message.to, message.resp)
                    }
                }
        }
    }
}