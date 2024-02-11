package ticketToRide.components.tickets

import csstype.px
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.useMemo
import ticketToRide.*
import ticketToRide.components.GameComponentProps
import ticketToRide.components.copyFrom

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

    if (playerState !is PlayerState.ChoosingTickets) {
        Typography {
            variant = TypographyVariant.h6
            sx { paddingLeft = 10.px }
            +str.header
        }
    }

    val fulfilledTickets = me.getFulfilledTickets(gameState.myTicketsOnHand, gameState.players)
    gameState.myTicketsOnHand.forEach { ticket ->
        ticket(ticket, props.gameMap, props.locale) {
            finalScreen = false
            highlighted = props.citiesToHighlight.containsAll(listOf(ticket.from, ticket.to))
            fulfilled = fulfilledTickets.contains(ticket)
            onMouseOver = { props.onTicketMouseOver(ticket) }
            onMouseOut = { props.onTicketMouseOut(ticket) }
        }
    }

    gameState.myPendingTicketsChoice?.let  { choice ->
        PendingTicketsChoiceComponent {
            copyFrom(props)
            this.choice = choice
            citiesToHighlight = props.citiesToHighlight
            onTicketMouseOver = props.onTicketMouseOver
            onTicketMouseOut = props.onTicketMouseOut
        }
    }
}

private fun strings(locale: Locale) = object : LocalizedStrings({ locale }) {
    val header by loc(
        Locale.En to "My tickets",
        Locale.Ru to "Мои маршруты"
    )
}
