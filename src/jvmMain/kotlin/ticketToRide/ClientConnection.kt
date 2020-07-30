package ticketToRide

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.serialization.SerializationStrategy

private val rootScope = CoroutineScope(Dispatchers.Default + Job())

sealed class ClientConnection(private val webSocket: WebSocketSession) {
    suspend fun <T> send(resp: T, serializer: SerializationStrategy<T>) =
        webSocket.send(json.stringify(serializer, resp))

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
    class ObserveSuccess(val game: Game, val connection: ClientConnection.Observer) : ConnectionOutcome()
    class Failure(val reason: ConnectResponse.Failure) : ConnectionOutcome()
}

private fun gameExists(id: GameId, redis: RedisStorage?) = redis?.hasGame(id) ?: games.containsKey(id)

private suspend fun loadGame(
    id: GameId,
    redis: RedisStorage?,
    process: suspend (Game) -> ConnectionOutcome
): ConnectionOutcome {
    val game = games.getOrElse(id) {
        redis?.loadGame(id)?.let { (state, map) ->
            Game.restore(rootScope, state, map, redis) { games.remove(id) }.also { game ->
                games[id] = game
            }
        }
    }
    return if (game != null) process(game)
    else ConnectionOutcome.Failure(ConnectResponse.Failure.NoSuchGame)
}

suspend fun WebSocketSession.establishConnection(gameId: GameId, redis: RedisStorage?): ConnectionOutcome {
    val req = (incoming.receive() as Frame.Text).readText()
        .let { json.parse(ConnectRequest.serializer(), it) }

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
                ) { games.remove(it.id) }
                games[gameId] = game
                redis?.saveMap(gameId, req.map)
                game.joinPlayer(req.playerName, req.playerColor, this)
            } else
                ConnectionOutcome.Failure(ConnectResponse.Failure.GameIdTaken)

        is ConnectRequest.Join ->
            loadGame(gameId, redis) {
                it.joinPlayer(req.playerName, req.playerColor, this)
            }

        is ConnectRequest.Reconnect ->
            loadGame(gameId, redis) {
                it.reconnectPlayer(req.playerName, this)
            }

        is ConnectRequest.Observe ->
            loadGame(gameId, redis) {
                val conn = ClientConnection.Observer(this)
                it.joinObserver(conn)
                ConnectionOutcome.ObserveSuccess(it, conn)
            }
    }

    val resp = when (outcome) {
        is ConnectionOutcome.Success -> ConnectResponse.Success
        is ConnectionOutcome.ObserveSuccess -> ConnectResponse.Success
        is ConnectionOutcome.Failure -> outcome.reason
    }
    send(json.stringify(ConnectResponse.serializer(), resp))

    return outcome
}