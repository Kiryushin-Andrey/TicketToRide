package ticketToRide

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.collections.immutable.*

@Serializable
data class LatLong(val lat: Double, val lng: Double)

@Serializable
data class CityName(val value: String)

@Serializable
data class City(val name: CityName, val latLng: LatLong)

@Serializable
class Segment constructor(val from: CityName, val to: CityName, val color: CardColor?, val length: Int) {
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

    fun connects(cityName1: CityName, cityName2: CityName) =
        (from == cityName1 && to == cityName2) || (from == cityName2 && to == cityName1)
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

    val totalSegmentsLength by lazy { segments.sumBy { it.length } }

    val totalColoredSegmentsLength by lazy { segments.filter { it.color != null }.sumBy { it.length } }

    val longTickets by lazy {
        allTickets.takeWhile { it.points >= longTicketMinPoints }
    }

    val shortTickets by lazy {
        allTickets.reversed()
            .dropWhile { it.points < shortTicketsPointsRange.first }
            .takeWhile { it.points <= shortTicketsPointsRange.second }
    }

    private val allTickets by lazy {
        val ixByCityName = cities.withIndex().associate { (ix, city) -> city.name to ix }
        fun ixByCityName(cityName: CityName) =
            ixByCityName[cityName] ?: throw Error("City $cityName not exists on game map")

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
                            && getSegmentBetween(source.name, dest.name) == null
                            && ixByCityName(source.name) < ixByCityName(dest.name)
                }
                .map { dest ->
                    val distance = dist[ixByCityName(source.name)][ixByCityName(dest.name)]
                    Ticket(source.name, dest.name, distance)
                }
        }.toList().sortedByDescending { it.points }
    }

    @Transient
    private val segmentsByCities =
        segments.asSequence().flatMap { sequenceOf(it.from to it, it.to to it) }.groupBy({ it.first }) { it.second }

    fun getSegmentBetween(from: CityName, to: CityName) =
        (segmentsByCities[from] ?: throw Error("City ${from.value} not found in map")).find { it.connects(from, to) }
}