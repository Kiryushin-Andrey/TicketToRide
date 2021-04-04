package ticketToRide

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
sealed class Card {

    @Serializable
    @SerialName("car")
    data class Car(val color: CardColor) : Card()

    @Serializable
    @SerialName("loco")
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
