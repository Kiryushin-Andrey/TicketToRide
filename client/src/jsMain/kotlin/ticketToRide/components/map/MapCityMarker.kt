package ticketToRide.components.map

import csstype.*
import emotion.react.css
import pigeonMaps.PigeonProps
import react.*
import react.dom.html.ReactHTML.div
import ticketToRide.CityId
import ticketToRide.PlayerId

external interface MapCityMarkerProps: PigeonProps {
    var cityId: CityId
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

val MapCityMarker = FC<MapCityMarkerProps> { props ->
    div {
        css {
            position = Position.absolute
            transform = translate((props.left ?: 0).px, (props.top ?: 0).px)
            if (props.myTurn) {
                cursor = Cursor.pointer
            }
            if (props.selected) {
                scale = number(1.2)
                zIndex = integer(150)
            }
        }

        onClick = {
            it.stopPropagation()
            props.onClick?.let { it(props.cityId) }
        }
        onMouseOver = { props.onMouseOver?.let { it(props.cityId) } }
        onMouseOut = { props.onMouseOut?.let { it(props.cityId) } }

        div {
            css {
                cssMarkerIcon()
                val img = props.station?.let { "station-${it.color.name.lowercase()}" } ?: when {
                    props.selected -> "city-marker-red"
                    props.hasOccupiedSegment -> "city-marker-green"
                    props.isTicketTarget -> "city-marker-yellow"
                    else -> "city-marker-blue"
                }
                backgroundImage = url("/icons/${img}.svg")
                if (props.station != null) {
                    transformOrigin = GeometryPosition.center
                    transform = translate((-50).pct, (-50).pct)
                    scale = number(1.5)
                } else {
                    transform = translate((-50).pct, (-50).pct)
                }
            }
        }
        if (props.selected || props.displayAllCityNames) {
            // popup bubble style taken from https://developers.google.com/maps/documentation/javascript/examples/overlay-popup
            div {
                css {
                    cssPopupContainer()
                }
                div {
                    css {
                        cssPopupBubbleAnchor()
                        bottom = if (props.station != null) 20.px else 16.px
                        after {
                            borderTopColor = if (props.selected) NamedColor.lightpink else NamedColor.white
                        }
                    }
                    div {
                        css {
                            popupBubble()
                            backgroundColor = if (props.selected) NamedColor.lightpink else NamedColor.white
                        }
                        +props.cityName
                    }
                }
            }
        }
    }
}

fun PropertiesBuilder.cssMarkerIcon() {
    position = Position.absolute
    width = 20.px
    height = 20.px
    backgroundSize = "${20.px} ${20.px}".unsafeCast<BackgroundSize>()
    backgroundRepeat = BackgroundRepeat.noRepeat
}

fun PropertiesBuilder.cssPopupContainer() {
    cursor = Auto.auto
    position = Position.absolute
    height = 0.px
    width = 200.px
}

fun PropertiesBuilder.cssPopupBubbleAnchor() {
    /* Position the div a fixed distance above the tip. */
    position = Position.absolute
    width = 100.pct
    left = 0.px
    after {
        content = string("''")
        position = Position.absolute
        top = 0.px
        left = 0.px
        /* Center the tip horizontally. */
        transform = translate((-50).pct)
        /* The tip is a https://css-tricks.com/snippets/css/css-triangle/ */
        width = 0.px
        height = 0.px
        /* The tip is 8px high, and 12px wide. */
        borderLeftWidth = 6.px
        borderLeftStyle = LineStyle.solid
        borderLeftColor = NamedColor.transparent
        borderRightWidth = 6.px
        borderRightStyle = LineStyle.solid
        borderRightColor = NamedColor.transparent
        borderTopWidth = 8.px /* tip height */
        borderTopStyle = LineStyle.solid
    }
}

fun PropertiesBuilder.popupBubble() {
    /* Position the bubble centred-above its parent. */
    position = Position.absolute
    top = 0.px
    left = 0.px
    transform = translate((-50).pct, (-100).pct)
    /* Style the bubble. */
    padding = 5.px
    borderRadius = 5.px
    fontSize = 14.px
    overflowY = Auto.auto
    maxHeight = 60.px
    boxShadow = BoxShadow(
        offsetX = 0.px,
        offsetY = 2.px,
        blurRadius = 10.px,
        spreadRadius = 1.px,
        color = NamedColor.grey
    )
}
