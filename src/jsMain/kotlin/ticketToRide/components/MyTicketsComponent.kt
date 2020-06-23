package ticketToRide.components

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import kotlinx.css.*
import react.*
import react.dom.span
import styled.*
import ticketToRide.CityName
import ticketToRide.Locale
import ticketToRide.LocalizedStrings
import ticketToRide.playerState.PlayerState
import ticketToRide.Ticket

class MyTicketsComponent : ComponentBase<MyTicketsComponent.Props, RState>() {

    interface Props : ComponentBaseProps {
        var citiesToHighlight: Set<CityName>
        var onTicketMouseOver: (Ticket) -> Unit
        var onTicketMouseOut: (Ticket) -> Unit
    }

    override fun RBuilder.render() {
        if (playerState !is PlayerState.ChoosingTickets) {
            mTypography(str.header, MTypographyVariant.h6) {
                css {
                    paddingLeft = 10.px
                }
            }
        }

        myTickets.forEach { render(it) }

        (playerState as? PlayerState.ChoosingTickets)?.let { choice ->
            styledDiv {
                css {
                    display = Display.flex
                    flexDirection = FlexDirection.row
                    justifyContent = JustifyContent.spaceBetween
                    alignItems = Align.baseline
                    paddingTop = 6.px
                    paddingBottom = 6.px
                }
                mTypography(str.ticketsChoice, MTypographyVariant.h6) {
                    css {
                        paddingLeft = 10.px
                    }
                }

                val disabledTooltip = when {
                    !props.connected -> str.disconnected
                    !choice.isValid ->  str.youNeedToKeepAtLeastNTickets(choice.minCountToKeep)
                    else -> ""
                }
                mTooltip(disabledTooltip) {
                    attrs {
                        disableHoverListener = choice.isValid && props.connected
                    }
                    span {
                        mButton(str.ticketsChoiceDone, MColor.primary, MButtonVariant.contained) {
                            attrs {
                                disabled = !choice.isValid || !props.connected
                                onClick = {
                                    if (choice.isValid && props.connected)
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

    private inner class Strings : LocalizedStrings({ props.locale }) {

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

    private val str = Strings()
}

fun RBuilder.myTickets(props: ComponentBaseProps, block: MyTicketsComponent.Props.() -> Unit) =
    componentBase<MyTicketsComponent, MyTicketsComponent.Props>(props, block)