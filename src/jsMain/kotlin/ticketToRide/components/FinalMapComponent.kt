package ticketToRide.components

import ticketToRide.City
import ticketToRide.PlayerView
import ticketToRide.Route
import ticketToRide.connects

interface FinalMapComponentProps : MapComponentBaseProps {
    var players: List<PlayerView>
}

class MapFinalComponent(props: FinalMapComponentProps) :
    MapComponentBase<FinalMapComponentProps, MapComponentBaseState>(props) {
    
    override fun segmentProps(segmentProps: MapSegmentProps, from: City, to: City, route: Route) = with(segmentProps) {
        super.segmentProps(this, from, to, route)
        occupiedBy = props.players.find { it.occupiedSegments.any { it.connects(from.name, to.name) } }
    }
}
