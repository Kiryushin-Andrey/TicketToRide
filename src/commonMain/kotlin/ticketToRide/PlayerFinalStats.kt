package ticketToRide

import graph.*
import kotlinx.collections.immutable.persistentMapOf

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
        val graph = vertices.map { city ->
            city to segments.mapNotNull {
                when {
                    it.from == city -> it.to to it.weight
                    it.to == city -> it.from to it.weight
                    else -> null
                }
            }.let { persistentMapOf(*it.toTypedArray()) }
        }.let { persistentMapOf(*it.toTypedArray()) }
        val subgraphs = graph.splitIntoConnectedSubgraphs()
        longestPath = subgraphs
            .map { it.getMaxEulerianSubgraph().getTotalWeight() }
            .max() ?: 0

        fun getFulfilledTickets(subgraphs: Sequence<Graph<String>>) = tickets.filter { ticket ->
            subgraphs.any { it.containsKey(ticket.from.value) && it.containsKey(ticket.to.value) }
        }

        fulfilledTickets = playerView.placedStations
            .asSequence()
            // get adjacent occupied segments for each station
            .map { city ->
                allPlayers.filter { it != playerView }
                    .flatMap { it.occupiedSegments.filter { s -> s.from == city || s.to == city } }
            }
            .filter { it.isNotEmpty() }
            // pick one segment per each of the stations and add it to the graph of the player's segments
            // thus build a new graph for each possible usage of each station placed on the map by this player
            // then pick one of these graphs having the best score regarding the tickets fulfilled
            .allCombinations()
            .map {
                it.fold(graph.builder()) { g, s -> g.apply { addEdge(s.from.value, s.to.value, s.length) } }.build()
            }
            .map { getFulfilledTickets(it.splitIntoConnectedSubgraphs()) }
            .maxBy { it.sumBy { it.points } }
            ?: getFulfilledTickets(subgraphs)

        unfulfilledTickets = tickets - fulfilledTickets
    }
}