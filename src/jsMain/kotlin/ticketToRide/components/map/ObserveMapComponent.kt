package ticketToRide.components.map

import react.RBuilder
import ticketToRide.City
import ticketToRide.Locale
import ticketToRide.PlayerView

class ObserveMapComponent(props: Props) :
    MapComponentBase<ObserveMapComponent.Props, MapComponentBaseState>(props) {

    interface Props : MapComponentBaseProps {
        var connected: Boolean
        var players: List<PlayerView>
    }

    override fun MapCityMarkerProps.fill(city: City) {
        connected = props.connected
        selected = props.citiesToHighlight.contains(city.name)
        hasOccupiedSegment = false
        isTicketTarget = false
    }

    override fun MapSegmentProps.fill(from: City, to: City) {
        occupiedBy = props.players.find { it.occupiedSegments.any { it.connects(from.name, to.name) } }
    }
}

fun RBuilder.observeMap(builder: ObserveMapComponent.Props.() -> Unit) {
    child(ObserveMapComponent::class) {
        attrs(builder)
    }
}