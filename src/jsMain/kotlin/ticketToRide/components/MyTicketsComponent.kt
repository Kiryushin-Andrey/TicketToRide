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
        for (ticket in myTickets) {
            render(ticket)
        }

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
            for (item in choice.items) {
                render(item.ticket) {
                    mCheckbox {
                        attrs {
                            color = MOptionColor.primary
                            checked = item.keep
                            onChange = { _, _ ->
                                act { choice.toggleTicket(item.ticket) }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun RBuilder.render(ticket: Ticket, renderCheckbox: StyledElementBuilder<*>.() -> Unit = {}) {
        val highlighted = props.citiesToHighlight.containsAll(listOf(ticket.from, ticket.to))
        mPaper {
            attrs {
                elevation = if (highlighted) 6 else 2
                with(asDynamic()) {
                    onMouseOver = { props.onTicketMouseOver(ticket) }
                    onMouseOut = { props.onTicketMouseOut(ticket) }
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
                css {
                    minHeight = 40.px
                    display = Display.flex
                    alignItems = Align.center
                    flexDirection = FlexDirection.row
                    justifyContent = JustifyContent.spaceBetween
                }
                mTypography(variant = MTypographyVariant.body2) {
                    renderCheckbox()
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
}

fun RBuilder.myTickets(props: ComponentBaseProps, block: MyTicketsProps.() -> Unit) = child(MyTickets::class) {
    attrs {
        gameState = props.gameState
        playerState = props.playerState
        onAction = props.onAction
        block()
    }
}
