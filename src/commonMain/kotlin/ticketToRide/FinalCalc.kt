package ticketToRide

import graph.*

class PlayerFinalStats(val playerView: PlayerView, tickets: List<Ticket>) {
    val name get() = playerView.name
    val color get() = playerView.color
    val carsLeft get() = playerView.carsLeft
    val occupiedSegments get() = playerView.occupiedSegments

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