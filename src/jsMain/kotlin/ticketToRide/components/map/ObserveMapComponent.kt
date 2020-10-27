package ticketToRide.components.map

import react.RBuilder
import ticketToRide.City
import ticketToRide.Locale
import ticketToRide.PlayerView
import ticketToRide.Segment

class ObserveMapComponent(props: Props) :
    MapComponentBase<ObserveMapComponent.Props, MapComponentBaseState>(props) {

    interface Props : MapComponentBaseProps {
        var locale: Locale
        var connected: Boolean
        var players: List<PlayerView>
    }

    override fun cityMarkerProps(markerProps: MapCityMarker.Props, city: City) = with(markerProps) {
        super.cityMarkerProps(this, city)

        connected = props.connected
        selected = props.citiesToHighlight.contains(city.name)
        hasOccupiedSegment = false
        isTicketTarget = false
    }

    override fun segmentProps(segmentProps: MapSegmentComponent.Props, from: City, to: City, segment: Segment) =
        with(segmentProps) {
            super.segmentProps(this, from, to, segment)
            occupiedBy = props.players.find { it.occupiedSegments.any { it.connects(from.name, to.name) } }
        }
}

fun RBuilder.observeMap(builder: ObserveMapComponent.Props.() -> Unit) {
    child(ObserveMapComponent::class) {
        attrs(builder)
    }
}