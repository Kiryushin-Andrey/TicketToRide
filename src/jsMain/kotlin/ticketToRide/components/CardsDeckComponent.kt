package ticketToRide.components

import kotlinx.css.*
import react.*
import styled.StyleSheet
import styled.css
import styled.styledDiv
import ticketToRide.Card
import ticketToRide.Color

external interface CardsDeckProps : RProps {
    var myTurn: Boolean
    var openCards: List<Card>
    var onPickTickets: () -> Unit
    var onPickLoco: () -> Unit
    var onPickCards: (Pair<Card?, Card?>) -> Unit
}

external interface CardsDeckState : RState {
    var chosenCard: Int?
}

class CardsDeck : RComponent<CardsDeckProps, CardsDeckState>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                +ComponentStyles.cardsDeck
            }
            styledDiv {
                css {
                    +ComponentStyles.cards
                }
                for ((ix, card) in props.openCards.withIndex()) {
                    card(card) {
                        enabled = props.myTurn && (card is Card.Car || state.chosenCard == null)
                        onClick = {
                            when {
                                card is Card.Loco -> props.onPickLoco()
                                state.chosenCard != null && state.chosenCard != ix -> props.onPickCards(
                                    Pair(props.openCards[state.chosenCard!!], card)
                                )
                            }
                            setState {
                                chosenCard = if (chosenCard == null && card is Card.Car) ix else null
                            }
                        }
                    }
                }
                card {
                    imageUrl = "/cards/faceDown.png"
                    color = blackAlpha(0.1)
                    enabled = props.myTurn
                    onClick = {
                        props.onPickCards(
                            Pair(
                                if (state.chosenCard != null) props.openCards[state.chosenCard!!] else null,
                                null
                            )
                        )
                    }
                }
            }
            styledDiv {
                card {
                    imageUrl = "/cards/routeFaceDown.png"
                    color = blackAlpha(0.1)
                    enabled = props.myTurn
                    onClick = props.onPickTickets
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