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
import react.dom.attrs
import react.dom.findDOMNode
import react.dom.span
import styled.css
import styled.styledA
import ticketToRide.*

external interface MapComponentBaseProps : Props {
    var locale: Locale
    var gameMap: GameMap
    var citiesToHighlight: Set<CityId>
    var citiesWithStations: Map<CityId, PlayerId>
    var onCityMouseOver: (CityId) -> Unit
    var onCityMouseOut: (CityId) -> Unit
}

external interface MapComponentBaseState : State {
    var mapZoom: Int
    var mapTilesProvider: MapTilesProvider
    var displayAllCityNames: Boolean
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
abstract class MapComponentBase<P, S>(props: P) : RComponent<P, S>(props)
        where P : MapComponentBaseProps, S : MapComponentBaseState {

    private val mapElement = createRef<Element>()

    override fun S.init(props: P) {
        mapZoom = props.gameMap.mapZoom
        mapTilesProvider = MapTilesProvider.Watermark
        displayAllCityNames = props.gameMap.mapZoom > 4
    }

    override fun RBuilder.render() {
        pigeonMap(state.mapTilesProvider) {
            attrs {
                ref = mapElement
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
            switchToFullScreenButton { mapElement.current }
            switchMapTilesButton()
        }
    }

    private fun RBuilder.cityMarkers() {
        props.gameMap.cities.forEach { city ->
            mapCityMarker {
                key = city.id.value.toString()
                cityIdBoxed = city.id
                cityName = city.id.localize(props.locale, props.gameMap)
                anchor = city.latLng.toPigeonMapCoords()
                displayAllCityNames = state.displayAllCityNames
                station = props.citiesWithStations[city.id]
                selected = props.citiesToHighlight.contains(city.id)
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
) = child(pigeonMaps.Map::class) {
    attrs {
        block()
        provider = tilesProvider.provider
        attribution = attribution(tilesProvider)
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
private fun attribution(tilesProvider: MapTilesProvider): ReactNode = RBuilder()
    .apply {
        span {
            +"Map tiles by "; link("Stamen Design", "http://stamen.com/")
            +", under "; link("CC BY 3.0", "http://creativecommons.org/licenses/by/3.0")
            +". Data by "; link("OpenStreetMap", "http://openstreetmap.org/")
            +", under "
            if (tilesProvider == MapTilesProvider.Terrain) link("ODbL", "http://www.openstreetmap.org/copyright")
            else link("CC BY SA", "http://creativecommons.org/licenses/by-sa/3.0")
        }
    }
    .childList.first()
