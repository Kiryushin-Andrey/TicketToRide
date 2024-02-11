package ticketToRide.components.tickets

import csstype.*
import emotion.react.css
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.ReactNode
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.useMemo
import react.useState
import ticketToRide.*
import ticketToRide.components.GameComponentProps

external interface PendingTicketsChoiceComponentProps : GameComponentProps {
    var choice: PendingTicketsChoice
    var citiesToHighlight: Set<CityId>
    var onTicketMouseOver: (Ticket) -> Unit
    var onTicketMouseOut: (Ticket) -> Unit
}

val PendingTicketsChoiceComponent = FC<PendingTicketsChoiceComponentProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }
    var keepTickets by useState(props.choice.tickets.map { false })
    val isChoiceValid = keepTickets.filter { it }.size >= props.choice.minCountToKeep

    div {
        css {
            display = Display.flex
            flexDirection = FlexDirection.row
            justifyContent = JustifyContent.spaceBetween
            alignItems = AlignItems.baseline
            paddingTop = 6.px
            paddingBottom = 6.px
        }
        Typography {
            variant = TypographyVariant.h6
            sx { paddingLeft = 10.px }
            +str.ticketsChoice
        }

        val disabledTooltip = when {
            !props.connected -> str.disconnected
            !isChoiceValid -> str.youNeedToKeepAtLeastNTickets(props.choice.minCountToKeep)
            else -> ""
        }
        Tooltip {
            title = ReactNode(disabledTooltip)
            disableHoverListener = isChoiceValid && props.connected

            ReactHTML.span {
                Button {
                    +str.ticketsChoiceDone
                    color = ButtonColor.primary
                    variant = ButtonVariant.contained
                    disabled = !isChoiceValid || !props.connected
                    onClick = {
                        if (isChoiceValid && props.connected)
                            props.act {
                                if (isChoiceValid) {
                                    val ticketsToKeep = props.choice.tickets.filterIndexed { ix, _ -> keepTickets[ix] }
                                    sendToServer(ConfirmTicketsChoiceRequest(ticketsToKeep))
                                    PlayerState.None
                                } else this
                            }
                    }
                }
            }
        }
    }

    props.choice.tickets.forEachIndexed { ix, ticket ->
        ticket(ticket, props.gameMap, props.locale) {
            finalScreen = false
            highlighted = props.citiesToHighlight.containsAll(listOf(ticket.from, ticket.to))
            fulfilled = false
            onMouseOver = { props.onTicketMouseOver(ticket) }
            onMouseOut = { props.onTicketMouseOut(ticket) }
            this.checkbox = TicketCheckbox(keepTickets[ix]) {
                keepTickets = keepTickets.mapIndexed { i, value -> if (i == ix) !value else value }
            }
        }
    }
}

private fun strings(locale: Locale) = object : LocalizedStrings({ locale }) {

    val ticketsChoice by loc(
        Locale.En to "Tickets choice",
        Locale.Ru to "Выбор маршрутов"
    )

    val youNeedToKeepAtLeastNTickets by locWithParam<Int>(
        Locale.En to { n -> "You need to keep at least $n tickets " },
        Locale.Ru to { n -> "Надо оставить минимум $n маршрутов" }
    )

    val ticketsChoiceDone by loc(
        Locale.En to "Done",
        Locale.Ru to "Готово"
    )


    val disconnected by loc(
        Locale.En to "Server connection lost",
        Locale.Ru to "Нет соединения с сервером"
    )
}
