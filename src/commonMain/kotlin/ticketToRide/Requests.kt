package ticketToRide

import kotlinx.serialization.*

@Serializable
sealed class Request

@Serializable
class StartGameRequest(val playerName: PlayerName, val carsCount: Int) : Request()

@Serializable
class ChatMessageRequest(val message: String) : Request()

@Serializable
sealed class GameRequest: Request()

@Serializable
class JoinGameRequest(val gameId: GameId, val playerName: PlayerName) : GameRequest()

@Serializable
object LeaveGameRequest : GameRequest()

@Serializable
class ConfirmTicketsChoiceRequest(val ticketsToKeep: List<Ticket>) : GameRequest()

@Serializable
sealed class PickedCard {

    @Serializable
    data class Open(val ix: Int, val card: Card.Car) : PickedCard()

    @Serializable
    object Closed : PickedCard()
}

@Serializable
sealed class PickCardsRequest : GameRequest() {

    @Serializable
    class Loco(val ix: Int) : PickCardsRequest()

    @Serializable
    class TwoCards private constructor(val cards: Pair<PickedCard, PickedCard>) : PickCardsRequest() {
        companion object {
            fun bothOpen(ix1: Int, ix2: Int, openCards: List<Card>) = TwoCards(
                PickedCard.Open(ix1, openCards[ix1] as Card.Car) to PickedCard.Open(ix2, openCards[ix2] as Card.Car)
            )

            fun openAndClosed(ix: Int, openCards: List<Card>) = TwoCards(
                PickedCard.Open(ix, openCards[ix] as Card.Car) to PickedCard.Closed
            )

            fun bothClosed() = TwoCards(PickedCard.Closed to PickedCard.Closed)
        }
    }

    fun getCardsToPick() = when (this) {
        is Loco -> listOf(Card.Loco)
        is TwoCards -> cards.toList().map {
            when (it) {
                is PickedCard.Open -> it.card
                is PickedCard.Closed -> Card.random()
            }
        }
    }

    fun getIndicesToReplace() = when (this) {
        is Loco -> listOf(ix)
        is TwoCards -> cards.toList().filterIsInstance<PickedCard.Open>().map { it.ix }
    }
}

@Serializable
object PickTicketsRequest : GameRequest()

@Serializable
class BuildSegmentRequest(val from: CityName, val to: CityName, val cards: List<Card>) : GameRequest()