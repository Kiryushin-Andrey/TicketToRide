package ticketToRide

import graph.*
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.*
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.*
import io.kotest.matchers.*
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldNotBeEmpty
import io.kotest.property.*
import io.kotest.property.arbitrary.*

class GraphProperties : FreeSpec({

    "removing path from graph" - {

        "leaves a smaller graph" {
            forAll(Arb.graphWithPairOfVertices) { (graph, path) ->
                graph.size >= graph.removePath(path.first, path.second, Distances(graph)).size
            }
        }

        "decreases source and target node degree by one" {
            val data = Arb.graphWithPairOfVertices.filter { (g, path) -> g.nodeDegree(path.first) > 1 && g.nodeDegree(path.second) > 1 }
            checkAll(data) { (graph, path) ->
                val originalDegrees = graph.nodeDegree(path.first) to graph.nodeDegree(path.second)
                graph.removePath(path.first, path.second, Distances(graph)).let {
                    it.nodeDegree(path.first) shouldBe originalDegrees.first - 1
                    it.nodeDegree(path.second) shouldBe originalDegrees.second - 1
                }
            }
        }

        "decreases the total graph weight" {
            forAll(PropTestConfig(5073329641730822048), Arb.graphWithPairOfVertices) { (g, path) ->
                g.getTotalWeight() > g.removePath(path.first, path.second, Distances(g)).getTotalWeight()
            }
        }

        "decreases the total number of edges" {
            forAll(Arb.graphWithPairOfVertices) { (g, path) ->
                g.map { it.value.size }.sum() > g.removePath(path.first, path.second, Distances(g)).map { it.value.size }.sum()
            }
        }

        "drops all nodes on path that do not have any edges after removal" {
            forAll(Arb.graphWithPairOfVertices) { (graph, path) ->
                graph.removePath(path.first, path.second, Distances(graph)).filterValues { it.isEmpty() }.isEmpty()
            }
        }
    }

    "path from a to b" - {

        "starts with a" {
            forAll(Arb.graphWithPairOfVertices) { (graph, path) ->
                Distances(graph).getPath(path.first, path.second).first().from == path.first
            }
        }

        "ends with b" {
            forAll(Arb.graphWithPairOfVertices) { (graph, path) ->
                Distances(graph).getPath(path.first, path.second).last().to == path.second
            }
        }

        "connects nodes" {
            checkAll(Arb.graphWithPairOfVertices) { (graph, nodes) ->
                val path = Distances(graph).getPath(nodes.first, nodes.second)
                path.zipWithNext().forAll { (prev, next) -> prev.to == next.from }
            }
        }

        "passes every node only once" {
            checkAll(Arb.graphWithPairOfVertices) { (graph, nodes) ->
                val path = Distances(graph).getPath(nodes.first, nodes.second)
                val allNodes = path.zipWithNext().map { it.first.from }
                allNodes.distinct().size shouldBe allNodes.size
            }
        }
    }

    "subgraphs of a disconnected graph" - {

        "have a total size equal to the size of original graph" {
            forAll(Arb.disconnectedGraph) {
                it.size == it.splitIntoConnectedSubgraphs().sumBy { it.size }
            }
        }

        "are themselves connected" {
            checkAll(Arb.disconnectedGraph) {
                it.splitIntoConnectedSubgraphs().forAll { it.isConnected() }
            }
        }
    }

    "maximum eulerian subgraph" - {

        "is eulerian" {
            checkAll(PropTestConfig(-8638342389302181338), Arb.connectedGraph) {
                shouldNotThrowAny {
                    it.getMaxEulerianSubgraph().isEulerian()
                }
            }
        }

        "is larger than or equal to any other eulerian subgraph" {
            forAll(Arb.graphWithSubgraph.filter { it.second.isEulerian() }) { (graph, subgraph) ->
                graph.getMaxEulerianSubgraph().getTotalWeight() >= subgraph.getTotalWeight()
            }
        }
    }
})

val Arb.Companion.connectedGraph: Arb<Graph<Int>>
    get() = arb { rs ->
        generateSequence {
            val size = Arb.int(6..30).next(rs)
            (0 until size)
                .associateWith { mutableListOf<Pair<Int, Int>>() }
                .also { graph ->
                    for (from in (1 until size)) {
                        val degree = Arb.int(1..maxOf(1, from / 2)).next(rs)
                        val edges = Arb.int(0 until from).take(degree).distinct()
                        for (to in edges) {
                            val weight = Arb.int(1..100).next(rs)
                            graph[from]!!.add(to to weight)
                            graph[to]!!.add(from to weight)
                        }
                    }
                }
        }
    }

val Arb.Companion.disconnectedGraph: Arb<Graph<Int>>
    get() = arb { rs ->
        generateSequence {
            val graphs = Arb.connectedGraph.take(Arb.int(2..6).next(rs), rs).toList()
            graphs.drop(1).fold(graphs[0].toMutableMap()) { result, next ->
                val size = result.size
                for ((from, edge) in next) {
                    result[from + size] = edge.map { (t, w) -> (t + size) to w }
                }
                result
            }
        }
    }

val Arb.Companion.graphWithPairOfVertices
    get() = arb { rs ->
        connectedGraph.values(rs).map {
            val from = Arb.int(0 until it.value.size).next(rs)
            val to = Arb.int(0 until it.value.size).filter { it != from }.next(rs)
            it.value to (from to to)
        }
    }

val Arb.Companion.graphWithSubgraph: Arb<Pair<Graph<Int>, Graph<Int>>>
    get() = arb { rs ->
        connectedGraph.values(rs).map { graph ->
            val subgraphSize = Arb.int(4 until graph.value.size).next(rs)
            val subgraph = graph.value.entries.asSequence()
                .filter { (from, _) -> from < subgraphSize }
                .map { (from, edges) -> from to edges.filter { (to, _) -> to < subgraphSize } }
                .associate { it }
            graph.value to subgraph
        }
    }