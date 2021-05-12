package ticketToRide

import kotlinx.serialization.Serializable

@Serializable
data class Ticket(val from: CityId, val to: CityId, val points: Int) {
    fun sharesCityWith(another: Ticket) = listOf(from, to).intersect(listOf(another.from, another.to)).any()
}
