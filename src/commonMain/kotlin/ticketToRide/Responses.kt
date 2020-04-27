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
class GameStateResponse(val gameId: GameId, val state: GameStateView) : Response()

@Serializable
class GameEndResponse(val gameId: GameId, val players: List<Pair<PlayerView, List<Ticket>>>) : Response()