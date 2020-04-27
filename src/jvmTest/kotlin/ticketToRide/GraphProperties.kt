package ticketToRide

import graph.*
import io.kotest.core.spec.style.*
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.*
import io.kotest.matchers.*
import io.kotest.property.*
import io.kotest.property.arbitrary.*

class GraphProperties : FreeSpec({

    "removing path from graph" - {

        "leaves a smaller graph" {
            forAll(Arbs.graphWithVerticesPair) { (graph, path) ->
                graph.size >= graph.removePath(path.first, path.second, Distances(graph)).size
            }
        }

        "decreases source and target node degree by one" {
            val data = Arbs.graphWithVerticesPair.filter { (g, path) -> g.nodeDegree(path.first) > 1 && g.nodeDegree(path.second) > 1 }
            checkAll(PropTestConfig(-205964080536688018), data) { (graph, path) ->
                val originalDegrees = graph.nodeDegree(path.first) to graph.nodeDegree(path.second)
                graph.removePath(path.first, path.second, Distances(graph)).let {
                    it.nodeDegree(path.first) shouldBe originalDegrees.first - 1
                    it.nodeDegree(path.second) shouldBe originalDegrees.second - 1
                }
            }
        }

        "drops source and target nodes from the graph if they had a degree of one" {
            val data = Arbs.graphWithVerticesPair.filter { (g, path) -> g.nodeDegree(path.first) == 1 && g.nodeDegree(path.second) == 1 }
            checkAll(data) { (graph, path) ->
                graph.removePath(path.first, path.second, Distances(graph)).keys.let {
                    it shouldNotContain path.first
                    it shouldNotContain path.second
                }
            }
        }
    }

    "path from a to b" - {

        "starts with a" {
            forAll(Arbs.graphWithVerticesPair) { (graph, path) ->
                Distances(graph).getPath(path.first, path.second).first().from == path.first
            }
        }

        "ends with b" {
            forAll(Arbs.graphWithVerticesPair) { (graph, path) ->
                Distances(graph).getPath(path.first, path.second).last().to == path.second
            }
        }

        "connects nodes" {
            checkAll(Arbs.graphWithVerticesPair) { (graph, nodes) ->
                val path = Distances(graph).getPath(nodes.first, nodes.second)
                path.zipWithNext().forAll { (prev, next) -> prev.to == next.from }
            }
        }

        "passes every node only once" {
            checkAll(PropTestConfig(-3599909229822396415), Arbs.graphWithVerticesPair) { (graph, nodes) ->
                val path = Distances(graph).getPath(nodes.first, nodes.second)
                val allNodes = path.zipWithNext().map { it.first.from }
                allNodes.distinct().size shouldBe allNodes.size
            }
        }
    }
})

object Arbs {
    val graph: Arb<Graph<Int>> = arb { rs ->
        generateSequence {
            val size = Arb.int(4, 15).next(rs)
            (0 until size)
                .associateWith { mutableListOf<Pair<Int, Int>>() }
                .also { graph ->
                    for (from in (1 until size)) {
                        val degree = Arb.int(1 .. maxOf(1, from-2)).next(rs)
                        val edges = Arb.int(0 until from).take(degree).distinct()
                        for (to in edges) {
                            val weight = rs.random.nextInt(100)
                            graph[from]!!.add(to to weight)
                            graph[to]!!.add(from to weight)
                        }
                    }
                }
        }
    }

    val graphWithVerticesPair = arb { rs ->
        graph.values(rs).map {
            val from = Arb.int(0 until it.value.size).next(rs)
            val to = Arb.int(0 until it.value.size).filter { it != from }.next(rs)
            it.value to (from to to)
        }
    }
}