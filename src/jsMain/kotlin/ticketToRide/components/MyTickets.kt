package ticketToRide.components

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.mButton
import kotlinx.css.*
import kotlinx.css.Color
import react.*
import styled.*
import ticketToRide.*

external interface MyTicketsProps : RProps {
    var tickets: List<Ticket>
    var pendingChoice: PendingTicketsChoice?
    var hoveredTicket: Ticket?
    var onHoveredTicketChanged: (Ticket?) -> Unit
    var onTicketChoiceMade: (Ticket, Boolean) -> Unit
}

class MyTickets : RComponent<MyTicketsProps, RState>() {
    override fun RBuilder.render() {
        for (ticket in props.tickets) {
            card(ticket) {
                css { +ComponentStyles.ticketRoute }
                render(ticket)
            }
        }
        if (props.pendingChoice != null) {
            for (ticket in props.pendingChoice!!.tickets) {
                card(ticket) {
                    renderWithChoice(ticket)
                }
            }
        }
    }

    private fun RBuilder.card(ticket: Ticket, builder: StyledElementBuilder<MPaperProps>.() -> Unit) {
        mPaper {
            attrs {
                elevation = if (props.hoveredTicket == ticket) 6 else 2
                with(asDynamic()) {
                    onMouseOver = { props.onHoveredTicketChanged(ticket) }
                    onMouseOut = { props.onHoveredTicketChanged(null) }
                }
            }
            css {
                minWidth = 300.px
                borderRadius = 4.px
                margin = 4.px.toString()
                paddingLeft = 12.px
                paddingRight = 12.px
                backgroundColor = Color.chocolate.withAlpha(0.2)
            }
            builder()
        }
    }

    private fun RBuilder.render(ticket: Ticket) {
        mTypography(variant = MTypographyVariant.body2) {
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

    private fun RBuilder.renderWithChoice(ticket: Ticket) {
        styledDiv {
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                justifyContent = JustifyContent.flexStart
            }
            styledDiv {
                css { +ComponentStyles.ticketRoute }
                render(ticket)
            }
            styledDiv {
                css {
                    display = Display.flex
                    alignItems = Align.center
                    flexDirection = FlexDirection.row
                    justifyContent = JustifyContent.spaceBetween
                }
                mButton("Беру") {
                    css { width = 45.pct }
                    attrs {
                        onClick = { props.onTicketChoiceMade(ticket, true) }
                    }
                }
                mButton("Сбрасываю") {
                    css { width = 45.pct }
                    attrs {
                        onClick = { props.onTicketChoiceMade(ticket, false) }
                    }
                }
            }
        }
    }

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