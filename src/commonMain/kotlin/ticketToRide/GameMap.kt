package ticketToRide

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.jvm.JvmInline

@Serializable
data class LatLong(val lat: Double, val lng: Double)

@JvmInline
@Serializable
value class CityId(override val value: String): ICityId {
    override fun toString() = "City-$value"
}

@Serializable
data class City(val id: CityId, val locales: Map<Locale, String>, val latLng: LatLong)

@Serializable
class Segment constructor(val from: CityId, val to: CityId, val color: CardColor? = null, val length: Int) {
    override fun equals(other: Any?) =
        if (other is Segment)
            ((from == other.from && to == other.to) || (from == other.to && to == other.from))
                    && color == other.color && length == other.length
        else false

    override fun hashCode(): Int {
        var result =
            if (from.value < to.value) 31 * from.hashCode() + to.hashCode()
            else 31 * to.hashCode() + from.hashCode()
        result = 31 * result + (color?.hashCode() ?: 0)
        result = 31 * result + length.hashCode()
        return result
    }

    fun connects(cityId1: CityId, cityId2: CityId) =
        (from == cityId1 && to == cityId2) || (from == cityId2 && to == cityId1)
}

@Serializable
data class GameMap(
    val cities: List<City>,
    val segments: List<Segment>,
    val mapCenter: LatLong,
    val mapZoom: Int,
    val pointsForLongestRoute: Int = 10,
    val longTicketMinPoints: Int = 20,
    private val shortTicketsPointsRange: Pair<Int, Int> = 5 to 12
) {
    private val pointsForSegments =
        mapOf(1 to 1, 2 to 2, 3 to 4, 4 to 7, 5 to 12, 6 to 15, 7 to 18, 8 to 21)

    fun getPointsForSegments(length: Int) =
        pointsForSegments[length] ?: throw Error("Points for ${length}-length segments not defined")

    val totalSegmentsLength by lazy { segments.sumOf { it.length } }

    val totalColoredSegmentsLength by lazy { segments.filter { it.color != null }.sumOf { it.length } }

    val longTickets by lazy {
        allTickets.takeWhile { it.points >= longTicketMinPoints }
    }

    val shortTickets by lazy {
        allTickets.reversed()
            .dropWhile { it.points < shortTicketsPointsRange.first }
            .takeWhile { it.points <= shortTicketsPointsRange.second }
    }

    private val allTickets by lazy {
        val ixByCityName = cities.withIndex().associate { (ix, city) -> city.id to ix }
        fun ixByCityName(cityId: CityId) =
            ixByCityName[cityId] ?: throw Error("City $cityId not exists on game map")

        val citiesCount = cities.size
        val dist = Array(citiesCount) { IntArray(citiesCount) { Int.MAX_VALUE } }
        for (segment in segments) {
            val fromIx = ixByCityName(segment.from)
            val targetIx = ixByCityName(segment.to)
            dist[fromIx][targetIx] = segment.length
            dist[targetIx][fromIx] = segment.length
        }
        for (k in (0 until citiesCount))
            for (i in (0 until citiesCount))
                for (j in (0 until citiesCount))
                    if (dist[i][k] < Int.MAX_VALUE && dist[k][j] < Int.MAX_VALUE && dist[i][j] > dist[i][k] + dist[k][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j]
                    }


        cities.asSequence().flatMap { source ->
            cities.asSequence()
                .filter { dest ->
                    source != dest
                            && getSegmentsBetween(source.id, dest.id).isEmpty()
                            && ixByCityName(source.id) < ixByCityName(dest.id)
                }
                .map { dest ->
                    val distance = dist[ixByCityName(source.id)][ixByCityName(dest.id)]
                    Ticket(source.id, dest.id, distance)
                }
        }.toList().sortedByDescending { it.points }
    }

    private val citiesById by lazy { cities.associateBy { it.id } }

    fun getCityName(id: CityId, locale: Locale) =
        citiesById.getValue(id).let { it.locales[locale] ?: it.locales.getValue(Locale.values().first()) }

    @Transient
    private val segmentsByCities =
        segments.asSequence().flatMap { sequenceOf(it.from to it, it.to to it) }.groupBy({ it.first }) { it.second }

    @Transient
    private val segmentsSet = segments.toSet()

    fun getSegmentsBetween(from: CityId, to: CityId) =
        segmentsByCities[from]?.filter { it.connects(from, to) } ?: emptyList()

    fun segmentExists(segment: Segment) = segmentsSet.contains(segment)
}

fun CityId.localize(locale: Locale, map: GameMap) = map.getCityName(this, locale)