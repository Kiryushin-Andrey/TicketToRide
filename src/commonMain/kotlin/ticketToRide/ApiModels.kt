package ticketToRide

import kotlinx.serialization.*

const val NoGameFound = "404"

enum class JoinGameFailure {
    GameNotExists,
    PlayerNameEmpty,
    PlayerNameTaken
}

@Serializable
sealed class ApiRequest()

interface GameRequest {
    val gameId : GameId
    val playerId : PlayerId
}

@Serializable
class StartGame(
    val playerId: PlayerId,
    val playerName: PlayerName) : ApiRequest()

@Serializable
class JoinGame(
    override val gameId: GameId,
    override val playerId: PlayerId,
    val playerName: PlayerName): ApiRequest(), GameRequest

@Serializable
class WentAway(
    override val gameId: GameId,
    override val playerId: PlayerId) : ApiRequest(), GameRequest

@Serializable
class CameBack(
    override val gameId: GameId,
    override val playerId: PlayerId) : ApiRequest(), GameRequest