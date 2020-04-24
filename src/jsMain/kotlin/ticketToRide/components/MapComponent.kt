package ticketToRide.components

import google.maps.*
import google.map.react.*
import kotlinext.js.*
import org.w3c.dom.*
import react.*
import ticketToRide.*
import ticketToRide.playerState.*
import kotlin.js.Promise

interface MapComponentProps : ComponentBaseProps {
    var gameMap: GameMap
    var citiesToHighlight: Set<CityName>
    var onCityMouseOver: (CityName) -> Unit
    var onCityMouseOut: (CityName) -> Unit
}

interface MainMapBlockState : RState {
    var map: Map<Element>?
    var mapZoom: Int
}

class MapComponent(props: MapComponentProps) : ComponentBase<MapComponentProps, MainMapBlockState>(props) {

    override fun RBuilder.render() {
        googleMap {
            attrs {
                center = props.gameMap.mapCenter.toGoogleMapCoords()
                zoom = props.gameMap.mapZoom
                googleMapLoader = { Promise.resolve(js("google.maps") as Any) }
                onGoogleApiLoaded = { maps -> setState { map = maps.map; mapZoom = props.gameMap.mapZoom } }
                yesIWantToUseGoogleMapApiInternals = true
                onChildMouseEnter = { key, _ -> setState { props.onCityMouseOver(CityName(key as String)) } }
                onChildMouseLeave = { key, _ -> setState { props.onCityMouseOut(CityName(key as String)) } }
                onZoomAnimationEnd = { zoom -> setState { mapZoom = (zoom as Int) } }
            }

            props.gameMap.cities.forEach { cityMarker(it) }
        }

        routeSegments()
    }

    private fun RBuilder.cityMarker(city: City) {
        val cityName = CityName(city.name)
        marker {
            key = city.name
            name = city.name
            lat = city.latLng.lat
            lng = city.latLng.lng
            displayAllCityNames = state.mapZoom > 5
            selected = (props.citiesToHighlight + playerState.citiesToHighlight).contains(cityName)
            hasOccupiedSegment = me.occupiedSegments.any { it.from == cityName || it.to == cityName }
            onClick = { act { onCityClick(cityName) } }
        }
    }

    private fun RBuilder.routeSegments() {
        state.map?.let { map ->
            val cityByName = props.gameMap.cities.associateBy { it.name }
            props.gameMap.cities.forEach { fromCity ->
                fromCity.routes.forEach { route ->
                    val toCity =
                        cityByName[route.destination] ?: error("City ${route.destination} not present in game map")

                    mapSegment {
                        this.map = map
                        mapZoom = state.mapZoom
                        from = fromCity
                        to = toCity
                        color = route.color
                        points = route.points
                        occupiedBy = players.find { it.occupiedSegments.any { it.connects(fromCity.name, toCity.name) } }
                    }
                }
            }
        }
    }
}

fun RBuilder.googleMap(block: RElementBuilder<Props>.() -> Unit): ReactElement {
    return child(GoogleMapReact::class, block)
}

fun LatLong.toGoogleMapCoords() = jsObject<Coords> {
    lat = this@toGoogleMapCoords.lat
    lng = this@toGoogleMapCoords.lng
}

fun RBuilder.mainMap(props: ComponentBaseProps, builder: MapComponentProps.() -> Unit) {
    child(MapComponent::class) {
        attrs {
            this.gameState = props.gameState
            this.playerState = props.playerState
            this.onAction = props.onAction
            builder()
        }
    }
}