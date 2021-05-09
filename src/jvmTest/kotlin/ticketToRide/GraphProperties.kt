package ticketToRide

import graph.*
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.*
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.*
import io.kotest.matchers.*
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.maps.*
import io.kotest.property.*
import io.kotest.property.arbitrary.*
import kotlinx.collections.immutable.*

class GraphProperties : FreeSpec({

    "removing path from graph" - {

        "leaves a smaller graph" {
            forAll(Arb.graphWithPairOfVertices) { (graph, path) ->
                graph.size >= graph.removePath(path.first, path.second, Distances(graph)).size
            }
        }

        "decreases source and target node degree by one" {
            val data =
                Arb.graphWithPairOfVertices.filter { (g, path) -> g.nodeDegree(path.first) > 1 && g.nodeDegree(path.second) > 1 }
            checkAll(data) { (graph, path) ->
                val originalDegrees = graph.nodeDegree(path.first) to graph.nodeDegree(path.second)
                graph.removePath(path.first, path.second, Distances(graph)).let {
                    it.nodeDegree(path.first) shouldBe originalDegrees.first - 1
                    it.nodeDegree(path.second) shouldBe originalDegrees.second - 1
                }
            }
        }

        "decreases the total graph weight" {
            forAll(Arb.graphWithPairOfVertices) { (g, path) ->
                g.getTotalWeight() > g.removePath(path.first, path.second, Distances(g)).getTotalWeight()
            }
        }

        "decreases the total number of edges" {
            forAll(Arb.graphWithPairOfVertices) { (g, path) ->
                g.map { it.value.size }.sum() > g.removePath(path.first, path.second, Distances(g))
                    .map { it.value.size }.sum()
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
                path.zipWithNext().forAll { (prev, next) ->
                    prev.to shouldBeExactly next.from
                }
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
                it.size == it.splitIntoConnectedSubgraphs().sumOf { it.size }
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
            checkAll(Arb.connectedGraph) {
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

    "adding segment to graph" - {

        "leaves it with an edge between two nodes with correct weight" {
            checkAll(
                Arb.connectedGraph,
                Arb.pair(Arb.int(0..100), Arb.int(0..100)).filter { (from, to) -> from != to },
                Arb.int(1..1000)
            ) { graph, (from, to), weight ->
                graph.withEdge(from, to, weight).let {
                    it.edgeWeight(from, to) shouldBe weight
                    it.edgeWeight(to, from) shouldBe weight
                }
            }
        }

        "makes no changes when applied twice" {
            checkAll(
                Arb.connectedGraph,
                Arb.int(0..100),
                Arb.int(0..100),
                Arb.int(1..1000)
            ) { graph, from, to, weight ->
                val first = graph.withEdge(from, to, weight)
                val second = first.withEdge(from, to, weight)
                first shouldContainExactly second
            }
        }
    }

    "generating all combinations" - {

        "each of the combinations contains items from every source list" {
            checkAll(Arb.list(Arb.list(Arb.int(), 1..10), 1..5)) { lists ->
                lists.asSequence().allCombinations().forEach { c ->
                    lists.forEach { c shouldContainAnyOf it }
                }
            }
        }

        "every source list has its item contained in each of the combinations" {
            checkAll(Arb.list(Arb.list(Arb.int(), 1..10), 1..5)) { lists ->
                val combinations = lists.asSequence().allCombinations().toList()
                lists.forEach { list ->
                    combinations.forEach { it shouldContainAnyOf list }
                }
            }
        }
    }
})

val Arb.Companion.connectedGraph: Arb<Graph<Int>>
    get() = arbitrary { rs ->
        val size = Arb.int(6..30).next(rs)
        (0 until size)
            .associateWith { persistentMapOf<Int, Int>().builder() }
            .also { graph ->
                for (from in (1 until size)) {
                    val degree = Arb.int(1..maxOf(1, from / 2)).next(rs)
                    val edges = Arb.int(0 until from).take(degree).distinct()
                    for (to in edges) {
                        val weight = Arb.int(1..100).next(rs)
                        graph.getValue(from)[to] = weight
                        graph.getValue(to)[from] = weight
                    }
                }
            }
            .mapValues { (_, v) -> v.build() }
            .toPersistentMap()
    }

val Arb.Companion.disconnectedGraph: Arb<Graph<Int>>
    get() = arbitrary { rs ->
        val graphs = Arb.connectedGraph.take(Arb.int(2..6).next(rs), rs).toList()
        graphs.drop(1).fold(graphs[0].toMutableMap()) { result, next ->
            val size = result.size
            for ((from, edges) in next) {
                result[from + size] = edges.entries.associate { (t, w) -> (t + size) to w }.toPersistentMap()
            }
            result
        }.toPersistentMap()
    }

val Arb.Companion.graphWithPairOfVertices
    get() = arbitrary { rs ->
        val graph = connectedGraph.sample(rs)
        val from = Arb.int(0 until graph.value.size).next(rs)
        val to = Arb.int(0 until graph.value.size).filter { it != from }.next(rs)
        graph.value to (from to to)
    }

val Arb.Companion.graphWithSubgraph: Arb<Pair<Graph<Int>, Graph<Int>>>
    get() = arbitrary { rs ->
        connectedGraph.sample(rs).let { graph ->
            graph.value to graph.value.mutate { g ->
                val subgraphSize = Arb.int(4 until graph.value.size).next(rs)
                g.keys.removeIf { it >= subgraphSize }
                g.replaceAll { _, v -> v.mutate { it.keys.removeIf { it >= subgraphSize } } }
            }
        }
    }