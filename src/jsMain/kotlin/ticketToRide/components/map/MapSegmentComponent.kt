package ticketToRide.components.map

import kotlinx.css.Color
import kotlinx.html.js.onClickFunction
import pigeonMaps.PigeonProps
import react.*
import react.dom.svg
import svg.*
import ticketToRide.*
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

interface MapSegmentProps : PigeonProps {
    var mapZoom: Int
    var from: City
    var to: City
    var color: CardColor?
    var points: Int
    var occupiedBy: PlayerId?
    var onClick: () -> Unit
}

private fun distance(from: PigeonMapCoords, to: PigeonMapCoords) =
    sqrt((from.x - to.x).pow(2) + (from.y - to.y).pow(2))

private fun MapSegmentProps.getPixels(): Pair<PigeonMapCoords, PigeonMapCoords> {
    val from = latLngToPixel(from.latLng.toPigeonMapCoords())
    val to = latLngToPixel(to.latLng.toPigeonMapCoords())
    return if (from.x < to.x) from to to else to to from
}

private val mapSegment = functionalComponent<MapSegmentProps> { props ->
    val (from, to) = props.getPixels()
    val distance = distance(from, to)
    val angle = acos((to.x - from.x) / distance) * (180 / PI) * (if (from.y < to.y) 1 else -1);
    val lineHeight = if (props.mapZoom > 4) 10 else 6

    props.occupiedBy?.let { occupiedBy ->
        occupiedSegment(occupiedBy, from, distance, angle, lineHeight)
    } ?: run {
        freeSegment(props, from, distance, angle, lineHeight)
    }
}

private fun RBuilder.freeSegment(
    props: MapSegmentProps,
    from: PigeonMapCoords,
    distance: Double,
    angle: Double,
    lineHeight: Int
) {
    val gap = if (props.mapZoom > 4) 8 else 4
    for (i in 0 until props.points) {
        rect {
            x = ((i * distance / props.points) + from.x).toInt()
            y = (from.y - lineHeight / 2).toInt()
            height = lineHeight
            width = (distance / props.points).toInt() - gap
            fill = props.color?.rgb ?: "#AAAAAA"
            stroke = Color.black.value
            strokeWidth = 1
            transform = "rotate ($angle ${from.x} ${from.y})"
            onClickFunction = { props.onClick() }
        }
    }
}

private fun RBuilder.occupiedSegment(
    occupiedBy: PlayerId,
    from: PigeonMapCoords,
    distance: Double,
    angle: Double,
    lineHeight: Int,
): ReactElement {
    // rails
    fun horizontal(delta: Int) = line {
        x1 = from.x.toInt()
        x2 = (from.x + distance).toInt()
        y1 = from.y.toInt() - delta
        y2 = from.y.toInt() - delta
        stroke = occupiedBy.color.rgb
        strokeWidth = 2
        transform = "rotate ($angle ${from.x} ${from.y})"
    }
    horizontal(-2)
    horizontal(2)

    // sleepers
    return rect {
        x = from.x.toInt()
        y = (from.y - lineHeight / 2).toInt()
        height = lineHeight
        width = distance.toInt()
        fill = "url(#sleepers)"
        transform = "rotate ($angle ${from.x} ${from.y})"
    }
}

private fun RBuilder.mapSegment(segment: Segment, from: City, to: City, builder: MapSegmentProps.() -> Unit) {
    child(mapSegment) {
        attrs {
            key = "${segment.from.value}-${segment.to.value}"
            this.from = from
            this.to = to
            color = segment.color
            points = segment.length
            builder()
        }
    }
}

interface RouteSegmentsProps : PigeonProps {
    var gameMap: GameMap
    var mapZoom: Int
    var fillSegmentProps: (MapSegmentProps, City, City) -> Unit
}

val routeSegmentsComponent = functionalComponent<RouteSegmentsProps> { props ->
    svg {
        attrs["width"] = "100%"
        attrs["height"] = "100%"

        defs {
            pattern {
                attrs {
                    id = "sleepers"
                    x = 0
                    y = 0
                    width = 10
                    height = 10
                    patternUnits = PatternUnits.userSpaceOnUse
                }
                line { x1 = 2; x2 = 2; y1 = 0; y2 = 10; stroke = Color.black.value; strokeWidth = 1 }
            }
        }

        val cityByName = props.gameMap.cities.associateBy { it.name }
        props.gameMap.segments.forEach {
            val fromCity = cityByName[it.from] ?: error("City ${it.from.value} not present in game map")
            val toCity = cityByName[it.to] ?: error("City ${it.to.value} not present in game map")
            mapSegment(it, fromCity, toCity) {
                mapZoom = props.mapZoom
                latLngToPixel = props.latLngToPixel
                props.fillSegmentProps(this, fromCity, toCity)
            }
        }
    }
}
