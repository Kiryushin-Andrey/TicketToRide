package ticketToRide.components.map

import pigeonMaps.MapProps
import react.RBuilder
import ticketToRide.City
import ticketToRide.Segment
import ticketToRide.components.ComponentBaseProps
import ticketToRide.playerState.*
import ticketToRide.playerState.PlayerState.MyTurn.*

interface MapComponentProps : MapComponentBaseProps,
    ComponentBaseProps

class MapComponent(props: MapComponentProps) : MapComponentBase<MapComponentProps, MapComponentBaseState>(props) {

    private val gameState get() = props.gameState
    private val playerState get() = props.playerState
    private val players get() = gameState.players
    private val me get() = players.find { it.name == gameState.myName }!!

    private fun act(block: PlayerState.() -> PlayerState) = props.onAction(playerState.block())

    override fun MapProps.fill() {
        onClick = {
            (playerState as? PlayerState.MyTurn)?.let {
                if (playerState is PickedCity || playerState is BuildingSegment || playerState is BuildingStation) {
                    act { PlayerState.MyTurn.Blank(it) }
                }
            }
        }
    }

    override fun MapCityMarkerProps.fill(city: City) {
        myTurn = gameState.myTurn
        selected = (props.citiesToHighlight + playerState.citiesToHighlight).contains(city.name)
        hasOccupiedSegment = me.occupiedSegments.any { it.from == city.name || it.to == city.name }
        isTicketTarget = gameState.myTicketsOnHand.any { it.from == city.name || it.to == city.name }
        onClick = { act { onCityClick(city.name) } }
    }

    override fun MapSegmentProps.fill(segment: Segment) {
        myTurn = gameState.myTurn
        occupiedBy = players.find { it.occupiedSegments.contains(segment) }
        onClick = { act { onSegmentClick(segment) } }
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