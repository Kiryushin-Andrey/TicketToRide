package ticketToRide

data class LatLong(val lat: Number, val lng: Number)
data class City(val name: String, val latLng: LatLong, val routes: List<Route> = emptyList())
data class Route(val destination: String, val color: CardColor?, val points: Int)

object GameMap {
    private val pointsForSegments = mapOf(1 to 1, 2 to 2, 3 to 4, 4 to 7, 5 to 12, 6 to 15, 7 to 18, 8 to 21)
    fun getPointsForSegments(length: Int) =
        pointsForSegments[length] ?: throw Error("Points for ${length}-length segments not defined")

    val longTicketMinPoints = RussiaMap.longTicketMinPoints
    val shortTicketsPointsRange = RussiaMap.shortTicketsPointsRange
    val pointsForLongestPath = 10
    val mapCenter = RussiaMap.mapCenter
    val mapZoom = RussiaMap.mapZoom
    val cities = RussiaMap.cities
    val citiesByName = cities.associateBy { it.name }
    val longTickets: List<Ticket>
    val shortTickets: List<Ticket>
    val totalSegmentsLength by lazy { cities.flatMap { it.routes }.sumBy { it.points } }

    init {
        val tickets = getAllTickets(cities)
        longTickets = tickets.takeWhile { it.points >= longTicketMinPoints }
        shortTickets = tickets.reversed()
            .dropWhile { it.points < shortTicketsPointsRange.first }
            .takeWhile { it.points <= shortTicketsPointsRange.second }
    }
}

fun GameMap.getSegmentBetween(from: CityName, to: CityName) : Segment? {
    val route = citiesByName[from.value]!!.routes.firstOrNull { it.destination == to.value }
        ?: citiesByName[to.value]!!.routes.firstOrNull { it.destination == from.value }
    return if (route != null) Segment(from, to, route.color, route.points) else null
}

private fun getAllTickets(cities: List<City>): List<Ticket> {
    val ixByCityName = cities.withIndex().associate { (ix, city) -> city.name to ix }
    fun ixByCityName(cityName: String) = ixByCityName[cityName] ?: throw Error("City ${cityName} not exists on game map")

    val citiesCount = cities.size
    val dist = Array(citiesCount) { IntArray(citiesCount) { Int.MAX_VALUE } }
    for ((ix, city) in cities.withIndex()) {
        dist[ix][ix] = 0
        for (route in city.routes) {
            val targetIx = ixByCityName(route.destination)
            dist[ix][targetIx] = route.points
            dist[targetIx][ix] = route.points
        }
    }
    for (k in (0 until citiesCount))
        for (i in (0 until citiesCount))
            for (j in (0 until citiesCount))
                if (dist[i][k] < Int.MAX_VALUE && dist[k][j] < Int.MAX_VALUE && dist[i][j] > dist[i][k] + dist[k][j]) {
                    dist[i][j] = dist[i][k] + dist[k][j]
                }

    val tickets = cities.asSequence().flatMap { source ->
        cities.asSequence()
            .filter { dest ->
                source != dest
                        && !source.routes.any { it.destination == dest.name }
                        && !dest.routes.any { it.destination == source.name }
                        && ixByCityName(source.name) < ixByCityName(dest.name)
            }
            .map { dest ->
                val distance = dist[ixByCityName(source.name)][ixByCityName(dest.name)]
                Ticket(CityName(source.name), CityName(dest.name), distance)
            }
    }
    return tickets.toList().sortedByDescending { it.points }
}