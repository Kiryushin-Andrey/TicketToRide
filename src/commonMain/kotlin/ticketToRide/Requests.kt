package ticketToRide

import kotlinx.serialization.*

const val NoGameFound = "404"

@Serializable
sealed class Request()

interface GameRequest {
    val gameId : GameId
    val playerName : PlayerName
}

@Serializable
class StartGameRequest(val playerName: PlayerName) : Request()

@Serializable
class JoinGameRequest(
    override val gameId: GameId,
    override val playerName: PlayerName): Request(), GameRequest

@Serializable
class WentAwayRequest(
    override val gameId: GameId,
    override val playerName: PlayerName) : Request(), GameRequest

@Serializable
class CameBackRequest(
    override val gameId: GameId,
    override val playerName: PlayerName) : Request(), GameRequest