package ticketToRide.components

import react.*
import styled.*
import google.map.react.*
import kotlinx.css.*
import kotlinx.css.properties.*
import kotlin.math.*

external interface GeoService {
    fun getWidth(): Int
    fun getHeight(): Int
}

external interface MarkerProps: RProps {
    var `$hover`: Boolean
    var `$dimensionKey`: Any
    var `$getDimensions`: (key: Any) -> Point
    var `$geoService`: GeoService
    var `$onMouseAllow`: () -> Any

    var lat: Number
    var lng: Number

    var name: String
    var hoveredAtTable: Boolean
    var showBallon: Boolean
    var scale: Double
}

class Marker : RComponent<MarkerProps, RState>() {
    override fun RBuilder.render() {
        val scale = when {
            props.`$hover` || props.showBallon -> K_SCALE_HOVER
            props.hoveredAtTable -> K_SCALE_TABLE_HOVER
            else -> props.scale
        }
        val mapWidth = props.`$geoService`.getWidth()
        val mapHeight = props.`$geoService`.getHeight()
        val markerDim = props.`$getDimensions`(props.`$dimensionKey`)
        val marker = MarkerDescription

        styledDiv {
            markerHolderStyle(marker)
            +props.name
            styledDiv {
                markerStyle(marker, scale, props.showBallon, props.`$hover`)
            }
            styledDiv {
                hintBalloonHorizontalPosStyle(markerDim.x.toInt(), marker, mapWidth)
            }
        }
    }

    private fun StyledDOMBuilder<*>.markerHolderStyle(marker: MarkerDescription) {
        css {
            position = Position.absolute
            cursor = Cursor.pointer
            width = marker.size.width.px
            height = marker.size.height.px
            left = (-marker.size.width * marker.origin.x).px
            top = (-marker.size.height * marker.origin.y).px
        }
    }

    private fun StyledDOMBuilder<*>.markerStyle(marker: MarkerDescription, scale: Double, showBalloon: Boolean, hover: Boolean) {
        val contrast = K_MIN_CONTRAST + (1 - K_MIN_CONTRAST) * min(scale / K_SCALE_NORMAL, 1.0);

        css {
            position = Position.absolute
            width = marker.size.width.px
            height = marker.size.height.px
            left = 0.px
            top = 0.px
            backgroundSize = "${marker.size.width}px ${marker.size.height}px"
            backgroundRepeat = BackgroundRepeat.noRepeat
            transition("transform", 0.25.s, cubicBezier(0.485, 1.650, 0.545, 0.835))
            transform {
                scale(scale, scale)
            }
            filter = "contrast(${contrast})"
            zIndex =
                round(scale * 10000).toInt() - (if (showBalloon) 20 else 0) + (if (hover) K_HINT_HTML_DEFAULT_Z_INDEX else 0) // balloon
            backgroundImage = Image("url(${marker.imageUrl})")
        }
    }

    private fun StyledDOMBuilder<*>.hintBalloonHorizontalPosStyle(x: Int, marker: MarkerDescription, mapWidth: Int) {
        val balloonWidthBase = 250
        // offset from map side
        val balloonMapOffset = 10
        // balloon with not more than map width
        val balloonWidth = min(balloonWidthBase, mapWidth - 2 * balloonMapOffset)
        // default ballon offset from arrow center i want
        val defaultOffset = balloonWidth * 0.15
        // from corner
        val offset = -defaultOffset + marker.size.width * 0.5
        // overflow in px (marker assymetric)
        val leftX = (x + offset - marker.size.width * marker.origin.x).toInt()
        val rightX = leftX + balloonWidth
        // recalc if overflow
        val mapOffset = offset + min(0, (mapWidth - balloonMapOffset) - rightX) + max(0, balloonMapOffset - leftX);

        css {
            width = balloonWidth.px
            left = mapOffset.px
            marginLeft = 0.px
        }
    }

    private val K_HINT_HTML_DEFAULT_Z_INDEX = 1000000;
    private val  K_SCALE_HOVER = 1.0;
    private val  K_SCALE_TABLE_HOVER = 1.0;
    private val  K_SCALE_NORMAL = 0.65;
    private val  K_MIN_CONTRAST = 0.4;
}

fun RBuilder.marker(block: MarkerProps.() -> Unit): ReactElement {
    return child(Marker::class) {
        attrs(block)
    }
}