package graph

typealias Edge<T> = Pair<T, Int>
typealias Graph<T> = Map<T, List<Edge<T>>>

class GraphSegment<T>(val from: T, val to: T, val weight: Int) {
    fun covers(a: T, b: T) = (from == a && to == b) || (from == b && to == a)
}

class Distances<T>(graph: Graph<T>) {
    private val entries = graph.entries.toList()

    private val size = graph.size
    private val vertices = entries.map { it.key }
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

fun <T> Graph<T>.edges(v: T) = this[v] ?: throw Error("Node ${v} not found in graph")

fun <T> Graph<T>.nodeDegree(v: T) = edges(v).size

@OptIn(ExperimentalStdlibApi::class)
fun <T> Graph<T>.getFirstSubgraph(): Graph<T> {
    val queue = ArrayDeque<T>()
    queue.addFirst(keys.firstOrNull() ?: return this)
    val graph = this
    return mutableMapOf<T, List<Edge<T>>>().apply {
        while (queue.size > 0) {
            val vertex = queue.removeLast()
            val edges = graph.edges(vertex)
            this[vertex] = edges
            edges.map { it.first }.filter { !this.containsKey(it) }.forEach { queue.addFirst(it) }
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

fun <T> Graph<T>.removePath(from: T, to: T, distances: Distances<T>) =
    distances.getPath(from, to).let { segments ->
        mapValues { (a, edges) -> edges.filter { (b, _) -> !segments.any { it.covers(a, b) } } }
            .filterNot { it.value.isEmpty() }
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

        pathsToRemove
            .map { removePath(it.from, it.to, distances) }
            .first { it.isConnected() }
            .getMaxEulerianSubgraph()
    }

fun <T> Graph<T>.getTotalWeight() = flatMap { it.value }.sumBy { (_, points) -> points } / 2
