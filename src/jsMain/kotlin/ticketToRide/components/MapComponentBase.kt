package ticketToRide.components

import google.map.react.Coords
import google.map.react.GoogleMapReact
import google.map.react.Props
import google.maps.*
import kotlinext.js.jsObject
import kotlinx.css.RuleSet
import org.w3c.dom.*
import react.*
import ticketToRide.*
import kotlin.js.Promise

interface MapComponentBaseProps : RProps {
    var gameMap: GameMap
    var citiesToHighlight: Set<CityName>
    var onCityMouseOver: (CityName) -> Unit
    var onCityMouseOut: (CityName) -> Unit
}

interface MapComponentBaseState : RState {
    var map: Map<Element>?
    var mapZoom: Int
}

open class MapComponentBase<P, S>(props: P) : RComponent<P, S>(props)
        where P : MapComponentBaseProps, S : MapComponentBaseState {

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

            props.gameMap.cities.forEach { marker { cityMarkerProps(this, it) } }
        }

        routeSegments()
    }

    protected open fun cityMarkerProps(markerProps: MapCityMarkerProps, city: City) = with(markerProps) {
        key = city.name
        name = city.name
        lat = city.latLng.lat
        lng = city.latLng.lng
        displayAllCityNames = state.mapZoom > 4
        selected = props.citiesToHighlight.contains(CityName(city.name))
    }

    protected open fun segmentProps(segmentProps: MapSegmentProps, from: City, to: City, route: Route) =
        with(segmentProps) {
            this.from = from
            this.to = to
            color = route.color
            points = route.points
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
                        segmentProps(this, fromCity, toCity, route)
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

fun RBuilder.finalMap(builder: FinalMapComponentProps.() -> Unit) {
    child(MapFinalComponent::class) {
        attrs(builder)
    }
}