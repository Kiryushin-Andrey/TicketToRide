package ticketToRide.components.map

import react.FC
import react.useCallback
import ticketToRide.*
import ticketToRide.components.*
import ticketToRide.PlayerState.MyTurn.*

external interface MapComponentProps : MapComponentBaseProps, GameComponentProps

val MapComponent = FC<MapComponentProps> { props ->
    val gameState = props.gameState
    val playerState = props.playerState
    val me = props.gameState.me

    val cityMarkerPropsBuilder = useCallback(props) { cityMarkerProps: MapCityMarkerProps, city: City ->
        with (cityMarkerProps) {
            myTurn = gameState.myTurn
            selected = (props.citiesToHighlight + playerState.citiesToHighlight).contains(city.id)
            hasOccupiedSegment = me.occupiedSegments.any { it.from == city.id || it.to == city.id }
            isTicketTarget = gameState.myTicketsOnHand.any { it.from == city.id || it.to == city.id }
            onClick = { props.act { onCityClick(city.id) } }
        }
    }

    val segmentPropsBuilder = useCallback(props) { segmentProps: MapSegmentProps, segment: Segment ->
        with (segmentProps) {
            myTurn = gameState.myTurn
            occupiedBy = gameState.players.find { it.occupiedSegments.contains(segment) }
            onClick = { props.act { onSegmentClick(segment) } }
        }
    }

    val onMapClick: () -> Unit = useCallback(props) {
        (playerState as? PlayerState.MyTurn)?.let {
            if (playerState is PickedCity || playerState is BuildingSegment || playerState is BuildingStation) {
                props.act { Blank(it) }
            }
        }
    }

    MapComponentBase {
        copyFrom(props)

        this.onMapClick = onMapClick
        this.cityMarkerPropsBuilder = cityMarkerPropsBuilder
        this.segmentPropsBuilder = segmentPropsBuilder
    }
}
