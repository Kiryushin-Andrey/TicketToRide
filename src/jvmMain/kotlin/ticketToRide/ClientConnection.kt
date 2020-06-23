package ticketToRide

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val rootScope = CoroutineScope(Dispatchers.Default + Job())

class ClientConnection(
    val name: PlayerName,
    private val ws: WebSocketSession
) {
    suspend fun send(resp: Response) = ws.send(json.stringify(Response.serializer(), resp))
    suspend fun ping() = ws.send(Response.Pong)
}

sealed class ConnectionOutcome {
    class Success(val game: Game, val connection: ClientConnection) : ConnectionOutcome()
    class Failure(val reason: ConnectResponse.Failure) : ConnectionOutcome()
}

fun gameExists(id: GameId, redis: RedisCredentials?) = redis?.hasGame(id) ?: games.containsKey(id)

suspend fun WebSocketSession.establishConnection(gameId: GameId, redis: RedisCredentials?): ConnectionOutcome {
    val req = (incoming.receive() as Frame.Text).readText()
        .let { json.parse(ConnectRequest.serializer(), it) }

    val conn = ClientConnection(req.playerName, this)
    val outcome = when (req) {
        is ConnectRequest.StartGame ->
            if (!gameExists(gameId, redis))
                Game(gameId, req.carsCount, redis) { games.remove(it.id) }.let { game ->
                    games[gameId] = game
                    redis?.saveMap(gameId, req.map)
                    rootScope.launch { game.start(conn, req.map) }
                    ConnectionOutcome.Success(game, conn)
                }
            else
                ConnectionOutcome.Failure(ConnectResponse.Failure.GameIdTaken)

        is ConnectRequest.JoinGame ->
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

    when (outcome) {
        is ConnectionOutcome.Success -> ConnectResponse.Success
        is ConnectionOutcome.Failure -> outcome.reason
    }.let { resp -> send(json.stringify(ConnectResponse.serializer(), resp)) }

    return outcome
}