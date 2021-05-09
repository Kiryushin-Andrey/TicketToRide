package ticketToRide.components.map

import react.RBuilder
import ticketToRide.City
import ticketToRide.Locale
import ticketToRide.PlayerView
import ticketToRide.Segment

external interface ObserveMapComponentProps : MapComponentBaseProps {
    var connected: Boolean
    var players: List<PlayerView>
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
class ObserveMapComponent(props: ObserveMapComponentProps) :
    MapComponentBase<ObserveMapComponentProps, MapComponentBaseState>(props) {

    override fun MapCityMarkerProps.fill(city: City) {
        selected = props.citiesToHighlight.contains(city.name)
        hasOccupiedSegment = false
        isTicketTarget = false
    }

    override fun MapSegmentProps.fill(segment: Segment) {
        occupiedBy = props.players.find { it.occupiedSegments.any { it.connects(from.name, to.name) } }
    }
}

fun RBuilder.observeMap(builder: ObserveMapComponentProps.() -> Unit) {
    child(ObserveMapComponent::class) {
        attrs(builder)
    }
}