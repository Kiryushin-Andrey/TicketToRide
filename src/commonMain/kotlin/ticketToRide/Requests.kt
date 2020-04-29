package ticketToRide

import kotlinx.serialization.*

@Serializable
sealed class Request

@Serializable
class StartGameRequest(val playerName: PlayerName) : Request()

@Serializable
class ChatMessageRequest(val message: String) : Request()

@Serializable
sealed class GameRequest: Request()

@Serializable
class JoinGameRequest(val gameId: GameId, val playerName: PlayerName) : GameRequest()

@Serializable
class ConfirmTicketsChoiceRequest(val ticketsToKeep: List<Ticket>) : GameRequest()

@Serializable
sealed class PickCardsRequest : GameRequest() {

    @Serializable
    object Loco : PickCardsRequest()

    @Serializable
    class TwoCards(val cards: Pair<Card.Car?, Card.Car?>) : PickCardsRequest()
}

@Serializable
object PickTicketsRequest : GameRequest()

@Serializable
class BuildSegmentRequest(val from: CityName, val to: CityName, val cards: List<Card>) : GameRequest()