package ticketToRide.components

import ticketToRide.*

interface FinalMapComponentProps : MapComponentBaseProps {
    var players: List<PlayerView>
    var playerToHighlight: PlayerName?
}

class MapFinalComponent(props: FinalMapComponentProps) :
    MapComponentBase<FinalMapComponentProps, MapComponentBaseState>(props) {

    override fun cityMarkerProps(markerProps: MapCityMarkerProps, city: City) = with(markerProps) {
        super.cityMarkerProps(markerProps, city)
        selected = selected || (station != null && station?.name == props.playerToHighlight)
    }

    override fun segmentProps(segmentProps: MapSegmentProps, from: City, to: City, route: Route) = with(segmentProps) {
        super.segmentProps(this, from, to, route)
        occupiedBy = props.players.find { it.occupiedSegments.any { it.connects(from.name, to.name) } }
        highlight = occupiedBy != null && occupiedBy?.name == props.playerToHighlight
        thinned = props.playerToHighlight != null && occupiedBy?.name != props.playerToHighlight
    }
}
