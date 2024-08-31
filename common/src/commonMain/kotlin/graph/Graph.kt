package graph

import kotlinx.collections.immutable.*

typealias Graph<T> = PersistentMap<T, PersistentMap<T, Int>>
typealias MutableGraph<T> = MutableMap<T, PersistentMap<T, Int>>

class GraphSegment<T>(val from: T, val to: T, val weight: Int)

class Distances<T>(graph: Graph<T>) {
    private val entries = graph.entries.toImmutableList()
    private val vertices = entries.map { it.key }.toImmutableList()
    private val size = graph.size

    private val dist = Array(vertices.size) { IntArray(vertices.size) { Int.MAX_VALUE } }
    private val prev = Array(vertices.size) { IntArray(vertices.size) { -1 } }

    private val ixByNodeValue by lazy { vertices.withIndex().associate { (ix, v) -> v to ix } }
    private fun ixByNodeValue(v: T) = ixByNodeValue[v] ?: error("Node $v not found in graph")

    fun getDist(a: T, b: T) = dist[ixByNodeValue(a)][ixByNodeValue(b)]

    fun getPath(a: T, b: T): List<GraphSegment<T>> {
        val y = ixByNodeValue(b)
        val vertices = generateSequence(ixByNodeValue(a)) { k ->
            prev[k][y].let {
                when {
                    it == -1 -> null
                    k == y -> null
                    else -> it
                }
            }
        }
        return vertices.zipWithNext()
            .map { (x, y) -> GraphSegment(this.vertices[x], this.vertices[y], dist[x][y]) }
            .toList()
    }

    init {
        for ((x, node) in entries.withIndex()) {
            dist[x][x] = 0
            prev[x][x] = x
            for ((dest, weight) in node.value) {
                val y = ixByNodeValue(dest)
                dist[x][y] = weight
                dist[y][x] = weight
                prev[x][y] = y
                prev[y][x] = x
            }
        }

        for (k in (0 until size)) {
            for (i in (0 until size)) {
                for (j in (i + 1 until size)) {
                    if (dist[i][k] < Int.MAX_VALUE && dist[k][j] < Int.MAX_VALUE && dist[i][j] > dist[i][k] + dist[k][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j]
                        dist[j][i] = dist[i][j]
                        prev[i][j] = prev[i][k]
                        prev[j][i] = prev[j][k]
                    }
                }
            }
        }
    }
}

fun <T> MutableGraph<T>.addEdge(from: T, to: T, weight: Int) {
    this[from] = this[from]?.mutate { it[to] = weight } ?: persistentMapOf(to to weight)
    this[to] = this[to]?.mutate { it[from] = weight } ?: persistentMapOf(from to weight)
}

fun <T> Graph<T>.withEdge(from: T, to: T, weight: Int): Graph<T> = mutate { it.addEdge(from, to, weight) }

fun <T> Graph<T>.edges(v: T) = this[v] ?: throw Error("Node ${v} not found in graph")

fun <T> Graph<T>.edgeWeight(from: T, to: T) = this[from]?.let { it[to] }

fun <T> Graph<T>.nodeDegree(v: T) = edges(v).size

fun <T> Graph<T>.getFirstSubgraph(): Graph<T> {
    val queue = ArrayDeque<T>()
    queue.addFirst(keys.firstOrNull() ?: return this)
    val graph = this
    return persistentMapOf<T, PersistentMap<T, Int>>().mutate { map ->
        while (queue.size > 0) {
            val vertex = queue.removeLast()
            val edges = graph.edges(vertex)
            map[vertex] = edges
            edges.map { it.key }.filter { !map.containsKey(it) }.forEach { queue.addFirst(it) }
        }
    }
}

fun <T> Graph<T>.isConnected() = size == getFirstSubgraph().size

fun <T> Graph<T>.splitIntoConnectedSubgraphs(): Sequence<Graph<T>> =
    if (isEmpty()) emptySequence()
    else this.let {
        sequence {
            val subGraph = it.getFirstSubgraph()
            yield(subGraph)
            if (subGraph.size < it.size) {
                val another = it - subGraph.map { it.key }
                yieldAll(another.splitIntoConnectedSubgraphs())
            }
        }
    }

fun <T> Graph<T>.isEulerian() = values.count { it.size % 2 == 1 }.let { it == 0 || it == 2 }

fun <T> MutableMap<T, PersistentMap<T, Int>>.removeEdge(from: T, to: T) {
    (this[from]!! - to).takeIf { it.isNotEmpty() }?.let { this[from] = it } ?: this.remove(from)
    (this[to]!! - from).takeIf { it.isNotEmpty() }?.let { this[to] = it } ?: this.remove(to)
}

fun <T> Graph<T>.removePath(from: T, to: T, distances: Distances<T>): Pair<Graph<T>, List<GraphSegment<T>>> {
    val path = distances.getPath(from, to)
    val newGraph = mutate { graph ->
        path.forEach { s -> graph.removeEdge(s.from, s.to) }
    }
    return newGraph to path
}

fun <T> Graph<T>.getMaxEulerianSubgraph(): Graph<T> =
    if (isEulerian()) this
    else {
        val distances = Distances(this)
        val oddDegreeVertices = entries.filter { it.value.size % 2 == 1 }.map { it.key }
        val pathsToRemove = oddDegreeVertices.asSequence()
            .flatMap { a ->
                oddDegreeVertices.asSequence().map { b -> GraphSegment(a, b, distances.getDist(a, b)) }
            }
            .filter { s -> s.from != s.to }
            .sortedBy { s -> s.weight }
            .toList()

        val allCombinations = pathsToRemove.asSequence()
            .map { it.from to it.to }
            .allCombinations()

        allCombinations
            .map { combination ->
                combination.fold(this to emptyList<GraphSegment<T>>()) { (graph, removedPaths), (from, to) ->
                    val (newGraph, path) = graph.removePath(from, to, distances)
                    newGraph to removedPaths + path
                }
            }
            .filter { (graph, _) -> graph.isConnected() }
            .maxByOrNull { (_, removedPaths) -> removedPaths.sumOf { it.weight } }
            ?.first
            ?.getMaxEulerianSubgraph()
            ?: this
    }

fun <T> Graph<T>.getTotalWeight() = flatMap { it.value.values }.sum() / 2

// returns all possible combinations of unique elements
// generated by picking one item from each of the lists
fun <T> Sequence<List<T>>.allCombinations(): Sequence<Set<T>> =
    filter { it.isNotEmpty() }.fold(sequenceOf(emptySet())) { sets, list ->
        sets.flatMap { prev -> list.asSequence().map { prev + it } }
    }
