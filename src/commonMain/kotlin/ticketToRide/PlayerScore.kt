package ticketToRide

import graph.*

const val InitialStationsCount = 3
const val PointsPerStation = 4

class PlayerScore(
    override val name: PlayerName,
    override val color: PlayerColor,
    val occupiedSegments: List<Segment>,
    val placedStations: List<CityName>,
    tickets: List<Ticket>,
    segmentsOccupiedByOtherPlayers: List<Segment>,
    private val map: GameMap
): PlayerId {
    private val fulfilledTicketsPoints get() = fulfilledTickets.sumBy { it.points }
    private val unfulfilledTicketPoints get() = unfulfilledTickets.sumBy { it.points }
    val stationsLeft get() = InitialStationsCount - placedStations.size
    val stationPoints = stationsLeft * PointsPerStation
    val segmentsPoints
        get() = occupiedSegments.groupingBy { it.length }.eachCount().entries
            .sumBy { (length, count) -> map.getPointsForSegments(length) * count }

    private fun getLongestPathPoints(longestPathOfAll: Int) =
        if (longestPathOfAll > 0 && longestRoute == longestPathOfAll) map.pointsForLongestRoute else 0

    fun getTotalPoints(longestPathOfAll: Int, gameInProgress: Boolean): Int {
        val result = fulfilledTicketsPoints + segmentsPoints + stationPoints + getLongestPathPoints(longestPathOfAll)
        return if (gameInProgress) result else result - unfulfilledTicketPoints;
    }


    val longestRoute: Int
    val fulfilledTickets: List<Ticket>
    val unfulfilledTickets: List<Ticket>

    init {
        val graph = buildSegmentsGraph(occupiedSegments)
        val subgraphs = graph.splitIntoConnectedSubgraphs().toList()

        fulfilledTickets = getFulfilledTickets(
            tickets,
            occupiedSegments,
            placedStations,
            segmentsOccupiedByOtherPlayers,
            graph,
            subgraphs
        )
        unfulfilledTickets = tickets - fulfilledTickets
        longestRoute = subgraphs
            .map { it.getMaxEulerianSubgraph().getTotalWeight() }
            .maxOrNull() ?: 0
    }
}