package ticketToRide

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class PlayerName(val value: String) {
    override fun toString() = "player $value"
}

interface PlayerId {
    val name: PlayerName
    val color: PlayerColor
}

@Serializable
data class PlayerView(
    override val name: PlayerName,
    override val color: PlayerColor,
    val points: Int? = null,
    val carsLeft: Int,
    val stationsLeft: Int,
    val cardsOnHand: Int,
    val ticketsOnHand: Int,
    val away: Boolean,
    val occupiedSegments: List<Segment> = emptyList(),
    val placedStations: List<CityId> = emptyList(),
    val pendingTicketsChoice: PendingTicketsChoiceState
) : PlayerId

fun List<PlayerView>.getNotUsedColors(playerName: String) =
    find { it.name.value == playerName }
        ?.let {
            // if a player is reconnecting back into the game under the same name
            // she should keep the same color as before
            listOf(it.color)
        }
        ?: PlayerColor.entries.filter { c -> !map { it.color }.contains(c) }
