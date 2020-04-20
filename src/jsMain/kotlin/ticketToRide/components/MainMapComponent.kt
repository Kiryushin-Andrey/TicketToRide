package ticketToRide.components

import google.maps.*
import google.map.react.*
import kotlinext.js.*
import org.w3c.dom.*
import react.*
import ticketToRide.*
import kotlin.js.Promise
import kotlin.math.*

data class RouteLine(val from: String, val to: String, val color: Color?, val segments: Int, val polyline: Polyline)

external interface MainMapBlockProps : RProps {
    var gameMap: GameMap
    var selectedTicket: Ticket?
    var myName: PlayerName
    var spannedSections: List<SpannedSection>
    var constructionInProgress: Set<CityName>
    var onCityClicked: (CityName) -> Unit
}

external interface MainMapBlockState : RState {
    var selectedCityName: CityName?
    var displayAllCityNames: Boolean
}

class MainMapBlock : RComponent<MainMapBlockProps, MainMapBlockState>() {
    private var routes: List<RouteLine> = emptyList()

    override fun MainMapBlockState.init() {
        displayAllCityNames = false
    }

    override fun RBuilder.render() {
        googleMap {
            attrs {
                center = GameMap.mapCenter.toGoogleMapCoords()
                zoom = GameMap.mapZoom
                googleMapLoader = { Promise.resolve(js("google.maps") as Any) }
                onGoogleApiLoaded = { maps -> drawRoutes(maps.map) }
                yesIWantToUseGoogleMapApiInternals = true
                onChildMouseEnter = { key, _ -> setState { selectedCityName = CityName(key as String) } }
                onChildMouseLeave = { _, _ -> setState { selectedCityName = null } }
                onZoomAnimationEnd = { zoom ->
                    setState {
                        displayAllCityNames = (zoom as Int) > 5
                    }
                }
            }
            val map = GameMap.cities.map { city ->
                val cityName = CityName(city.name)
                marker {
                    key = city.name
                    name = city.name
                    lat = city.latLng.lat
                    lng = city.latLng.lng
                    displayAllCityNames = state.displayAllCityNames
                    selected = (state.selectedCityName == cityName
                            || props.selectedTicket?.from == cityName
                            || props.selectedTicket?.to == cityName)
                    partOfSpannedSection =
                        props.spannedSections.containsCity(cityName) || props.constructionInProgress.contains(cityName)
                    onClick = { props.onCityClicked(cityName) }
                }
            }
        }
    }

    private fun List<SpannedSection>.containsCity(name: CityName) = any {
        it.player == props.myName && it.from == name || it.to == name
    }

    private fun drawRoutes(map: Map<Element>) {
        val cityByName = GameMap.cities.associateBy { it.name }
        routes = GameMap.cities
            .flatMap { city ->
                city.routes.map { route ->
                    val dest =
                        cityByName[route.destination] ?: error("City ${route.destination} not present in game map")
                    val spanColor = getSpanPlayerColor(city.name, dest.name)
                    val polyline = Polyline(jsObject {
                        this.map = map
                        path = arrayOf(city.latLng, dest.latLng)
                        strokeOpacity = 1
                        strokeColor = spanColor?.rgb ?: route.color?.rgb ?: "#AAAAAA"
                        strokeWeight = if (spanColor == null) 5 else 2
                        icons =
                            if (spanColor == null)
                                arrayOf(segmentSplitIcon(route, false)/*, dashedLikeIcon(route.color?.rgb ?: "#AAAAAA")*/)
                            else
                                arrayOf(segmentSplitIcon(route, true), spanMarkerIcon(route.segments, spanColor.rgb))
                    })
                    RouteLine(city.name, dest.name, route.color, route.segments, polyline)
                }
            }
    }

    private fun getSpanPlayerColor(from: String, to: String): Color? {
        return if (from == "Москва" && to == "Воронеж") Color.BLUE else null
    }

    private fun segmentSplitIcon(route: Route, spanned: Boolean) = jsObject<IconSequence> {
        icon = jsObject {
            path = if (spanned) "M 1,1 -1,-1 M 1,-1 -1,1" else "M 1,0 -1,0"
            strokeOpacity = 1
            strokeColor = if (route.color != Color.BLACK) "#000000" else "#FFFFFF"
            scale = 3
        }
        offset = "0px"
        repeat = "${ceil(100f / route.segments)}%"
    }

    private fun spanMarkerIcon(segmentsCount: Int, color: String) = jsObject<IconSequence> {
        icon = jsObject {
            path = SymbolPath.CIRCLE
            strokeOpacity = 1
            strokeColor = color
            strokeWeight = 2
            scale = 5
        }
        offset = "${ceil(100f / segmentsCount)}%"
        repeat = "${ceil(100f / segmentsCount)}%"
    }
}

fun RBuilder.googleMap(block: RElementBuilder<Props>.() -> Unit): ReactElement {
    return child(GoogleMapReact::class, block)
}

fun LatLong.toGoogleMapCoords() = jsObject<Coords> {
    lat = this@toGoogleMapCoords.lat
    lng = this@toGoogleMapCoords.lng
}
