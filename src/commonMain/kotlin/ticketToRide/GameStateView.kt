package ticketToRide

import kotlinx.serialization.Serializable

@Serializable
data class GameId(val value: String)

@Serializable
data class CityName(val value: String)

@Serializable
data class PlayerName(val value: String)

@Serializable
data class Card(val value: Color) {
    companion object {
        fun random() = Card(Color.values().random())
    }
}

val Card.isLoko: Boolean
    get() = this.value == Color.NONE

@Serializable
data class Ticket(val from: CityName, val to: CityName, val points: Int) {
    fun sharesCityWith(another: Ticket) = listOf(from, to).intersect(listOf(another.from, another.to)).any()
}

@Serializable
data class PendingTicketsChoice(val tickets: List<Ticket>, val shouldChooseOnNextTurn: Boolean)

fun PendingTicketsChoice?.toState() = when {
    this == null -> PendingTicketsChoiceState.None
    this.shouldChooseOnNextTurn -> PendingTicketsChoiceState.Choosing
    else -> PendingTicketsChoiceState.TookInAdvance
}

@Serializable
enum class PendingTicketsChoiceState { None, TookInAdvance, Choosing }

@Serializable
data class PlayerView(
    val name: PlayerName,
    val color: Color,
    val carsLeft: Int,
    val cardsOnHand: Int,
    val ticketsOnHand: Int,
    val pendingTicketsChoice: PendingTicketsChoiceState,
    val away: Boolean
)

@Serializable
data class GameStateView(
    val players: List<PlayerView>,
    val openCards: List<Card>,
    val turn: Int,
    val myCards: Map<Card, Int>,
    val myTicketsOnHand: List<Ticket>,
    val myPendingTicketsChoice: PendingTicketsChoice?
)