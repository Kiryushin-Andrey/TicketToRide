package ticketToRide.components.map

import csstype.Color
import csstype.None
import csstype.pct
import csstype.px
import emotion.react.css
import fscreen.fScreen
import mui.icons.material.CropFree
import mui.icons.material.Map
import mui.icons.material.ZoomIn
import mui.icons.material.ZoomOut
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import ticketToRide.*
import web.html.HTMLDivElement
import web.window.WindowTarget

external interface MapComponentBaseProps : Props {
    var locale: Locale
    var gameMap: GameMap
    var citiesToHighlight: Set<CityId>
    var citiesWithStations: Map<CityId, PlayerId>
    var onCityMouseOver: (CityId) -> Unit
    var onCityMouseOut: (CityId) -> Unit
    var onMapClick: (() -> Unit)?

    var cityMarkerPropsBuilder: (MapCityMarkerProps, City) -> Unit
    var segmentPropsBuilder: (MapSegmentProps, Segment) -> Unit
}

fun MapComponentBaseProps.copyFrom(other: MapComponentBaseProps) {
    locale = other.locale
    gameMap = other.gameMap
    citiesToHighlight = other.citiesToHighlight
    citiesWithStations = other.citiesWithStations
    onCityMouseOver = other.onCityMouseOver
    onCityMouseOut = other.onCityMouseOut
    cityMarkerPropsBuilder = other.cityMarkerPropsBuilder
    segmentPropsBuilder = other.segmentPropsBuilder
}

val MapComponentBase = FC<MapComponentBaseProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }
    var mapTilesProvider by useState(MapTilesProvider.Watermark)
    var mapZoom by useState(props.gameMap.mapZoom)
    var displayAllCityNames by useState(props.gameMap.mapZoom > 4)
    val mapElement = useRef<HTMLDivElement>()

    div {
        css {
            width = 100.pct
            height = 100.pct
        }
        ref = mapElement

        pigeonMaps.Map::class.react {
            provider = mapTilesProvider.provider
            attribution = attribution(mapTilesProvider)

            defaultCenter = props.gameMap.mapCenter.toPigeonMapCoords()
            defaultZoom = props.gameMap.mapZoom
            zoom = mapZoom
            onAnimationStart = {
                if (displayAllCityNames && mapZoom <= 4) {
                    displayAllCityNames = false
                }
            }
            onAnimationStop = {
                if (!displayAllCityNames && mapZoom > 4) {
                    displayAllCityNames = true
                }
            }
            onBoundsChanged = {
                mapZoom = it.zoom as Int
                displayAllCityNames = mapZoom > 4
            }
            props.onMapClick?.let { handler ->
                onClick = { handler() }
            }

            cityMarkers(props, displayAllCityNames)
            routeSegments(props, mapZoom)

            MapControlButton {
                tooltip = str.zoomIn
                icon = ZoomIn
                topPosition = 10.px
                onClick = { mapZoom += 1 }
            }
            MapControlButton {
                tooltip = str.zoomOut
                icon = ZoomOut
                topPosition = 40.px
                onClick = { mapZoom -= 1 }
            }
            MapControlButton {
                tooltip = str.fullscreen
                icon = CropFree
                topPosition = 70.px
                onClick = {
                    if (fScreen.fullscreenElement != null) fScreen.exitFullscreen()
                    else mapElement.current?.let { fScreen.requestFullscreen(it) }
                }
            }
            MapControlButton {
                tooltip = str.toggleMapTiles
                icon = Map
                topPosition = 100.px
                onClick = {
                    mapTilesProvider =
                        if (mapTilesProvider == MapTilesProvider.Terrain) MapTilesProvider.Watermark
                        else MapTilesProvider.Terrain
                }
            }
        }
    }
}

private fun ChildrenBuilder.cityMarkers(props: MapComponentBaseProps, displayAllCityNames: Boolean) {
    props.gameMap.cities.forEach { city ->
        MapCityMarker {
            key = city.id.value
            cityId = city.id
            cityName = city.id.localize(props.locale, props.gameMap)
            anchor = city.latLng.toPigeonMapCoords()
            this.displayAllCityNames = displayAllCityNames
            station = props.citiesWithStations[city.id]
            selected = props.citiesToHighlight.contains(city.id)
            onMouseOver = props.onCityMouseOver
            onMouseOut = props.onCityMouseOut

            props.cityMarkerPropsBuilder(this, city)
        }
    }
}

private fun ChildrenBuilder.routeSegments(props: MapComponentBaseProps, mapZoom: Int) {
    RouteSegmentsComponent {
        gameMap = props.gameMap
        this.mapZoom = mapZoom
        fillSegmentProps = props.segmentPropsBuilder
    }
}

private fun ChildrenBuilder.link(text: String, href: String) {
    ReactHTML.a {
        css {
            color = Color("#0078A8")
            textDecoration = None.none
        }
        this.href = href
        target = WindowTarget._blank
        rel = "noreferrer noopener"
        +text
    }
}

// attribution element shouldn't fall into map component's children
// otherwise it will get map specific properties like latLngToPixel that don't make sense for it
private fun attribution(tilesProvider: MapTilesProvider): ReactNode {
    return span.create {
        +"Map tiles by "; link("Stamen Design", "http://stamen.com/")
        +", under "; link("CC BY 3.0", "http://creativecommons.org/licenses/by/3.0")
        +". Data by "; link("OpenStreetMap", "http://openstreetmap.org/")
        +", under "
        if (tilesProvider == MapTilesProvider.Terrain) link("ODbL", "http://www.openstreetmap.org/copyright")
        else link("CC BY SA", "http://creativecommons.org/licenses/by-sa/3.0")
    }
}

private fun strings(locale: Locale) = object : LocalizedStrings({ locale }) {

    val zoomIn by loc(
        Locale.En to "Zoom in",
        Locale.Ru to "Увеличить"
    )

    val zoomOut by loc(
        Locale.En to "Zoom out",
        Locale.Ru to "Уменьшить"
    )

    val fullscreen by loc(
        Locale.En to "Toggle fullscreen",
        Locale.Ru to "Полный экран"
    )

    val toggleMapTiles by loc(
        Locale.En to "Toggle map view",
        Locale.Ru to "Вид карты"
    )
}
