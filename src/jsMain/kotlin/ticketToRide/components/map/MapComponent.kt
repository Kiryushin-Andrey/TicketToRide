package ticketToRide.components.map

import react.RBuilder
import ticketToRide.*
import ticketToRide.components.ComponentBaseProps
import ticketToRide.playerState.*

interface MapComponentProps : MapComponentBaseProps,
    ComponentBaseProps

class MapComponent(props: MapComponentProps) : MapComponentBase<MapComponentProps, MapComponentBaseState>(props) {

    private val gameState get() = props.gameState
    private val playerState get() = props.playerState
    private val players get() = gameState.players
    private val me get() = players.find { it.name == gameState.myName }!!

    private fun act(block: PlayerState.() -> PlayerState) = props.onAction(playerState.block())

    override fun MapCityMarkerProps.fill(city: City) {
        connected = props.connected
        selected = (props.citiesToHighlight + playerState.citiesToHighlight).contains(city.name)
        hasOccupiedSegment = me.occupiedSegments.any { it.from == city.name || it.to == city.name }
        isTicketTarget = gameState.myTicketsOnHand.any { it.from == city.name || it.to == city.name }
        onClick = { act { onCityClick(city.name) } }
    }

    override fun MapSegmentProps.fill(from: City, to: City) {
        occupiedBy = players.find { it.occupiedSegments.any { it.connects(from.name, to.name) } }
        onClick = { act { onSegmentClick(from.name, to.name) } }
    }
}

fun RBuilder.gameMap(props: ComponentBaseProps, builder: MapComponentProps.() -> Unit) {
    child(MapComponent::class) {
        attrs {
            this.locale = props.locale
            this.connected = props.connected
            this.gameState = props.gameState
            this.playerState = props.playerState
            this.onAction = props.onAction
            builder()
        }
    }
}