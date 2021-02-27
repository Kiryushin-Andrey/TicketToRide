package ticketToRide

import kotlinx.coroutines.CompletableJob
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Request

// initiated from server to wait for request in queue to be processed before moving on
// with the connection handshake process
class Callback(val job: CompletableJob) : Request()

// initiated from server as part of connection handshake process
class JoinPlayer(val color: PlayerColor) : Request()

@Serializable
@SerialName("message")
class ChatMessage(val message: String) : Request()

@Serializable
@SerialName("leave")
object LeaveGameRequest : Request()

@Serializable
@SerialName("confirmTickets")
class ConfirmTicketsChoiceRequest(val ticketsToKeep: List<Ticket>) : Request()

@Serializable
sealed class PickedCard {

    @Serializable
    @SerialName("open")
    data class Open(val ix: Int, val card: Card.Car) : PickedCard()

    @Serializable
    @SerialName("closed")
    object Closed : PickedCard()
}

@Serializable
sealed class PickCardsRequest : Request() {

    @Serializable
    @SerialName("pickLoco")
    class Loco(val ix: Int) : PickCardsRequest()

    @Serializable
    @SerialName("pickTwoCards")
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

    fun getCardsToPick(map: GameMap) = when (this) {
        is Loco -> listOf(Card.Loco)
        is TwoCards -> cards.toList().map {
            when (it) {
                is PickedCard.Open -> it.card
                is PickedCard.Closed -> Card.random(map)
            }
        }
    }

    fun getIndicesToReplace() = when (this) {
        is Loco -> listOf(ix)
        is TwoCards -> cards.toList().filterIsInstance<PickedCard.Open>().map { it.ix }
    }
}

@Serializable
@SerialName("pickTickets")
object PickTicketsRequest : Request()

@Serializable
@SerialName("build")
class BuildSegmentRequest(val from: CityName, val to: CityName, val cards: List<Card>) : Request()

@Serializable
@SerialName("station")
class BuildStationRequest(val target: CityName, val cards: List<Card>) : Request()