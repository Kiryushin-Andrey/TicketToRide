package ticketToRide.components

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.mButton
import kotlinx.css.*
import react.*
import styled.*
import ticketToRide.PendingTicketsChoice
import ticketToRide.Ticket

external interface MyTicketsProps : RProps {
    var tickets: List<Ticket>
    var pendingChoice: PendingTicketsChoice?
    var hoveredTicket: Ticket?
    var onHoveredTicketChanged: (Ticket?) -> Unit
    var onConfirmTicketsChoice: (List<Ticket>) -> Unit
}

external interface MyTicketsState : RState {
    var ticketsToKeep: List<Ticket>
}

class MyTickets : RComponent<MyTicketsProps, MyTicketsState>() {
    override fun MyTicketsState.init() {
        ticketsToKeep = emptyList()
    }

    override fun RBuilder.render() {
        for (ticket in props.tickets) {
            render(ticket, false)
        }
        if (props.pendingChoice != null) {
            styledDiv {
                css {
                    display = Display.flex
                    flexDirection = FlexDirection.row
                    justifyContent = JustifyContent.spaceBetween
                }
                mTypography("Выбор маршрутов", MTypographyVariant.h6) {
                    css {
                        paddingLeft = 10.px
                        paddingTop = 10.px
                    }
                }
                mButton("Готово", MColor.primary) {
                    attrs {
                        disabled = !isTicketsChoiceValid
                        title = if (isTicketsChoiceValid) "" else "Надо оставить минимум ${props.pendingChoice!!.minCountToKeep} маршрутов"
                        onClick = { if (isTicketsChoiceValid) { props.onConfirmTicketsChoice(state.ticketsToKeep) } }
                    }
                }
            }
            for (ticket in props.pendingChoice!!.tickets) {
                render(ticket, true)
            }
        }
    }

    private fun RBuilder.render(ticket: Ticket, withCheckbox: Boolean) {
        mPaper {
            attrs {
                elevation = if (props.hoveredTicket == ticket) 6 else 2
                with(asDynamic()) {
                    onMouseOver = { props.onHoveredTicketChanged(ticket) }
                    onMouseOut = { props.onHoveredTicketChanged(null) }
                }
            }
            css {
                borderRadius = 4.px
                margin = 4.px.toString()
                paddingLeft = 12.px
                paddingRight = 12.px
                backgroundColor = Color.chocolate.withAlpha(0.2)
            }
            styledDiv {
                css { +ComponentStyles.ticketRoute }
                mTypography(variant = MTypographyVariant.body2) {
                    if (withCheckbox) {
                        mCheckbox {
                            attrs {
                                color = MOptionColor.primary
                                checked = state.ticketsToKeep.contains(ticket)
                                onChange = { _, value ->
                                    setState {
                                        ticketsToKeep =
                                            if (value) (ticketsToKeep + ticket).distinct()
                                            else ticketsToKeep - ticket
                                    }
                                }
                            }
                        }
                    }
                    +"${ticket.from.value} - ${ticket.to.value}"
                }
                mPaper {
                    attrs { elevation = 4 }
                    css {
                        width = 20.px
                        height = 20.px
                        textAlign = TextAlign.center
                        verticalAlign = VerticalAlign.middle
                        padding = 3.px.toString()
                        borderRadius = 50.pct
                        backgroundColor = Color.orange.withAlpha(0.7)
                    }
                    +ticket.points.toString()
                }
            }
        }
    }

    private val isTicketsChoiceValid
        get() =
            if (props.pendingChoice != null) state.ticketsToKeep.size >= props.pendingChoice!!.minCountToKeep
            else false

    private object ComponentStyles : StyleSheet("MyTickets", isStatic = true) {
        val ticketRoute by css {
            minHeight = 40.px
            display = Display.flex
            alignItems = Align.center
            flexDirection = FlexDirection.row
            justifyContent = JustifyContent.spaceBetween
        }
    }
}