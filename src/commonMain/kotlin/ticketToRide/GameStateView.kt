package ticketToRide

import kotlinx.serialization.Serializable
import kotlin.math.min
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
    }
}

fun List<Card>.getOptionsForCardsToDrop(count: Int, color: CardColor?): List<List<Card>> {
    val countByCar = filterIsInstance<Card.Car>().groupingBy { it }.eachCount()
    fun getOptionsForCars(locoCount: Int) =
        if (locoCount == count)
            listOf(List(locoCount) { Card.Loco })
        else countByCar
            .filter {
                it.value >= count - locoCount && (color == null || it.key.color == color)
            }
            .map { (car, _) ->
                val carsCount = count - locoCount
                if (locoCount > 0) List(locoCount) { Card.Loco } + List(carsCount) { car }
                else List(carsCount) { car }
            }

    val locoCount = min(count, filterIsInstance<Card.Loco>().count())
    return (0..locoCount).flatMap { getOptionsForCars(it) }
}

@Serializable
class Segment constructor(val from: CityName, val to: CityName, val color: CardColor?, val length: Int) {
    override fun equals(other: Any?) =
        if (other is Segment)
            ((from == other.from && to == other.to) || (from == other.to && to == other.from))
                    && color == other.color && length == other.length
        else false

    override fun hashCode(): Int {
        var result =
            if (from.value < to.value) 31 * from.hashCode() + to.hashCode()
            else 31 * to.hashCode() + from.hashCode()
        result = 31 * result + (color?.hashCode() ?: 0)
        result = 31 * result + length.hashCode()
        return result
    }
}

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
    val stationsLeft: Int,
    val cardsOnHand: Int,
    val ticketsOnHand: Int,
    val away: Boolean,
    val occupiedSegments: List<Segment>,
    val placedStations: List<CityName>,
    val pendingTicketsChoice: PendingTicketsChoiceState
)

fun List<PlayerView>.getStations() = flatMap { p -> p.placedStations.map { it to p } }.associate { it }

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