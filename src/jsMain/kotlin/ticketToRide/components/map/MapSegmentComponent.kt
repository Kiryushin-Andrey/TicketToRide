package ticketToRide.components.map

import google.maps.*
import kotlinext.js.jsObject
import org.w3c.dom.Element
import react.*
import ticketToRide.*
import kotlin.math.ceil

class MapSegmentComponent : RComponent<MapSegmentComponent.Props, RState>() {

    interface Props : RProps {
        var map: Map<Element>
        var mapZoom: Int
        var from: City
        var to: City
        var color: CardColor?
        var points: Int
        var occupiedBy: PlayerView?
    }

    private lateinit var line: Polyline

    override fun componentWillUpdate(nextProps: Props, nextState: RState) {
        line.setMap(null)
    }

    override fun componentWillUnmount() {
        line.setMap(null)
    }

    override fun RBuilder.render() {
        val occupiedBy = props.occupiedBy
        val (from, to) =
            if (props.from.latLng.lng < props.to.latLng.lng) (props.from to props.to)
            else (props.to to props.from)
        line = Polyline(jsObject {
            map = props.map
            geodesic = true
            path = arrayOf(from.latLng, to.latLng)
            strokeColor = occupiedBy?.color?.rgb ?: props.color?.rgb ?: "#AAAAAA"
            strokeWeight = if (occupiedBy != null) 8 else 3
            icons =
                if (occupiedBy == null)
                    arrayOf(segmentSplitIcon(props.color?.rgb, props.points, false))
                else
                    occupiedSegmentIcons(occupiedBy.color, props.points, props.mapZoom)
        })
    }

    private fun occupiedSegmentIcons(color: PlayerColor, points: Int, mapZoom: Int): Array<IconSequence> {
        val iconsList = mutableListOf(
            segmentSplitIcon(color.rgb, points, true),
            occupiedIcon(points, color.rgb)
        )
        if (mapZoom > 4) iconsList += carIcon(color.rgb)
        return iconsList.toTypedArray()
    }

    private fun segmentSplitIcon(colorRgb: String?, points: Int, occupied: Boolean) = jsObject<IconSequence> {
        icon = jsObject {
            path = if (occupied) "M 1,1 -1,-1 M 1,-1 -1,1" else "M 1,0 -1,0"
            strokeOpacity = 1
            strokeColor = if (occupied || colorRgb == "#000000") "#FFFFFF" else "#000000"
            scale = 3
        }
        offset = "0px"
        repeat = "${ceil(100f / points)}%"
    }

    private fun occupiedIcon(segmentsCount: Int, color: String) = jsObject<IconSequence> {
        icon = jsObject {
            path = SymbolPath.CIRCLE
            strokeOpacity = 1
            strokeColor = color
            strokeWeight = 2
            scale = 5
        }
        offset = "${ceil(100f / segmentsCount)}%"
        repeat = "${ceil(100f / segmentsCount)}%"
    }

    private fun carIcon(color: String) = jsObject<IconSequence> {
        icon = jsObject {
            path =
                "m 5.5,19.5 c 0,1.1 0.9,2 2,2 h 7 c 0.729,0 1.38669,-0.403362 1.71875,-1.03125 0.09197,0.01309 0.185809,0.03125 0.28125,0.03125 1.1,0 2,-0.9 2,-2 0,-1.1 -0.9,-2 -2,-2 1.1,0 2,-0.9 2,-2 0,-1.1 -0.9,-2 -2,-2 v -2 c 1.1,0 2,-0.9 2,-2 0,-1.1 -0.9,-2 -2,-2 1.1,0 2,-0.9 2,-2 0,-1.1 -0.9,-2 -2,-2 -0.09544,0 -0.189277,0.018157 -0.28125,0.03125 C 15.886691,1.903362 15.229,1.5 14.5,1.5 h -7 c -1.1,0 -2,0.9 -2,2 z m 2,-0.5 v -3 c 0,-0.3 0.2,-0.5 0.5,-0.5 h 3 c 0.3,0 0.5,0.2 0.5,0.5 v 3 c 0,0.3 -0.2,0.5 -0.5,0.5 H 8 C 7.7,19.5 7.5,19.3 7.5,19 Z m 0,-6 V 10 C 7.5,9.7 7.7,9.5 8,9.5 h 3 c 0.3,0 0.5,0.2 0.5,0.5 v 3 c 0,0.3 -0.2,0.5 -0.5,0.5 H 8 C 7.7,13.5 7.5,13.3 7.5,13 Z m 0,-6 V 4 C 7.5,3.7 7.7,3.5 8,3.5 h 3 c 0.3,0 0.5,0.2 0.5,0.5 v 3 c 0,0.3 -0.2,0.5 -0.5,0.5 H 8 C 7.7,7.5 7.5,7.3 7.5,7 Z M 16,18.5 C 16,18.2 16.2,18 16.5,18 16.8,18 17,18.2 17,18.5 17,18.8 16.8,19 16.5,19 16.2,19 16,18.8 16,18.5 Z m 0,-4 C 16,14.2 16.2,14 16.5,14 16.8,14 17,14.2 17,14.5 17,14.8 16.8,15 16.5,15 16.2,15 16,14.8 16,14.5 Z m 0,-6 C 16,8.2 16.2,8 16.5,8 16.8,8 17,8.2 17,8.5 17,8.8 16.8,9 16.5,9 16.2,9 16,8.8 16,8.5 Z m 0,-4 C 16,4.2 16.2,4 16.5,4 16.8,4 17,4.2 17,4.5 17,4.8 16.8,5 16.5,5 16.2,5 16,4.8 16,4.5 Z"
            strokeOpacity = 1
            strokeColor = color
            strokeWeight = 1
            scale = 1
        }
        offset = "-30px"
        repeat = "30px"
    }
}

fun RBuilder.mapSegment(builder: MapSegmentComponent.Props.() -> Unit) {
    child(MapSegmentComponent::class) {
        attrs(builder)
    }
}