package ticketToRide.components.map

import fscreen.fScreen
import kotlinx.css.Color
import kotlinx.css.color
import kotlinx.css.properties.TextDecoration
import kotlinx.css.px
import kotlinx.css.textDecoration
import org.w3c.dom.Element
import pigeonMaps.MapProps
import react.*
import react.dom.findDOMNode
import react.dom.span
import styled.css
import styled.styledA
import ticketToRide.*

external interface MapComponentBaseProps : RProps {
    var locale: Locale
    var gameMap: GameMap
    var citiesToHighlight: Set<CityName>
    var citiesWithStations: Map<CityName, PlayerId>
    var onCityMouseOver: (CityName) -> Unit
    var onCityMouseOut: (CityName) -> Unit
}

external interface MapComponentBaseState : RState {
    var mapZoom: Int
    var mapTilesProvider: MapTilesProvider
    var displayAllCityNames: Boolean
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
abstract class MapComponentBase<P, S>(props: P) : RComponent<P, S>(props)
        where P : MapComponentBaseProps, S : MapComponentBaseState {

    override fun S.init(props: P) {
        mapZoom = props.gameMap.mapZoom
        mapTilesProvider = MapTilesProvider.Watermark
        displayAllCityNames = props.gameMap.mapZoom > 4
    }

    override fun RBuilder.render() {
        var mapElement: Element? = null
        pigeonMap(state.mapTilesProvider) {
            ref {
                mapElement = findDOMNode(it)
            }
            attrs {
                defaultCenter = props.gameMap.mapCenter.toPigeonMapCoords()
                defaultZoom = props.gameMap.mapZoom
                zoom = state.mapZoom
                onAnimationStart = {
                    if (state.displayAllCityNames && state.mapZoom <= 4) setState { displayAllCityNames = false }
                }
                onAnimationStop = {
                    if (!state.displayAllCityNames && state.mapZoom > 4) setState { displayAllCityNames = true }
                }
                onBoundsChanged = {
                    setState {
                        mapZoom = it.zoom as Int
                        displayAllCityNames = mapZoom > 4
                    }
                }

                fill()
            }

            cityMarkers()
            routeSegments()

            zoomInButton()
            zoomOutButton()
            switchToFullScreenButton { mapElement }
            switchMapTilesButton()
        }
    }

    private fun RBuilder.cityMarkers() {
        props.gameMap.cities.forEach { city ->
            mapCityMarker {
                key = city.name.value
                nameBoxed = city.name
                anchor = city.latLng.toPigeonMapCoords()
                displayAllCityNames = state.displayAllCityNames
                station = props.citiesWithStations[city.name]
                selected = props.citiesToHighlight.contains(city.name)
                onMouseOver = props.onCityMouseOver
                onMouseOut = props.onCityMouseOut
                fill(city)
            }
        }
    }

    private fun RBuilder.routeSegments() {
        child(routeSegmentsComponent) {
            attrs {
                gameMap = props.gameMap
                mapZoom = state.mapZoom
                fillSegmentProps = { props, segment -> props.fill(segment) }
            }
        }
    }

    private fun RBuilder.zoomInButton() {
        mapControlButton {
            tooltip = str.zoomIn
            icon = "add"
            topPosition = 10.px
            onClick = { setState { mapZoom += 1 } }
        }
    }

    private fun RBuilder.zoomOutButton() {
        mapControlButton {
            tooltip = str.zoomOut
            icon = "remove"
            topPosition = 40.px
            onClick = { setState { mapZoom -= 1 } }
        }
    }

    private fun RBuilder.switchToFullScreenButton(getMapElement: () -> Element?) {
        mapControlButton {
            tooltip = str.fullscreen
            icon = "crop_free"
            topPosition = 70.px
            onClick = {
                if (fScreen.fullscreenElement != null) fScreen.exitFullscreen()
                else getMapElement()?.let { fScreen.requestFullscreen(it) }
            }
        }
    }

    private fun RBuilder.switchMapTilesButton() {
        mapControlButton {
            tooltip = str.toggleMapTiles
            icon = "map"
            topPosition = 100.px
            onClick = {
                setState {
                    mapTilesProvider =
                        if (mapTilesProvider == MapTilesProvider.Terrain) MapTilesProvider.Watermark
                        else MapTilesProvider.Terrain
                }
            }
        }
    }

    protected open fun MapProps.fill() {}

    protected abstract fun MapCityMarkerProps.fill(city: City)

    protected abstract fun MapSegmentProps.fill(segment: Segment)

    private inner class Strings : LocalizedStrings({ props.locale }) {

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

    private val str = Strings()
}

private fun RBuilder.pigeonMap(
    tilesProvider: MapTilesProvider,
    block: RElementBuilder<MapProps>.() -> Unit
): ReactElement {
    return child(pigeonMaps.Map::class) {
        attrs {
            block()
            provider = tilesProvider.provider
            attribution = attribution(tilesProvider)
        }
    }
}

private fun RBuilder.link(text: String, href: String) {
    styledA {
        css {
            color = Color("#0078A8")
            textDecoration = TextDecoration.none
        }
        attrs {
            this.href = href
            target = "_blank"
            rel = "noreferrer noopener"
        }
        +text
    }
}

// attribution element shouldn't fall into map component's children
// otherwise it will get map specific properties like latLngToPixel that don't make sense for it
private fun attribution(tilesProvider: MapTilesProvider) = RBuilder().span {
    +"Map tiles by "; link("Stamen Design", "http://stamen.com/")
    +", under "; link("CC BY 3.0", "http://creativecommons.org/licenses/by/3.0")
    +". Data by "; link("OpenStreetMap", "http://openstreetmap.org/")
    +", under "
    if (tilesProvider == MapTilesProvider.Terrain) link("ODbL", "http://www.openstreetmap.org/copyright")
    else link("CC BY SA", "http://creativecommons.org/licenses/by-sa/3.0")
}
