package ticketToRide.components

import kotlinx.css.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import react.RBuilder
import react.RState
import styled.StyleSheet
import styled.css
import styled.styledDiv
import ticketToRide.Card
import ticketToRide.playerState.*
import kotlin.browser.document

class CardsDeck : ComponentBase<ComponentBaseProps, RState>() {

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
        !myTurn ->
            "Ждем своего хода"
        playerState is PlayerState.ChoosingTickets ->
            "Сначала надо выбрать маршруты"
        cardIx != null && chosenCardIx != null && openCards[cardIx] is Card.Loco ->
            "Уже выбрана другая карта, локомотив брать нельзя"
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
                    openCard(card, canPickCards, ix, chosenCardIx, getDisabledTooltip(ix)) {
                        act { pickedOpenCard(ix) }
                    }
                }
                closedCard(getDisabledTooltip(null), chosenCardIx != null) {
                    act { pickedClosedCard() }
                }
            }
            styledDiv {
                val tooltipForTickets = getDisabledTooltip(null) ?: if (lastRound) "Идет последний круг" else null
                ticketsCard(tooltipForTickets) {
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
}

fun RBuilder.cardsDeck(props: ComponentBaseProps) = child(CardsDeck::class) {
    attrs {
        this.gameState = props.gameState
        this.playerState = props.playerState
        this.onAction = props.onAction
    }
}
