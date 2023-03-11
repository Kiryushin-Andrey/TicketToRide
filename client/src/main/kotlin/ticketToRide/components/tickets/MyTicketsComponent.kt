package ticketToRide.components.tickets

import csstype.*
import emotion.react.css
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import ticketToRide.*
import ticketToRide.components.*
import ticketToRide.playerState.PlayerState

external interface MyTicketsComponentProps : GameComponentProps {
    var citiesToHighlight: Set<CityId>
    var onTicketMouseOver: (Ticket) -> Unit
    var onTicketMouseOut: (Ticket) -> Unit
}

val MyTicketsComponent = FC<MyTicketsComponentProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }
    val playerState = props.playerState
    val gameState = props.gameState
    val me = gameState.me
    val connected = props.connected

    if (playerState !is PlayerState.ChoosingTickets) {
        Typography {
            variant = TypographyVariant.h6
            sx { paddingLeft = 10.px }
            +str.header
        }
    }

    val fulfilledTickets = me.getFulfilledTickets(gameState.myTicketsOnHand, gameState.players)
    gameState.myTicketsOnHand.forEach { ticket ->
        render(props, ticket, isFulfilled = fulfilledTickets.contains(ticket))
    }

    (playerState as? PlayerState.ChoosingTickets)?.let { choice ->
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
                !connected -> str.disconnected
                !choice.isValid -> str.youNeedToKeepAtLeastNTickets(choice.minCountToKeep)
                else -> ""
            }
            Tooltip {
                title = ReactNode(disabledTooltip)
                disableHoverListener = choice.isValid && connected

                span {
                    Button {
                        +str.ticketsChoiceDone
                        color = ButtonColor.primary
                        variant = ButtonVariant.contained
                        disabled = !choice.isValid || !connected
                        onClick = {
                            if (choice.isValid && connected)
                                props.act { choice.confirm() }
                        }
                    }
                }
            }
        }

        choice.items.forEach {
            render(props, it.ticket, isFulfilled = false, TicketCheckbox(it.keep) {
                props.act { choice.toggleTicket(it.ticket) }
            })
        }
    }
}

private fun ChildrenBuilder.render(props: MyTicketsComponentProps, ticket: Ticket, isFulfilled: Boolean, checkbox: TicketCheckbox? = null) {
    ticket(ticket, props.gameMap, props.locale) {
        finalScreen = false
        highlighted = props.citiesToHighlight.containsAll(listOf(ticket.from, ticket.to))
        fulfilled = isFulfilled
        onMouseOver = { props.onTicketMouseOver(ticket) }
        onMouseOut = { props.onTicketMouseOut(ticket) }
        this.checkbox = checkbox
    }
}

private fun strings(locale: Locale) = object : LocalizedStrings({ locale }) {

    val header by loc(
        Locale.En to "My tickets",
        Locale.Ru to "Мои маршруты"
    )

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
