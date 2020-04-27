package ticketToRide.components

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.mButton
import kotlinx.css.*
import react.*
import react.dom.span
import styled.*
import ticketToRide.CityName
import ticketToRide.playerState.PlayerState
import ticketToRide.Ticket

interface MyTicketsProps : ComponentBaseProps {
    var citiesToHighlight: Set<CityName>
    var onTicketMouseOver: (Ticket) -> Unit
    var onTicketMouseOut: (Ticket) -> Unit
}

class MyTickets : ComponentBase<MyTicketsProps, RState>() {
    override fun RBuilder.render() {
        myTickets.forEach { render(it) }

        (playerState as? PlayerState.ChoosingTickets)?.let { choice ->
            styledDiv {
                css {
                    display = Display.flex
                    flexDirection = FlexDirection.row
                    justifyContent = JustifyContent.spaceBetween
                    alignItems = Align.baseline
                }
                mTypography("Выбор маршрутов", MTypographyVariant.h6) {
                    css {
                        paddingLeft = 10.px
                    }
                }
                mTooltip("Надо оставить минимум ${choice.minCountToKeep} маршрутов") {
                    attrs {
                        disableHoverListener = choice.isValid
                    }
                    span {
                        mButton("Готово", MColor.primary) {
                            attrs {
                                disabled = !choice.isValid
                                onClick = {
                                    if (choice.isValid)
                                        act { choice.confirm() }
                                }
                            }
                        }
                    }
                }
            }

            choice.items.forEach {
                render(it.ticket, TicketCheckbox(it.keep) {
                    act { choice.toggleTicket(it.ticket) }
                })
            }
        }
    }

    private fun RBuilder.render(ticket: Ticket, checkbox: TicketCheckbox? = null) {
        ticket(ticket) {
            highlighted = props.citiesToHighlight.containsAll(listOf(ticket.from, ticket.to))
            onMouseOver = { props.onTicketMouseOver(ticket) }
            onMouseOut = { props.onTicketMouseOut(ticket) }
            this.checkbox = checkbox
        }
    }
}

fun RBuilder.myTickets(props: ComponentBaseProps, block: MyTicketsProps.() -> Unit) = child(MyTickets::class) {
    attrs {
        gameState = props.gameState
        playerState = props.playerState
        onAction = props.onAction
        block()
    }
}
