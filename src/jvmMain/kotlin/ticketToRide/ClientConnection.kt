package ticketToRide

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import core.*

private val rootScope = CoroutineScope(Dispatchers.Default + Job())

class PlayerConnection(val name: PlayerName, val game: Game, webSocket: WebSocketSession) :
    ListenerConnection<Response>(webSocket, Response.serializer()) {

    override fun toString() = name.value
}

class ObserverConnection(val game: Game, webSocket: WebSocketSession) :
    ListenerConnection<GameStateForObservers>(webSocket, GameStateForObservers.serializer()) {

    override fun toString() = "anonymous observer"
}

fun cannotJoin(reason: CannotJoinReason) = ConnectionOutcome.CannotJoin(reason)

private suspend fun <T> loadGame(
    id: GameId,
    redis: RedisStorage?,
    process: suspend (Game) -> ConnectionOutcome<T, CannotJoinReason>
): ConnectionOutcome<T, CannotJoinReason> {
    val game = games.getOrElse(id) {
        redis?.loadGame(id)?.let { (state, map) ->
            Game.restore(rootScope, state, map, redis) { games.remove(id) }.also { game ->
                games[id] = game
            }
        }
    }
    return if (game != null) process(game)
    else cannotJoin(CannotJoinReason.NoSuchGame)
}

suspend fun WebSocketSession.connectAsPlayer(
    gameId: GameId,
    redis: RedisStorage?
): ConnectionOutcome<PlayerConnection, CannotJoinReason> {

    fun gameExists(id: GameId, redis: RedisStorage?) = redis?.hasGame(id) ?: games.containsKey(id)

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
                cannotJoin(CannotJoinReason.GameIdTaken)

        is ConnectRequest.Join ->
            loadGame(gameId, redis) {
                it.joinPlayer(req.playerName, req.playerColor, this)
            }

        is ConnectRequest.Reconnect ->
            loadGame(gameId, redis) {
                it.reconnectPlayer(req.playerName, this)
            }
    }

    val resp = when (outcome) {
        is ConnectionOutcome.Success -> ConnectResponse.Success
        is ConnectionOutcome.CannotJoin -> ConnectResponse.CannotJoin(outcome.reason)
        is ConnectionOutcome.CannotConnect -> ConnectResponse.CannotConnect
    }
    send(Frame.Text(json.stringify(ConnectResponse.serializer(CannotJoinReason.serializer()), resp)))

    return outcome
}

suspend fun WebSocketSession.connectAsObserver(
    gameId: GameId,
    redis: RedisStorage?
): ConnectionOutcome<ObserverConnection, CannotJoinReason> {
    incoming.receive()
    val outcome = loadGame(gameId, redis) { game ->
        val conn = ObserverConnection(game, this)
        game.joinObserver(conn)
        ConnectionOutcome.Success(conn)
    }
    val resp = when (outcome) {
        is ConnectionOutcome.Success -> ConnectResponse.Success
        is ConnectionOutcome.CannotJoin -> ConnectResponse.CannotJoin(outcome.reason)
        is ConnectionOutcome.CannotConnect -> ConnectResponse.CannotConnect
    }
    send(Frame.Text(json.stringify(ConnectResponse.serializer(CannotJoinReason.serializer()), resp)))
    return outcome
}