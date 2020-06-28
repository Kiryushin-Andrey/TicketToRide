package ticketToRide

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationStrategy

private val rootScope = CoroutineScope(Dispatchers.Default + Job())

sealed class ClientConnection(private val webSocket: WebSocketSession) {
    suspend fun <T> send(resp: T, serializer: SerializationStrategy<T>) = webSocket.send(json.stringify(serializer, resp))
    suspend fun ping() = webSocket.send(Response.Pong)

    class Player(val name: PlayerName, webSocket: WebSocketSession) : ClientConnection(webSocket) {
        override fun toString() = name.value
    }
    class Observer(webSocket: WebSocketSession) : ClientConnection(webSocket) {
        override fun toString() = "anonymous observer"
    }
}

sealed class ConnectionOutcome {
    class Success(val game: Game, val connection: ClientConnection.Player) : ConnectionOutcome()
    object ObserveSuccess : ConnectionOutcome()
    class Failure(val reason: ConnectResponse.Failure) : ConnectionOutcome()
}

fun gameExists(id: GameId, redis: RedisCredentials?) = redis?.hasGame(id) ?: games.containsKey(id)

suspend fun WebSocketSession.establishConnection(gameId: GameId, redis: RedisCredentials?): ConnectionOutcome {
    val req = (incoming.receive() as Frame.Text).readText()
        .let { json.parse(ConnectRequest.serializer(), it) }

    val outcome = when (req) {
        is ConnectRequest.Start ->
            ClientConnection.Player(req.playerName, this).let { conn ->
                if (!gameExists(gameId, redis))
                    Game(gameId, req.carsCount, redis) { games.remove(it.id) }.let { game ->
                        games[gameId] = game
                        redis?.saveMap(gameId, req.map)
                        rootScope.launch { game.start(conn, req.map) }
                        ConnectionOutcome.Success(game, conn)
                    }
                else
                    ConnectionOutcome.Failure(ConnectResponse.Failure.GameIdTaken)
            }

        is ConnectRequest.Join ->
            ClientConnection.Player(req.playerName, this).let { conn ->
                games[gameId]?.let { game ->
                    if (game.joinPlayer(req.playerName, conn))
                        ConnectionOutcome.Success(game, conn)
                    else
                        ConnectionOutcome.Failure(ConnectResponse.Failure.PlayerNameTaken)
                } ?: redis?.loadGame(gameId)?.let { (state, map) ->
                    Game(gameId, state.initialCarsCount, redis) { games.remove(gameId) }.let { game ->
                        games[gameId] = game
                        rootScope.launch { game.restore(conn, state, map) }
                        ConnectionOutcome.Success(game, conn)
                    }
                } ?: ConnectionOutcome.Failure(ConnectResponse.Failure.NoSuchGame)
            }

        is ConnectRequest.Observe ->
            ClientConnection.Observer(this).let { conn ->
                games[gameId]?.let { game ->
                    game.joinObserver(conn)
                    ConnectionOutcome.ObserveSuccess
                } ?: ConnectionOutcome.Failure(ConnectResponse.Failure.NoSuchGame)
            }
    }

    when (outcome) {
        is ConnectionOutcome.Success -> ConnectResponse.Success
        is ConnectionOutcome.ObserveSuccess -> ConnectResponse.Success
        is ConnectionOutcome.Failure -> outcome.reason
    }.let { resp -> send(json.stringify(ConnectResponse.serializer(), resp)) }

    return outcome
}