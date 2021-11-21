package ticketToRide.components.map

import kotlinx.css.*
import kotlinx.css.properties.*
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onMouseOutFunction
import kotlinx.html.js.onMouseOverFunction
import pigeonMaps.PigeonProps
import react.*
import react.dom.attrs
import styled.StyleSheet
import styled.css
import styled.inlineStyles
import styled.styledDiv
import ticketToRide.CityId
import ticketToRide.ICityId
import ticketToRide.PlayerId

external interface MapCityMarkerProps: PigeonProps {
    var cityIdBoxed: ICityId
    var cityName: String
    var displayAllCityNames: Boolean
    var selected: Boolean
    var station: PlayerId?
    var hasOccupiedSegment: Boolean
    var isTicketTarget: Boolean
    var myTurn: Boolean
    var onMouseOver: ((CityId) -> Unit)?
    var onMouseOut: ((CityId) -> Unit)?
    var onClick: ((CityId) -> Unit)?
}
val MapCityMarkerProps.cityId get() = cityIdBoxed.unboxed

private val mapCityMarker = fc<MapCityMarkerProps> { props ->
    styledDiv {
        inlineStyles {
            position = Position.absolute
            transform {
                translate((props.left ?: 0).px, (props.top ?: 0).px)
            }
            if (props.myTurn) {
                cursor = Cursor.pointer
            }
            if (props.selected) {
                transform { scale(1.2) }
                zIndex = 150
            }
        }
        attrs {
            onClickFunction = {
                it.stopPropagation()
                props.onClick?.let { it(props.cityId) }
            }
            onMouseOverFunction = { props.onMouseOver?.let { it(props.cityId) } }
            onMouseOutFunction = { props.onMouseOut?.let { it(props.cityId) } }
        }
        styledDiv {
            css {
                +ComponentStyle.markerIcon
                val img = props.station?.let { "station-${it.color.name.lowercase()}" } ?: when {
                    props.selected -> "city-marker-red"
                    props.hasOccupiedSegment -> "city-marker-green"
                    props.isTicketTarget -> "city-marker-yellow"
                    else -> "city-marker-blue"
                }
                backgroundImage = Image("url(/icons/${img}.svg)")
                if (props.station != null) {
                    put("transform-origin", "center")
                    transform {
                        translate((-50).pct, (-50).pct)
                        scale(1.5)
                    }
                } else {
                    transform {
                        translate((-50).pct, (-50).pct)
                    }
                }
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
                        bottom = if (props.station != null) 20.px else 16.px
                        after {
                            borderTopColor = if (props.selected) Color.lightPink else Color.white
                        }
                    }
                    styledDiv {
                        css {
                            +ComponentStyle.popupBubble
                            backgroundColor = if (props.selected) Color.lightPink else Color.white
                        }
                        +props.cityName
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
            padding = 5.px.toString()
            borderRadius = 5.px
            fontSize = 14.px
            overflowY = Overflow.auto
            maxHeight = 60.px
            boxShadow(Color.grey, 0.px, 2.px, 10.px, 1.px)
        }
    }

fun RBuilder.mapCityMarker(block: MapCityMarkerProps.() -> Unit) {
    child(mapCityMarker) {
        attrs(block)
    }
}