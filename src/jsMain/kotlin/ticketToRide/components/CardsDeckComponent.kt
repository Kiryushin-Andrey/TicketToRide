package ticketToRide.components

import kotlinx.css.*
import react.RBuilder
import react.RState
import styled.StyleSheet
import styled.css
import styled.styledDiv
import ticketToRide.playerState.*

class CardsDeck : ComponentBase<ComponentBaseProps, RState>() {
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
                    openCard(card, myTurn, canPickCards, ix, chosenCardIx) {
                        act { pickedOpenCard(ix) }
                    }
                }
                closedCard(myTurn, canPickCards) {
                    act { pickedClosedCard() }
                }
            }
            styledDiv {
                ticketsCard(myTurn, canPickCards, lastRound) {
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
