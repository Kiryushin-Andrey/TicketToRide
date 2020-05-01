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
    data class Car(val color: CardColor) : Card()

    @Serializable
    object Loco : Card()

    companion object {
        fun random(): Card {
            val colors = CardColor.values()
            val value = Random.nextInt(colors.size + 1)
            return if (value < colors.size) Car(colors[value]) else Loco
        }

        fun randomNoLoco() = Car(CardColor.values().random())
    }
}

@Serializable
data class Segment(val from: CityName, val to: CityName, val color: CardColor?, val length: Int)

fun Segment.connects(cityName1: String, cityName2: String) =
    (from.value == cityName1 && to.value == cityName2) || (from.value == cityName2 && to.value == cityName1)

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
    val color: PlayerColor,
    val carsLeft: Int,
    val cardsOnHand: Int,
    val ticketsOnHand: Int,
    val away: Boolean,
    val occupiedSegments: List<Segment>,
    val pendingTicketsChoice: PendingTicketsChoiceState
)

@Serializable
data class GameStateView(
    val players: List<PlayerView>,
    val openCards: List<Card>,
    val turn: Int,
    val lastRound: Boolean,
    val myName: PlayerName,
    val myCards: List<Card>,
    val myTicketsOnHand: List<Ticket>,
    val myPendingTicketsChoice: PendingTicketsChoice?
) {
    val me get() = players.find { it.name == myName }!!
    val myTurn get() = players[turn].name == myName
}