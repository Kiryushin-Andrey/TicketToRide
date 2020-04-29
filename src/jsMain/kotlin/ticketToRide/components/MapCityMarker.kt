package ticketToRide.components

import kotlinx.css.*
import kotlinx.css.properties.*
import kotlinx.html.js.onClickFunction
import react.*
import styled.*

interface MapCityMarkerProps: RProps {
    var lat: Number
    var lng: Number

    var name: String
    var displayAllCityNames: Boolean
    var selected: Boolean
    var hasOccupiedSegment: Boolean
    var onClick: (() -> Unit)?
}

class MapCityMarker : RComponent<MapCityMarkerProps, RState>() {
    override fun RBuilder.render() {
        val scale = if (props.selected) 1.2 else 1.0

        styledDiv {
            css {
                position = Position.absolute
                cursor = Cursor.pointer
                if (props.selected) {
                    transform {
                        scale(scale)
                    }
                    zIndex = 150
                }
            }
            attrs {
                onClickFunction = { props.onClick?.let { it() } }
            }
            styledDiv {
                css {
                    +ComponentStyle.markerIcon
                    val img = when {
                        props.selected -> "city-marker-red"
                        props.hasOccupiedSegment -> "city-marker-green"
                        else -> "city-marker-blue"
                    }
                    backgroundImage = Image("url(/icons/${img}.svg)")
                }
            }
            if (props.selected || props.displayAllCityNames) {
                // popup bubble style taken from https://developers.google.com/maps/documentation/javascript/examples/overlay-popup
                styledDiv {
                    css {
                        +ComponentStyle.popupContainer
                    }
                    styledDiv {
                        css {
                            +ComponentStyle.popupBubbleAnchor
                        }
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
        val markerIcon by css {
            position = Position.absolute
            width = 20.px
            height = 20.px
            put("transform-origin", "left top")
            transform {
                translate((-50).pct, (-50).pct)
            }
            backgroundSize = "${20.px} ${20.px}"
            backgroundRepeat = BackgroundRepeat.noRepeat
        }
        val popupContainer by css {
            cursor = Cursor.auto
            position = Position.absolute
            height = 0.px
            width = 200.px
        }
        val popupBubbleAnchor by css {
            /* Position the div a fixed distance above the tip. */
            position = Position.absolute
            width = 100.pct
            left = 0.px
            bottom = 16.px
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
}

fun RBuilder.marker(block: MapCityMarkerProps.() -> Unit): ReactElement {
    return child(MapCityMarker::class) {
        attrs(block)
    }
}