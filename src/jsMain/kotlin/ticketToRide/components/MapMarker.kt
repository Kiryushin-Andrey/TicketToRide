package ticketToRide.components

import kotlinx.css.*
import kotlinx.css.properties.*
import react.*
import styled.*
import kotlin.math.*

external interface MarkerProps: RProps {
    var lat: Number
    var lng: Number

    var name: String
    var displayAllCityNames: Boolean
    var selected: Boolean
}

class MapMarker : RComponent<MarkerProps, RState>() {
    override fun RBuilder.render() {
        val scale = if (props.selected) 1.5 else 1.0

        val marker = MarkerDescription

        styledDiv {
            markerHolderStyle(marker)
            styledDiv {
                markerStyle(marker, scale)
            }
            if (props.selected || props.displayAllCityNames) {
                // marker style taken from https://developers.google.com/maps/documentation/javascript/examples/overlay-popup
                styledDiv {
                    css {
                        +ComponentStyle.popupContainer
                        zIndex = if (props.selected) 150 else 100
                    }
                    styledDiv {
                        css { +ComponentStyle.popupBubbleAnchor }
                        styledDiv {
                            css { +ComponentStyle.popupBubble }
                            +props.name
                        }
                    }
                }
            }
        }
    }

    private object ComponentStyle : StyleSheet("mapMarker", isStatic = true) {
        val popupContainer by css {
            cursor = Cursor.auto
            height = 0.px
            position = Position.absolute
            width = 200.px
        }
        val popupBubbleAnchor by css {
            /* Position the div a fixed distance above the tip. */
            position = Position.absolute
            width = 100.pct
            bottom = 8.px
            left = 0.px
            after {
                content = QuotedString("")
                position = Position.absolute
                top = 0.px
                left = 0.px
                /* Center the tip horizontally. */
                transform {
                    translateX((-50).pct)
                }
                /* The tip is a https://css-tricks.com/snippets/css/css-triangle/ */
                width = 0.px
                height = 0.px
                /* The tip is 8px high, and 12px wide. */
                borderLeftWidth = 6.px
                borderLeftStyle = BorderStyle.solid
                borderLeftColor = Color.transparent
                borderRightWidth = 6.px
                borderRightStyle = BorderStyle.solid
                borderRightColor = Color.transparent
                borderTopWidth = 8.px /* tip height */
                borderTopStyle = BorderStyle.solid
                borderTopColor = Color.white
            }
        }
        val popupBubble by css {
            /* Position the bubble centred-above its parent. */
            position = Position.absolute
            top = 0.px
            left = 0.px
            transform {
                translate((-50).pct, (-100).pct)
            }
            /* Style the bubble. */
            backgroundColor = Color.white
            padding = 5.px.toString()
            borderRadius = 5.px
            fontSize = 14.px
            overflowY = Overflow.auto
            maxHeight = 60.px
            boxShadow(Color.black.withAlpha(0.5), 0.px, 2.px, 10.px, 1.px)
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

    private fun StyledDOMBuilder<*>.markerStyle(marker: MarkerDescription, scale: Double) {
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
            zIndex = round(scale * 10).toInt()
            backgroundImage = Image("url(${marker.imageUrl})")
        }
    }

    private val  K_SCALE_NORMAL = 0.65;
    private val  K_MIN_CONTRAST = 0.4;
}

fun RBuilder.marker(block: MarkerProps.() -> Unit): ReactElement {
    return child(MapMarker::class) {
        attrs(block)
    }
}