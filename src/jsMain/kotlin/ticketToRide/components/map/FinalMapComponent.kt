package ticketToRide.components.map

import react.RBuilder
import ticketToRide.*

class FinalMapComponent(props: Props) :
    MapComponentBase<FinalMapComponent.Props, MapComponentBaseState>(props) {

    interface Props : MapComponentBaseProps {
        var players: List<PlayerScore>
        var playerToHighlight: PlayerName?
    }

    override fun MapCityMarkerProps.fill(city: City) {
        selected = selected || (station != null && station?.name == props.playerToHighlight)
    }

    override fun MapSegmentProps.fill(segment: Segment) {
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