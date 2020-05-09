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
    val carsLeft get() = playerView.carsLeft
    val occupiedSegments get() = playerView.occupiedSegments

    val fulfilledTicketsPoints get() = fulfilledTickets.sumBy { it.points }
    val unfulfilledTicketPoints get() = unfulfilledTickets.sumBy { it.points }
    val stationPoints get() = playerView.stationsLeft * PointsPerStation
    val segmentsPoints
        get() = occupiedSegments.groupingBy { it.length }.eachCount().entries
            .sumBy { (length, count) -> map.getPointsForSegments(length) * count }

    fun getLongestPathPoints(longestPathOfAll: Int) =
        if (longestPath == longestPathOfAll) map.pointsForLongestPath else 0

    fun getTotalPoints(longestPathOfAll: Int) =
        fulfilledTicketsPoints - unfulfilledTicketPoints + segmentsPoints + stationPoints + getLongestPathPoints(
            longestPathOfAll
        )


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
        longestPath = subgraphs
            .map { it.getMaxEulerianSubgraph().getTotalWeight() }
            .max() ?: 0

        fun getFulfilledTickets(subgraphs: Sequence<Graph<String>>) = tickets.filter { ticket ->
            subgraphs.any { it.containsKey(ticket.from.value) && it.containsKey(ticket.to.value) }
        }

        fulfilledTickets = playerView.placedStations
            .asSequence()
            .map { city ->
                // get adjacent occupied segments for each station
                allPlayers.filter { it != playerView }
                    .flatMap { it.occupiedSegments.filter { s -> s.from == city || s.to == city } }
            }
            .filter { it.isNotEmpty() }
            .allCombinations()
            .map { it.fold(graph) { g, s -> g.withEdge(s.from.value, s.to.value, s.length) } }
            .map { getFulfilledTickets(it.splitIntoConnectedSubgraphs()) }
            .maxBy { it.sumBy { it.points } }
            ?: getFulfilledTickets(subgraphs)

        unfulfilledTickets = tickets - fulfilledTickets
    }
}