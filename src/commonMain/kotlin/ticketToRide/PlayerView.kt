package ticketToRide

import kotlinx.serialization.Serializable

@Serializable
data class PlayerName(val value: String)

interface PlayerId {
    val name: PlayerName
    val color: PlayerColor
}

@Serializable
data class PlayerView(
    override val name: PlayerName,
    override val color: PlayerColor,
    val points: Int?,
    val carsLeft: Int,
    val stationsLeft: Int,
    val cardsOnHand: Int,
    val ticketsOnHand: Int,
    val away: Boolean,
    val occupiedSegments: List<Segment>,
    val placedStations: List<CityName>,
    val pendingTicketsChoice: PendingTicketsChoiceState
) : PlayerId
