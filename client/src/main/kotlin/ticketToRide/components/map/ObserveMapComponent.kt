package ticketToRide.components.map

import react.ChildrenBuilder
import react.FC
import react.useCallback
import ticketToRide.City
import ticketToRide.PlayerView
import ticketToRide.Segment
import ticketToRide.screens.ObserveGameScreenProps

external interface ObserveMapComponentProps : MapComponentBaseProps {
    var connected: Boolean
    var players: List<PlayerView>
}

val ObserveMapComponent = FC<ObserveMapComponentProps> { props ->
    val cityMarkerPropsBuilder = useCallback(props.citiesToHighlight) { cityMarkerProps: MapCityMarkerProps, city: City ->
        with (cityMarkerProps) {
            selected = props.citiesToHighlight.contains(city.id)
            hasOccupiedSegment = false
            isTicketTarget = false
        }
    }

    val segmentPropsBuilder = useCallback(props.players) { segmentProps: MapSegmentProps, _: Segment ->
        with (segmentProps) {
            occupiedBy = props.players.find { it.occupiedSegments.any { it.connects(from.id, to.id) } }
        }
    }

    MapComponentBase {
        copyFrom(props)
        this.cityMarkerPropsBuilder = cityMarkerPropsBuilder
        this.segmentPropsBuilder = segmentPropsBuilder
    }
}

fun ChildrenBuilder.observeMap(props: ObserveGameScreenProps, builder: ObserveMapComponentProps.() -> Unit) {
    ObserveMapComponent {
        locale = props.locale
        connected = props.connected
        players = props.gameState.players
        gameMap = props.gameMap

        builder()
    }
}
