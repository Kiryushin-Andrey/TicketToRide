package ticketToRide

import kotlinx.serialization.*

@Serializable
sealed class Request

@Serializable
class StartGameRequest(val playerName: PlayerName) : Request()

@Serializable
class JoinGameRequest(val gameId: GameId, val playerName: PlayerName): Request()

@Serializable
class ConfirmTicketsChoiceRequest(val ticketsToKeep: List<Ticket>) : Request()

@Serializable
sealed class PickCardsRequest : Request() {

    @Serializable
    object Loco : PickCardsRequest()

    @Serializable
    class TwoCards(val cards: Pair<Card?, Card?>) : PickCardsRequest()
}

@Serializable
object PickTicketsRequest : Request()