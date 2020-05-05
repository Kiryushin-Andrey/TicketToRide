package ticketToRide.components

import kotlinx.css.*
import react.RBuilder
import react.RState
import styled.StyleSheet
import styled.css
import styled.styledDiv
import ticketToRide.Card
import ticketToRide.playerState.*

class CardsDeck : ComponentBase<ComponentBaseProps, RState>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                +ComponentStyles.cardsDeck
            }
            val disabledTooltip = when {
                !myTurn -> "Ждем своего хода"
                playerState is PlayerState.ChoosingTickets -> "Сначала надо выбрать маршруты"
                else -> null
            }
            styledDiv {
                css {
                    +ComponentStyles.cards
                }
                val chosenCardIx = (playerState as? PickedFirstCard)?.chosenCardIx
                for ((ix, card) in openCards.withIndex()) {
                    val tooltipForCard = disabledTooltip
                        ?: if (card is Card.Loco && chosenCardIx != null) "Уже выбрана другая карта, локомотив брать нельзя"
                        else null
                    openCard(card, canPickCards, ix, chosenCardIx, tooltipForCard) {
                        act { pickedOpenCard(ix) }
                    }
                }
                closedCard(disabledTooltip) {
                    act { pickedClosedCard() }
                }
            }
            styledDiv {
                val tooltipForTickets = disabledTooltip ?: if (lastRound) "Идет последний круг" else null
                ticketsCard(tooltipForTickets) {
                    act { pickedTickets() }
                }
            }
        }
    }

    private object ComponentStyles : StyleSheet("CardsDeck", isStatic = true) {
        val cardsDeck by css {
            gridColumnStart = GridColumnStart("1")
            gridColumnEnd = GridColumnEnd("4")
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
