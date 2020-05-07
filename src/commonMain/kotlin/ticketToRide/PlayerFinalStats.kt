package ticketToRide

import graph.*

class PlayerFinalStats(val playerView: PlayerView, tickets: List<Ticket>, private val map: GameMap) {
    val name get() = playerView.name
    val color get() = playerView.color
    val carsLeft get() = playerView.carsLeft
    val occupiedSegments get() = playerView.occupiedSegments

    val fulfilledTicketsPoints get() = fulfilledTickets.sumBy { it.points }
    val unfulfilledTicketPoints get() = unfulfilledTickets.sumBy { it.points }
    val segmentsPoints
        get() = occupiedSegments.groupingBy { it.length }.eachCount().entries
            .sumBy { (length, count) -> map.getPointsForSegments(length) * count }

    fun getLongestPathPoints(longestPathOfAll: Int) =
        if (longestPath == longestPathOfAll) map.pointsForLongestPath else 0

    fun getTotalPoints(longestPathOfAll: Int) =
        fulfilledTicketsPoints - unfulfilledTicketPoints + segmentsPoints + getLongestPathPoints(longestPathOfAll)


    val longestPath: Int
    val fulfilledTickets: List<Ticket>
    val unfulfilledTickets: List<Ticket>

    init {
        val segments = playerView.occupiedSegments.map { GraphSegment(it.from.value, it.to.value, it.length) }
        val vertices = segments.flatMap { listOf(it.from, it.to) }.distinct().toList()
        val graph = vertices.associateWith { city ->
            segments.mapNotNull {
                when {
                    it.from == city -> it.to to it.weight
                    it.to == city -> it.from to it.weight
                    else -> null
                }
            }
        }
        val subgraphs = graph.splitIntoConnectedSubgraphs()

        fulfilledTickets =
            tickets.filter { t -> subgraphs.any { it.containsKey(t.from.value) && it.containsKey(t.to.value) } }
        unfulfilledTickets = tickets - fulfilledTickets

        longestPath = subgraphs
            .map { it.getMaxEulerianSubgraph().getTotalWeight() }
            .max() ?: 0
    }
}