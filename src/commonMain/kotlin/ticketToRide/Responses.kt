package ticketToRide

import kotlinx.serialization.Serializable

enum class JoinGameFailure {
    GameNotExists,
    PlayerNameEmpty,
    PlayerNameTaken
}

@Serializable
sealed class Response()

@Serializable
class FailureResponse(val reason: JoinGameFailure) : Response()

@Serializable
class GameStateResponse(val gameId: GameId, val state: GameState) : Response()