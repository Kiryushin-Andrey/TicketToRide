package ticketToRide

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class OptionForCardsToDrop(val cards: List<Card>, val segmentColor: CardColor? = null) {
    fun hasSameCardsAs(another: OptionForCardsToDrop) =
            cards.size == another.cards.size && cardsCount.all { (card, count) ->
                another.cardsCount[card]?.let { it == count } ?: false
            }

    @Transient
    private val cardsCount = cards.groupingBy { it }.eachCount()
}

fun List<Card>.getOptionsForCardsToDrop(count: Int, color: CardColor?): List<OptionForCardsToDrop> {
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
    return (0..locoCount).flatMap { getOptionsForCars(it) }.map { OptionForCardsToDrop(it, color) }
}
