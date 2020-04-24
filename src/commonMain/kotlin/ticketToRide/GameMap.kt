package ticketToRide

data class LatLong(val lat: Number, val lng: Number)
data class City(val name: String, val latLng: LatLong, val routes: List<Route> = emptyList())
data class Route(val destination: String, val color: Color?, val points: Int)

object GameMap {
    val mapCenter = RussiaMap.mapCenter
    val mapZoom = RussiaMap.mapZoom
    val cities = RussiaMap.cities
    val citiesByName = cities.associateBy { it.name }
    val longTickets: List<Ticket>
    val shortTickets: List<Ticket>

    init {
        val tickets = getAllTickets(cities)
        longTickets = tickets.takeWhile { it.points >= 20 }
        shortTickets = tickets.reversed().dropWhile { it.points < 5 }.takeWhile { it.points <= 12 }
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