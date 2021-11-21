package ticketToRide

import kotlinx.collections.immutable.*
import kotlin.math.ceil
import kotlin.random.Random

sealed class Try<out TSuccess, out TError> {
    data class Success<out TSuccess, out TError>(val value: TSuccess) : Try<TSuccess, TError>()
    data class Error<out TSuccess, out TError>(val errors: TError) : Try<TSuccess, TError>()
}

sealed class GameMapParseError(val line: String, val lineNumber: Int) {

    sealed class MapCenter(line: String, lineNumber: Int) : GameMapParseError(line, lineNumber) {
        object Missing : MapCenter("", -1)
        class BadFormat(line: String, lineNumber: Int) : MapCenter(line, lineNumber)
    }

    sealed class MapZoom(line: String, lineNumber: Int) : GameMapParseError(line, lineNumber) {
        object Missing : MapZoom("", -1)
        class BadFormat(line: String, lineNumber: Int) : MapZoom(line, lineNumber)
    }

    sealed class City(line: String, lineNumber: Int) : GameMapParseError(line, lineNumber) {
        class Unknown(line: String, lineNumber: Int, val cityId: CityId) : City(line, lineNumber)
        class BadFormat(line: String, lineNumber: Int) : City(line, lineNumber)
        class InvalidLatLong(line: String, lineNumber: Int) : City(line, lineNumber)
    }

    class BadRouteFormat(line: String, lineNumber: Int) : GameMapParseError(line, lineNumber)

    class UnexpectedRoute(line: String, lineNumber: Int) : GameMapParseError(line, lineNumber)

    class BadPropertyFormat(line: String, lineNumber: Int, val propName: String) : GameMapParseError(line, lineNumber)

    class UnknownProperty(line: String, lineNumber: Int, val propName: String) : GameMapParseError(line, lineNumber)
}

fun GameMap.Companion.parse(file: String): Try<GameMap, List<GameMapParseError>> {

    data class ParsingState(
        val mapCenter: LatLong? = null,
        val mapZoom: Int? = null,
        val currentFrom: CityId? = null,
        private val cities: PersistentMap<CityId, City> = persistentMapOf(),
        private val segments: PersistentList<Segment> = persistentListOf(),
        val errors: PersistentList<GameMapParseError> = persistentListOf(
            GameMapParseError.MapCenter.Missing,
            GameMapParseError.MapZoom.Missing
        ),
        private val props: PersistentList<GameMap.() -> GameMap> = persistentListOf()
    ) {

        fun withProp(block: GameMap.() -> GameMap) = copy(props = props + block, currentFrom = null)

        fun withCity(city: City) = copy(
            currentFrom = city.id,
            cities = cities + (city.id to city)
        )

        fun withSegment(to: CityId, count: Int, line: String, lineNumber: Int, createSegment: () -> Segment) = copy(
                segments = segments + List(count) { createSegment() },
                errors =
                if (cities.containsKey(to)) errors
                else (errors + GameMapParseError.City.Unknown(line, lineNumber, to))
        )

        fun withError(err: GameMapParseError) = copy(errors = errors + err)

        private fun <T : Any> Sequence<Pair<T, Int>>.pickRandom(): T? {
            fun <T : Any> Sequence<Pair<T, Int>>.pick(n: Int): T? =
                    firstOrNull()?.let { (v, i) -> if (n >= i) drop(1).pick(n - i) else v }
            return sumOf { it.second }.takeIf { it > 0 }?.let {
                pick(Random.nextInt(it - 1))
            }
        }

        private fun color(segments: List<Segment>): List<Segment> {
            val totalLength = segments.filter { it.length <= 4 }.sumOf { it.length }
            val countPerColor = ceil(totalLength.toDouble() / CardColor.values().size).toInt()
            val usedByColor = mutableMapOf<CardColor, Int>()

            val adjacentColorsByCity = segments
                    .flatMap { sequenceOf(it.from, it.to) }.distinct()
                    .associateWith { mutableListOf<CardColor>() }

            return segments.map { segment ->
                if (segment.length < 5) {
                    val adjacentColors = sequenceOf(segment.from, segment.to)
                            .flatMap { adjacentColorsByCity[it]!! }
                            .toSet()

                    val next = CardColor.values().asSequence()
                            .map { it to (countPerColor - usedByColor.getOrElse(it, { 0 })) }
                            .filter { (color, n) -> !adjacentColors.contains(color) && n >= segment.length }
                            .pickRandom() ?: CardColor.values().random()
                    adjacentColorsByCity[segment.from]!!.add(next)
                    adjacentColorsByCity[segment.to]!!.add(next)
                    usedByColor[next] = (usedByColor[next] ?: 0) + 1

                    Segment(segment.from, segment.to, next, segment.length)
                } else {
                    segment
                }
            }
        }

        fun build(): Try<GameMap, List<GameMapParseError>> =
            errors.filterNot { it is GameMapParseError.City.Unknown && cities.containsKey(it.cityId) }.let { errors ->
                if (errors.isEmpty() && mapCenter != null && mapZoom != null) {
                    val map = GameMap(
                        cities.values.toList(),
                        color(segments),
                        mapCenter,
                        mapZoom
                    )
                    Try.Success(props.fold(map) { m, block -> m.block() })
                } else
                    Try.Error(errors.toList())
            }
    }

    fun String.isEmptyOrComment() = let { it.isBlank() || it.startsWith("//") || it.startsWith("--") }

    fun String.tryParseDouble() = replace(',', '.').toDoubleOrNull() ?: replace('.', ',').toDoubleOrNull()

    return file.splitToSequence('\n').map { it.trim() }
        .foldIndexed(ParsingState()) { ix, acc, line ->
            acc.run {
                val lineNumber = ix + 2
                when {
                    // blank line or comment - ignore
                    line.isEmptyOrComment() ->
                        this

                    // line starting with vertical bar - should be route from the previously mentioned city
                    line.startsWith('|') -> {
                        if (currentFrom == null)
                            withError(
                                GameMapParseError.UnexpectedRoute(line, lineNumber)
                            )
                        else {
                            line.trimStart('|').split(';').map { it.trim() }.let {
                                if (it.size != 2) {
                                    withError(GameMapParseError.BadRouteFormat(line, lineNumber))
                                } else {
                                    val to = CityId(it[0])
                                    val segmentLength = it.getOrNull(1)?.toIntOrNull()
                                    if (segmentLength == null)
                                        withError(GameMapParseError.BadRouteFormat(line, lineNumber))
                                    else {
                                        val segmentsCount = line.indexOfFirst { it != '|' }
                                        withSegment(to, segmentsCount, line, lineNumber) {
                                            Segment(currentFrom, to, null, segmentLength)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // line containing a semicolon - one of the game-wide properties
                    line.contains(':') -> {
                        line.split(':').map { it.trim() }.let { lineParts ->
                            if (lineParts.size != 2)
                                withError(GameMapParseError.BadPropertyFormat(line, lineNumber, lineParts[0]))
                            else {
                                val prop = lineParts[0]
                                val value = lineParts[1]
                                when (prop) {
                                    GameMapPropertyNames.mapCenter -> {
                                        value.split(' ').takeIf { it.size == 2 }
                                            ?.let { it[0].tryParseDouble() to it[1].tryParseDouble() }
                                            ?.takeIf { it.first != null && it.second != null }
                                            ?.let { (latitude, longitude) ->
                                                copy(
                                                    mapCenter = LatLong(
                                                        latitude!!,
                                                        longitude!!
                                                    ),
                                                    errors = errors - GameMapParseError.MapCenter.Missing
                                                )
                                            } ?: copy(
                                            errors = errors
                                                    - GameMapParseError.MapCenter.Missing
                                                    + GameMapParseError.MapCenter.BadFormat(line, lineNumber)
                                        )
                                    }

                                    GameMapPropertyNames.mapZoom -> {
                                        val mapZoom = value.toIntOrNull()
                                        if (mapZoom != null && mapZoom >= 3 && mapZoom <= 10)
                                            copy(mapZoom = mapZoom, errors = errors - GameMapParseError.MapZoom.Missing)
                                        else
                                            copy(
                                                errors = errors
                                                        - GameMapParseError.MapZoom.Missing
                                                        + GameMapParseError.MapZoom.BadFormat(line, lineNumber)
                                            )
                                    }

                                    GameMapPropertyNames.pointsForLongestRoute -> {
                                        withProp { copy(pointsForLongestRoute = value.toInt()) }
                                    }

                                    GameMapPropertyNames.minPointsForLongTickets -> {
                                        withProp { copy(longTicketMinPoints = value.toInt()) }
                                    }

                                    GameMapPropertyNames.pointsRangeForShortTickets -> {
                                        val range =
                                            value.split('-').map { it.trim() }.let { it[0].toInt() to it[1].toInt() }
                                        withProp { copy(shortTicketsPointsRange = range) }
                                    }

                                    else ->
                                        withError(GameMapParseError.UnknownProperty(line, lineNumber, prop))
                                }
                            }
                        }
                    }

                    // line starting with text - city
                    else -> {
                        line.split(';').map { it.trim() }
                            .let {
                                if (it.size < 3)
                                    withError(
                                        GameMapParseError.City.BadFormat(line, lineNumber)
                                    )
                                else {
                                    val name = CityId(it[0])
                                    val locales = Locale.values().take(it.size - 2)
                                        .mapIndexed { ix, locale -> locale to it[ix] }
                                        .associate { it }
                                    val latitude = it[it.size - 2].tryParseDouble()
                                    val longitude = it[it.size - 1].tryParseDouble()
                                    if (latitude == null || longitude == null)
                                        withError(GameMapParseError.City.InvalidLatLong(line, lineNumber))
                                    else {
                                        withCity(City(name, locales, LatLong(latitude, longitude)))
                                    }
                                }
                            }

                    }
                }
            }
        }.build()
}

object GameMapPropertyNames {
    const val mapCenter = "map-center"
    const val mapZoom = "map-zoom"
    const val pointsForLongestRoute = "points-for-longest-route"
    const val minPointsForLongTickets = "long-ticket-min-points"
    const val pointsRangeForShortTickets = "short-tickets-points"

    val all = listOf(mapCenter, mapZoom, pointsForLongestRoute, minPointsForLongTickets, pointsRangeForShortTickets)
}