package ticketToRide.components

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

    override fun segmentProps(segmentProps: MapSegmentComponent.Props, from: City, to: City, route: Route) = with(segmentProps) {
        super.segmentProps(this, from, to, route)
        occupiedBy = props.players.find { it.occupiedSegments.any { it.connects(from.name, to.name) } }
        if (props.playerToHighlight != null && occupiedBy?.name != props.playerToHighlight) {
            occupiedBy = null
        }
    }
}
