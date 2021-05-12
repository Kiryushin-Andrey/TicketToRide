package ticketToRide.components.map

import react.RBuilder
import ticketToRide.City
import ticketToRide.PlayerView
import ticketToRide.Segment
import ticketToRide.screens.ObserveGameScreenProps

external interface ObserveMapComponentProps : MapComponentBaseProps {
    var connected: Boolean
    var players: List<PlayerView>
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
class ObserveMapComponent(props: ObserveMapComponentProps) :
    MapComponentBase<ObserveMapComponentProps, MapComponentBaseState>(props) {

    override fun MapCityMarkerProps.fill(city: City) {
        selected = props.citiesToHighlight.contains(city.id)
        hasOccupiedSegment = false
        isTicketTarget = false
    }

    override fun MapSegmentProps.fill(segment: Segment) {
        occupiedBy = props.players.find { it.occupiedSegments.any { it.connects(from.id, to.id) } }
    }
}

fun RBuilder.observeMap(props: ObserveGameScreenProps, builder: ObserveMapComponentProps.() -> Unit) {
    child(ObserveMapComponent::class) {
        attrs {
            locale = props.locale
            connected = props.connected
            players = props.gameState.players
            gameMap = props.gameMap

            builder()
        }
    }
}