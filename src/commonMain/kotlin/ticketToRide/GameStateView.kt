package ticketToRide

import kotlinx.serialization.Serializable
import kotlin.math.min
import kotlin.random.Random

@Serializable
data class GameId(val value: String)

fun GameId.Companion.random() = GameId(Random.nextBytes(5).joinToString("") { it.toUByte().toString(16).padStart(2, '0') })

val GameId.webSocketUrl get() = "/game/$value/ws"

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
        fun random(map: GameMap): Card {
            val colors = CardColor.values()
            return map.segments.asSequence()
                .filter { it.color != null }
                .flatMap { segment -> sequence { repeat(segment.length) { yield(Car(segment.color!!)) } } }
                .drop(Random.nextInt(map.totalColoredSegmentsLength + map.totalColoredSegmentsLength / colors.size))
                .firstOrNull() ?: Loco
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
class GameStateView(
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