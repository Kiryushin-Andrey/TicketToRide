package ticketToRide.components.map

import react.FC
import react.useCallback
import ticketToRide.*

external interface FinalMapComponentProps : MapComponentBaseProps {
    var players: List<PlayerScore>
    var playerToHighlight: PlayerName?
}

val FinalMapComponent = FC<FinalMapComponentProps> { props ->
    val cityMarkerPropsBuilder = useCallback(props.playerToHighlight) { cityMarkerProps: MapCityMarkerProps, _: City ->
        with (cityMarkerProps) {
            selected = selected || (station != null && station?.name == props.playerToHighlight)
        }
    }
    val segmentPropsBuilder = useCallback(props.playerToHighlight) { segmentProps: MapSegmentProps, segment: Segment ->
        with (segmentProps) {
            occupiedBy = props.players.find { it.occupiedSegments.contains(segment) }
            if (props.playerToHighlight != null && occupiedBy?.name != props.playerToHighlight) {
                occupiedBy = null
            }
        }
    }

    MapComponentBase {
        copyFrom(props)
        this.cityMarkerPropsBuilder = cityMarkerPropsBuilder
        this.segmentPropsBuilder = segmentPropsBuilder
    }
}
