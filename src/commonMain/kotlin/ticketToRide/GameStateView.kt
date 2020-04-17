package ticketToRide

import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class GameId(val value: String)

@Serializable
data class CityName(val value: String)

@Serializable
data class PlayerName(val value: String)

@Serializable
sealed class Card {

    @Serializable
    data class Car(val value: Color) : Card()

    @Serializable
    object Loco : Card()

    companion object {
        fun random(): Card {
            val colors = Color.values()
            val value = Random.nextInt(colors.size + 1)
            return if (value < colors.size) Car(colors[value]) else Loco
        }

        fun randomNoLoco() = Car(Color.values().random())
    }
}

@Serializable
data class Ticket(val from: CityName, val to: CityName, val points: Int) {
    fun sharesCityWith(another: Ticket) = listOf(from, to).intersect(listOf(another.from, another.to)).any()
}

@Serializable
data class PendingTicketsChoice(val tickets: List<Ticket>, val minCountToKeep: Int, val shouldChooseOnNextTurn: Boolean)

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
    val myName: PlayerName,
    val myCards: List<Card>,
    val myTicketsOnHand: List<Ticket>,
    val myPendingTicketsChoice: PendingTicketsChoice?
) {
    val myTurn: Boolean
        get() = players[turn].name == myName
}