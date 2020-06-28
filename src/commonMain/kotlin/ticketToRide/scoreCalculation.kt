package ticketToRide

import graph.*
import kotlinx.collections.immutable.persistentMapOf

// build a weighted graph of all segments occupied by the player
fun PlayerView.getOccupiedSegmentsGraph(): Graph<String> {
    val segments = occupiedSegments.map { GraphSegment(it.from.value, it.to.value, it.length) }
    val vertices = segments.flatMap { listOf(it.from, it.to) }.distinct().toList()
    return vertices.map { city ->
        city to segments.mapNotNull {
            when {
                it.from == city -> it.to to it.weight
                it.to == city -> it.from to it.weight
                else -> null
            }
        }.let { persistentMapOf(*it.toTypedArray()) }
    }.let { persistentMapOf(*it.toTypedArray()) }
}

// calculate the list of tickets fulfilled by the player
fun PlayerView.getFulfilledTickets(
    tickets: List<Ticket>,
    allPlayers: List<PlayerView>,
    graph: Graph<String> = getOccupiedSegmentsGraph(),
    subgraphs: List<Graph<String>> = graph.splitIntoConnectedSubgraphs().toList()
): List<Ticket> {

    fun getFulfilledTickets(subgraphs: List<Graph<String>>) = tickets.filter { ticket ->
        subgraphs.any { it.containsKey(ticket.from.value) && it.containsKey(ticket.to.value) }
    }

    return placedStations
        .asSequence()
        // get adjacent occupied segments for each station
        .map { city ->
            allPlayers.filter { it != this }
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
        .map { getFulfilledTickets(it.splitIntoConnectedSubgraphs().toList()) }
        .maxBy { it.sumBy { it.points } }
        ?: getFulfilledTickets(subgraphs)
}