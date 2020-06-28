package ticketToRide

import graph.*

const val PointsPerStation = 4

class PlayerFinalStats(
    val playerView: PlayerView,
    tickets: List<Ticket>,
    allPlayers: List<PlayerView>,
    private val map: GameMap
) {
    val name get() = playerView.name
    val color get() = playerView.color
    val occupiedSegments get() = playerView.occupiedSegments

    val fulfilledTicketsPoints get() = fulfilledTickets.sumBy { it.points }
    val unfulfilledTicketPoints get() = unfulfilledTickets.sumBy { it.points }
    val stationPoints get() = playerView.stationsLeft * PointsPerStation
    val segmentsPoints
        get() = occupiedSegments.groupingBy { it.length }.eachCount().entries
            .sumBy { (length, count) -> map.getPointsForSegments(length) * count }

    fun getLongestPathPoints(longestPathOfAll: Int) =
        if (longestPath == longestPathOfAll) map.pointsForLongestRoute else 0

    fun getTotalPoints(longestPathOfAll: Int) =
        fulfilledTicketsPoints - unfulfilledTicketPoints + segmentsPoints + stationPoints + getLongestPathPoints(
            longestPathOfAll
        )


    val longestPath: Int
    val fulfilledTickets: List<Ticket>
    val unfulfilledTickets: List<Ticket>

    init {
        val graph = playerView.getOccupiedSegmentsGraph()
        val subgraphs = graph.splitIntoConnectedSubgraphs().toList()

        fulfilledTickets = playerView.getFulfilledTickets(tickets, allPlayers, graph, subgraphs)
        unfulfilledTickets = tickets - fulfilledTickets
        longestPath = subgraphs
            .map { it.getMaxEulerianSubgraph().getTotalWeight() }
            .max() ?: 0
    }
}