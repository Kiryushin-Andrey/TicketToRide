package ticketToRide.components

import google.map.react.Coords
import google.map.react.GoogleMapReact
import google.map.react.Props
import kotlinext.js.jsObject
import org.w3c.dom.*
import react.*
import ticketToRide.*
import kotlin.js.Promise

interface MapComponentBaseProps : RProps {
    var gameMap: GameMap
    var citiesToHighlight: Set<CityName>
    var citiesWithStations: Map<CityName, PlayerView>
    var onCityMouseOver: (CityName) -> Unit
    var onCityMouseOut: (CityName) -> Unit
}

interface MapComponentBaseState : RState {
    var map: google.maps.Map<Element>?
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
                onChildMouseEnter = { key, _ -> setState { props.onCityMouseOver(CityName(key)) } }
                onChildMouseLeave = { key, _ -> setState { props.onCityMouseOut(CityName(key)) } }
                onZoomAnimationEnd = { zoom -> setState { mapZoom = zoom } }
            }

            props.gameMap.cities.forEach { marker { cityMarkerProps(this, it) } }
        }

        routeSegments()
    }

    protected open fun cityMarkerProps(markerProps: MapCityMarker.Props, city: City) = with(markerProps) {
        key = city.name.value
        name = city.name.value
        lat = city.latLng.lat
        lng = city.latLng.lng
        displayAllCityNames = state.mapZoom > 4
        station = props.citiesWithStations[city.name]
        selected = props.citiesToHighlight.contains(city.name)
    }

    protected open fun segmentProps(segmentProps: MapSegmentComponent.Props, from: City, to: City, segment: Segment) =
        with(segmentProps) {
            this.from = from
            this.to = to
            color = segment.color
            points = segment.length
        }

    private fun RBuilder.routeSegments() {
        state.map?.let { map ->
            val cityByName = props.gameMap.cities.associateBy { it.name }
            props.gameMap.segments.forEach {
                val fromCity = cityByName[it.from] ?: error("City ${it.from.value} not present in game map")
                val toCity = cityByName[it.to] ?: error("City ${it.to.value} not present in game map")
                mapSegment {
                    this.map = map
                    mapZoom = state.mapZoom
                    segmentProps(this, fromCity, toCity, it)
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