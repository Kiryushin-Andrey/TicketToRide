package ticketToRide.components

import react.RBuilder
import ticketToRide.*
import ticketToRide.playerState.*

interface MapComponentProps : MapComponentBaseProps, ComponentBaseProps

class MapComponent(props: MapComponentProps) : MapComponentBase<MapComponentProps, MapComponentBaseState>(props) {

    private val gameState get() = props.gameState
    private val playerState get() = props.playerState
    private val players get() = gameState.players
    private val me get() = players.find { it.name == gameState.myName }!!

    private fun act(block: PlayerState.() -> PlayerState) = props.onAction(playerState.block())

    override fun cityMarkerProps(markerProps: MapCityMarkerProps, city: City) = with(markerProps) {
        super.cityMarkerProps(this, city)

        val cityName = CityName(city.name)
        selected = (props.citiesToHighlight + playerState.citiesToHighlight).contains(cityName)
        hasOccupiedSegment = me.occupiedSegments.any { it.from == cityName || it.to == cityName }
        onClick = { act { onCityClick(cityName) } }
    }

    override fun segmentProps(segmentProps: MapSegmentProps, from: City, to: City, route: Route) = with(segmentProps) {
        super.segmentProps(this, from, to, route)
        occupiedBy = players.find { it.occupiedSegments.any { it.connects(from.name, to.name) } }
    }
}

fun RBuilder.gameMap(props: ComponentBaseProps, builder: MapComponentProps.() -> Unit) {
    child(MapComponent::class) {
        attrs {
            this.gameState = props.gameState
            this.playerState = props.playerState
            this.onAction = props.onAction
            builder()
        }
    }
}