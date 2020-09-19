package ticketToRide

import kotlinx.serialization.Serializable

@Serializable
data class PlayerName(val value: String) {
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
    val placedStations: List<CityName> = emptyList(),
    val pendingTicketsChoice: PendingTicketsChoiceState
) : PlayerId
