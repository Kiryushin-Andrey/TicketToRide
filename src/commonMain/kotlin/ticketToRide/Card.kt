package ticketToRide

import kotlinx.serialization.Serializable
import kotlin.random.Random

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

    val locoCount = kotlin.math.min(count, filterIsInstance<Card.Loco>().count())
    return (0..locoCount).flatMap { getOptionsForCars(it) }
}