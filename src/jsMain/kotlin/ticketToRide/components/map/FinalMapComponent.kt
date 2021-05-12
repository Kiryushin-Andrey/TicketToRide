package ticketToRide.components.map

import react.RBuilder
import ticketToRide.*

external interface FinalMapComponentProps : MapComponentBaseProps {
    var players: List<PlayerScore>
    var playerToHighlight: IPlayerName?
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
class FinalMapComponent(props: FinalMapComponentProps) :
    MapComponentBase<FinalMapComponentProps, MapComponentBaseState>(props) {

    override fun MapCityMarkerProps.fill(city: City) {
        selected = selected || (station != null && station?.name == props.playerToHighlight)
    }

    override fun MapSegmentProps.fill(segment: Segment) {
        occupiedBy = props.players.find { it.occupiedSegments.any { it.connects(from.id, to.id) } }
        if (props.playerToHighlight != null && occupiedBy?.name != props.playerToHighlight) {
            occupiedBy = null
        }
    }
}

fun RBuilder.finalMap(builder: FinalMapComponentProps.() -> Unit) {
    child(FinalMapComponent::class) {
        attrs(builder)
    }
}