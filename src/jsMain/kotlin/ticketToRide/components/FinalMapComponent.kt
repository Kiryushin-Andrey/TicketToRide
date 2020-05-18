package ticketToRide.components

import react.RBuilder
import ticketToRide.*

class FinalMapComponent(props: Props) :
    MapComponentBase<FinalMapComponent.Props, MapComponentBaseState>(props) {

    interface Props : MapComponentBaseProps {
        var players: List<PlayerView>
        var playerToHighlight: PlayerName?
    }

    override fun cityMarkerProps(markerProps: MapCityMarker.Props, city: City) = with(markerProps) {
        super.cityMarkerProps(markerProps, city)
        selected = selected || (station != null && station?.name == props.playerToHighlight)
    }

    override fun segmentProps(segmentProps: MapSegmentComponent.Props, from: City, to: City, segment: Segment) = with(segmentProps) {
        super.segmentProps(this, from, to, segment)
        occupiedBy = props.players.find { it.occupiedSegments.any { it.connects(from.name, to.name) } }
        if (props.playerToHighlight != null && occupiedBy?.name != props.playerToHighlight) {
            occupiedBy = null
        }
    }
}

fun RBuilder.finalMap(builder: FinalMapComponent.Props.() -> Unit) {
    child(FinalMapComponent::class) {
        attrs(builder)
    }
}