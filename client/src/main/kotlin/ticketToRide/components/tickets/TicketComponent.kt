package ticketToRide.components.tickets

import csstype.*
import emotion.react.css
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.label
import ticketToRide.GameMap
import ticketToRide.Locale
import ticketToRide.Ticket
import ticketToRide.localize

class TicketCheckbox(val checked: Boolean, val onChange: () -> Unit)

external interface TicketComponentProps : Props {
    var locale: Locale
    var gameMap: GameMap
    var ticket: Ticket
    var highlighted: Boolean
    var fulfilled: Boolean
    var finalScreen: Boolean
    var checkbox: TicketCheckbox?
    var onMouseOver: () -> Unit
    var onMouseOut: () -> Unit
}

private val TicketComponent = FC<TicketComponentProps> { props ->
    Paper {
        elevation = if (props.highlighted) 6 else 2
        onMouseOver = { props.onMouseOver() }
        onMouseOut = { props.onMouseOut() }
        sx {
            borderRadius = 4.px
            margin = 4.px
            paddingLeft = 12.px
            paddingRight = 12.px
            backgroundColor = if (props.fulfilled && !props.finalScreen) NamedColor.lightgreen else NamedColor.linen
        }
        label {
            div {
                css {
                    minHeight = 40.px
                    display = Display.flex
                    alignItems = AlignItems.center
                    flexDirection = FlexDirection.row
                    justifyContent = JustifyContent.spaceBetween
                    if (props.checkbox != null) {
                        cursor = Cursor.pointer
                    }
                }
                Typography {
                    variant = TypographyVariant.body2

                    props.checkbox?.let {
                        Checkbox {
                            color = CheckboxColor.primary
                            checked = it.checked
                            onChange = { _, _ -> it.onChange() }
                        }
                    }
                    val from = props.ticket.from.localize(props.locale, props.gameMap)
                    val to = props.ticket.to.localize(props.locale, props.gameMap)
                    +"$from - $to"
                }
                when {
                    props.finalScreen && props.fulfilled ->
                        pointsLabel("+${props.ticket.points}", NamedColor.lightgreen)

                    props.finalScreen && !props.fulfilled ->
                        pointsLabel("-${props.ticket.points}", NamedColor.lightcoral)

                    else ->
                        pointsLabel(props.ticket.points, NamedColor.orange)
                }
            }
        }
    }
}

fun ChildrenBuilder.ticket(ticket: Ticket, gameMap: GameMap, locale: Locale, builder: TicketComponentProps.() -> Unit) {
    TicketComponent {
        this.ticket = ticket
        this.gameMap = gameMap
        this.locale = locale
        builder()
    }
}
