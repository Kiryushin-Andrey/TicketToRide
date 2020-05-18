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
        class Unknown(line: String, lineNumber: Int, val cityName: CityName) : City(line, lineNumber)
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
        val currentFrom: CityName? = null,
        private val cities: PersistentMap<CityName, City> = persistentMapOf(),
        private val segments: PersistentList<Segment> = persistentListOf(),
        val errors: PersistentList<GameMapParseError> = persistentListOf(
            GameMapParseError.MapCenter.Missing,
            GameMapParseError.MapZoom.Missing
        ),
        private val props: PersistentList<GameMap.() -> GameMap> = persistentListOf()
    ) {

        fun withProp(block: GameMap.() -> GameMap) = copy(props = props + block, currentFrom = null)

        fun withCity(city: City) = copy(
            currentFrom = city.name,
            cities = cities + (city.name to city)
        )

        fun withSegment(segment: Segment, line: String, lineNumber: Int) = copy(
            segments = segments + segment,
            errors = if (cities.containsKey(segment.to)) errors
            else (errors + GameMapParseError.City.Unknown(line, lineNumber, segment.to))
        )

        fun withError(err: GameMapParseError) = copy(errors = errors + err)

        fun Iterable<Segment>.color() = asSequence().let { segments ->
            val segmentsByCities =
                segments.flatMap { sequenceOf(it.from to it, it.to to it) }.groupBy({ it.first }) { it.second }

            val totalLength = segments.filter { it.length <= 4 }.sumBy { it.length }
            val countPerColor = ceil(totalLength.toDouble() / CardColor.values().size).toInt()
            val usedByColor = mutableMapOf<CardColor, Int>()

            val colorMap = mutableMapOf<Segment, CardColor?>()
            for (segment in segments.sortedByDescending { it.length }.dropWhile { it.length > 4 }) {

                val adjacentColors = sequenceOf(segment.from, segment.to)
                    .flatMap { segmentsByCities[it]?.asSequence() ?: throw Error("City ${it.value} not found in map") }
                    .mapNotNull { colorMap[it] }
                    .toSet()

                fun <T : Any> Sequence<Pair<T, Int>>.pickRandom(): T? {
                    fun <T : Any> Sequence<Pair<T, Int>>.pick(n: Int): T? =
                        firstOrNull()?.let { (v, i) -> if (n >= i) drop(1).pick(n - i) else v }
                    return sumBy { it.second }.takeIf { it > 0 }?.let {
                        pick(Random.nextInt(it - 1))
                    }
                }

                val next = CardColor.values().asSequence()
                    .map { it to (countPerColor - usedByColor.getOrElse(it, { 0 })) }
                    .filter { (color, n) -> !adjacentColors.contains(color) && n >= segment.length }
                    .pickRandom() ?: CardColor.values().random()
                colorMap[segment] = next
                usedByColor[next] = (usedByColor[next] ?: 0) + 1
            }

            segments.map { Segment(it.from, it.to, colorMap[it], it.length) }.toList()
        }

        fun build(): Try<GameMap, List<GameMapParseError>> =
            errors.filterNot { it is GameMapParseError.City.Unknown && cities.containsKey(it.cityName) }.let { errors ->
                if (errors.isEmpty() && mapCenter != null && mapZoom != null) {
                    val map = GameMap(
                        cities.values.toList(),
                        segments.color(),
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
                val lineNumber = ix + 2;
                when {
                    // blank line or comment - ignore
                    line.isEmptyOrComment() ->
                        this

                    // line starting with hyphen - should be route from the previously mentioned city
                    line.startsWith('-') -> {
                        if (currentFrom == null)
                            withError(
                                GameMapParseError.UnexpectedRoute(line, lineNumber)
                            )
                        else {
                            line.trimStart('-').split(';').map { it.trim() }.let {
                                val to = CityName(it[0])
                                val segmentLength = it.getOrNull(1)?.toIntOrNull()
                                if (it.size != 2 || segmentLength == null)
                                    withError(
                                        GameMapParseError.BadRouteFormat(line, lineNumber)
                                    )
                                else {
                                    withSegment(Segment(currentFrom, to, null, segmentLength), line, lineNumber)
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
                                if (it.size != 3)
                                    withError(
                                        GameMapParseError.City.BadFormat(line, lineNumber)
                                    )
                                else {
                                    val name = CityName(it[0])
                                    val latitude = it[1].tryParseDouble()
                                    val longitude = it[2].tryParseDouble()
                                    if (latitude == null || longitude == null)
                                        withError(GameMapParseError.City.InvalidLatLong(line, lineNumber))
                                    else {
                                        withCity(City(name, LatLong(latitude, longitude)))
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