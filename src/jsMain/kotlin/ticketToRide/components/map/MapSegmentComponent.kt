package ticketToRide.components.map

import kotlinx.css.Color
import kotlinx.css.Cursor
import kotlinx.css.cursor
import kotlinx.html.js.onClickFunction
import pigeonMaps.PigeonProps
import react.*
import react.dom.svg
import styled.css
import svg.*
import ticketToRide.*
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

external interface MapSegmentProps : PigeonProps {
    var myTurn: Boolean
    var mapZoom: Int
    var from: City
    var to: City
    var color: CardColor?
    var points: Int
    var occupiedBy: PlayerId?
    var currentIx: Int
    var totalCount: Int
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
    val angle = acos((to.x - from.x) / distance) * (180 / PI) * (if (from.y < to.y) 1 else -1)
    val yHeight = if (props.mapZoom > 4) 10 else 6

    // shift segment in case there are several segments for this pair of cities
    val yGap = 4
    val yShift = 0 - (props.totalCount - 1) * (yGap + yHeight) / 2 + props.currentIx * (yGap + yHeight)

    props.occupiedBy?.let { occupiedBy ->
        occupiedSegment(occupiedBy, from, distance, angle, yHeight, yShift)
    } ?: run {
        freeSegment(props, from, distance, angle, yHeight, yShift)
    }
}

private fun RBuilder.freeSegment(
    props: MapSegmentProps,
    from: PigeonMapCoords,
    distance: Double,
    angle: Double,
    lineHeight: Int,
    lineShift: Int
) {
    fun block(x: Int, width: Int) = styledRect {
        if (props.myTurn) {
            css {
                cursor = Cursor.pointer
            }
        }
        attrs {
            this.x = x
            this.y = (from.y + lineShift - lineHeight / 2).toInt()
            this.height = lineHeight
            this.width = width
            fill = toSegmentRgb(props.color)
            stroke = Color.black.value
            strokeWidth = 1
            transform = "rotate ($angle ${from.x} ${from.y})"
            onClickFunction = {
                it.stopPropagation()
                props.onClick()
            }
        }
    }

    if (distance < 40) {
        block(
            x = from.x.toInt(),
            width = distance.toInt()
        )
    } else {
        val gap = when {
            distance < 80 -> 2
            distance < 120 -> 4
            else -> 8
        }
        for (i in 0 until props.points) {
            block(
                x = ((i * distance / props.points) + from.x).toInt(),
                width = (distance / props.points).toInt() - gap
            )
        }
    }
}

private fun RBuilder.occupiedSegment(
    occupiedBy: PlayerId,
    from: PigeonMapCoords,
    distance: Double,
    angle: Double,
    lineHeight: Int,
    lineShift: Int
): ReactElement {
    // rails
    fun horizontal(delta: Int) = line {
        x1 = from.x.toInt()
        x2 = (from.x + distance).toInt()
        y1 = from.y.toInt() + lineShift - delta
        y2 = from.y.toInt() + lineShift - delta
        stroke = occupiedBy.color.rgb
        strokeWidth = 2
        transform = "rotate ($angle ${from.x} ${from.y})"
    }
    horizontal(-2)
    horizontal(2)

    // sleepers
    return styledRect {
        attrs {
            x = from.x.toInt()
            y = (from.y + lineShift - lineHeight / 2).toInt()
            height = lineHeight
            width = distance.toInt()
            fill = "url(#sleepers)"
            transform = "rotate ($angle ${from.x} ${from.y})"
        }
    }
}

private fun RBuilder.mapSegment(
        segment: Segment,
        cityByName: Map<CityName, City>,
        currentIx: Int,
        totalCount: Int,
        builder: MapSegmentProps.() -> Unit) {
    child(mapSegment) {
        attrs {
            key = segment.hashCode().toString()
            from = cityByName[segment.from] ?: error("City ${segment.from.value} not present in game map")
            to = cityByName[segment.to] ?: error("City ${segment.to.value} not present in game map")
            color = segment.color
            points = segment.length
            this.currentIx = currentIx
            this.totalCount = totalCount
            builder()
        }
    }
}

external interface RouteSegmentsProps : PigeonProps {
    var gameMap: GameMap
    var mapZoom: Int
    var myTurn: Boolean
    var fillSegmentProps: (MapSegmentProps, Segment) -> Unit
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
        props.gameMap.segments
                .groupBy { segment -> segment.from to segment.to }.values
                .forEach { segments ->
                    segments.forEachIndexed { ix, segment ->
                        mapSegment(segment, cityByName, ix, segments.size) {
                            myTurn = props.myTurn
                            mapZoom = props.mapZoom
                            latLngToPixel = props.latLngToPixel
                            props.fillSegmentProps(this, segment)
                        }
                    }
                }
    }
}
