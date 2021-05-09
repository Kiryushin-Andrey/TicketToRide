package ticketToRide.components.cards

import kotlinx.css.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import react.RBuilder
import react.RState
import styled.StyleSheet
import styled.css
import styled.styledDiv
import ticketToRide.Card
import ticketToRide.Locale
import ticketToRide.LocalizedStrings
import ticketToRide.components.ComponentBase
import ticketToRide.components.ComponentBaseProps
import ticketToRide.components.componentBase
import ticketToRide.playerState.*
import ticketToRide.playerState.PlayerState.MyTurn.PickedFirstCard
import kotlinx.browser.document

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
class CardsDeckComponent : ComponentBase<ComponentBaseProps, RState>() {

    override fun componentDidMount() {
        document.addEventListener("keypress", onKeyPress)
    }

    override fun componentWillUnmount() {
        document.removeEventListener("keypress", onKeyPress)
    }

    private val onKeyPress = { e: Event ->
        with(e as KeyboardEvent) {
            if (canPickCards && charCode.toChar() in ('0'..'5')) {
                val cardIx = (key.toInt() - 1).takeIf { it >= 0 }
                if (getDisabledTooltip(cardIx) == null)
                    act { if (cardIx != null) pickedOpenCard(cardIx) else pickedClosedCard() }
            }
        }
    }

    private val chosenCardIx get() = (playerState as? PickedFirstCard)?.chosenCardIx

    private fun getDisabledTooltip(cardIx: Int?) = when {
        !props.connected ->
            str.disconnected
        !myTurn ->
            str.waitingForYouTurn
        playerState is PlayerState.ChoosingTickets ->
            str.chooseTicketsFirst
        cardIx != null && chosenCardIx != null && openCards[cardIx] is Card.Loco ->
            str.cannotTakeLocoWithAnotherCard
        else -> null
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                +ComponentStyles.cardsDeck
            }
            styledDiv {
                css {
                    +ComponentStyles.cards
                }
                val chosenCardIx = (playerState as? PickedFirstCard)?.chosenCardIx
                for ((ix, card) in openCards.withIndex()) {
                    openCard(card, props.locale, canPickCards, ix, chosenCardIx, getDisabledTooltip(ix)) {
                        act { pickedOpenCard(ix) }
                    }
                }
                closedCard(props.locale, getDisabledTooltip(null), chosenCardIx != null) {
                    act { pickedClosedCard() }
                }
            }
            styledDiv {
                val tooltipForTickets = getDisabledTooltip(null) ?: if (lastRound) str.lastRound else null
                ticketsCard(props.locale, tooltipForTickets) {
                    act { pickedTickets() }
                }
            }
        }
    }

    private object ComponentStyles : StyleSheet("CardsDeck", isStatic = true) {
        val cardsDeck by css {
            height = 100.pct
            display = Display.flex
            flexDirection = FlexDirection.row
            flexWrap = FlexWrap.wrap
            justifyContent = JustifyContent.spaceBetween
        }
        val cards by css {
            display = Display.flex
            flexDirection = FlexDirection.row
            flexWrap = FlexWrap.wrap
            justifyContent = JustifyContent.flexStart
        }
    }

    private inner class Strings : LocalizedStrings({ props.locale }) {

        val disconnected by loc(
            Locale.En to "Server connection lost",
            Locale.Ru to "Нет соединения с сервером"
        )

        val waitingForYouTurn by loc(
            Locale.En to "Wait for you turn to move",
            Locale.Ru to "Ждем своего хода"
        )

        val chooseTicketsFirst by loc(
            Locale.En to "Choose tickets first",
            Locale.Ru to "Сначала надо выбрать маршруты"
        )

        val cannotTakeLocoWithAnotherCard by loc(
            Locale.En to "You cannot take loco together with another card",
            Locale.Ru to "Уже выбрана другая карта, локомотив брать нельзя"
        )

        val lastRound by loc(
            Locale.En to "Last round",
            Locale.Ru to "Идет последний круг"
        )
    }

    private val str = Strings()
}

fun RBuilder.cardsDeck(props: ComponentBaseProps) = componentBase<CardsDeckComponent, ComponentBaseProps>(props)