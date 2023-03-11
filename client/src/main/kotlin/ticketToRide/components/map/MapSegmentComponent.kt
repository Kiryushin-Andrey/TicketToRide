package ticketToRide.components.map

import csstype.Cursor
import csstype.NamedColor
import csstype.pct
import emotion.react.css
import pigeonMaps.PigeonProps
import react.*
import react.dom.svg.ReactSVG.defs
import react.dom.svg.ReactSVG.line
import react.dom.svg.ReactSVG.pattern
import react.dom.svg.ReactSVG.rect
import react.dom.svg.ReactSVG.svg
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

private val mapSegment = FC<MapSegmentProps> { props ->
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

private fun ChildrenBuilder.freeSegment(
    props: MapSegmentProps,
    from: PigeonMapCoords,
    distance: Double,
    angle: Double,
    lineHeight: Int,
    lineShift: Int
) {
    fun block(x: Double, width: Int) {
        rect {
            if (props.myTurn) {
                css {
                    cursor = Cursor.pointer
                }
            }
            this.x = x
            this.y = from.y + lineShift - lineHeight / 2
            this.height = lineHeight.toDouble()
            this.width = width.toDouble()
            fill = toSegmentRgb(props.color)
            stroke = "black"
            strokeWidth = 1.0
            transform = "rotate ($angle ${from.x} ${from.y})"
            onClick = {
                it.stopPropagation()
                props.onClick()
            }
        }
    }

    if (distance < 40) {
        block(
            x = from.x,
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
                x = (i * distance / props.points) + from.x,
                width = (distance / props.points).toInt() - gap
            )
        }
    }
}

private fun ChildrenBuilder.occupiedSegment(
    occupiedBy: PlayerId,
    from: PigeonMapCoords,
    distance: Double,
    angle: Double,
    lineHeight: Int,
    lineShift: Int
) {
    // rails
    fun horizontal(delta: Int) = line {
        x1 = from.x
        x2 = from.x + distance
        y1 = from.y + lineShift - delta
        y2 = from.y + lineShift - delta
        stroke = occupiedBy.color.rgb
        strokeWidth = 2.0
        transform = "rotate ($angle ${from.x} ${from.y})"
    }
    horizontal(-2)
    horizontal(2)

    // sleepers
    rect {
        x = from.x
        y = from.y + lineShift - lineHeight / 2
        height = lineHeight.toDouble()
        width = distance
        fill = "url(#sleepers)"
        transform = "rotate ($angle ${from.x} ${from.y})"
    }
}

private fun ChildrenBuilder.mapSegment(
    segment: Segment,
    cityById: Map<CityId, City>,
    currentIx: Int,
    totalCount: Int,
    builder: MapSegmentProps.() -> Unit
) {
    mapSegment {
        key = segment.hashCode().toString()
        from = cityById[segment.from] ?: error("City ${segment.from} not present in game map")
        to = cityById[segment.to] ?: error("City ${segment.to} not present in game map")
        color = segment.color
        points = segment.length
        this.currentIx = currentIx
        this.totalCount = totalCount
        builder()
    }
}

external interface RouteSegmentsProps : PigeonProps {
    var gameMap: GameMap
    var mapZoom: Int
    var myTurn: Boolean
    var fillSegmentProps: (MapSegmentProps, Segment) -> Unit
}

val RouteSegmentsComponent = FC<RouteSegmentsProps> { props ->
    svg {
        css {
           width = 100.pct
           height = 100.pct
        }

        defs {
            pattern {
                id = "sleepers"
                x = 0.0
                y = 0.0
                width = 10.0
                height = 10.0
                patternUnits = "userSpaceOnUse"

                line { x1 = 2.0; x2 = 2.0; y1 = 0.0; y2 = 10.0; stroke = "black"; strokeWidth = 1.0 }
            }
        }

        val cityByName = props.gameMap.cities.associateBy { it.id }
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
