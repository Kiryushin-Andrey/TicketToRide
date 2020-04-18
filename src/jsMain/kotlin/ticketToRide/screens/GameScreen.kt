package ticketToRide.screens

import com.ccfraser.muirwik.components.MDividerVariant
import com.ccfraser.muirwik.components.mDivider
import kotlinx.css.*
import react.*
import styled.StyleSheet
import styled.css
import styled.styledDiv
import ticketToRide.*
import ticketToRide.components.*

external interface GameScreenProps : RProps {
    var gameState: GameStateView
    var sendRequest: (Request) -> Unit
}

external interface GameScreenState : RState {
    var selectedTicket: Ticket?
}

class GameScreen(props: GameScreenProps) : RComponent<GameScreenProps, GameScreenState>(props) {
    override fun RBuilder.render() {
        styledDiv {
            css {
                +ComponentStyles.screen
            }

            styledDiv {
                css {
                    +ComponentStyles.verticalPanel
                    put("resize", "horizontal")
                }
                child(PlayersList::class) {
                    attrs {
                        players = props.gameState.players
                        turn = props.gameState.turn
                    }
                }
                mDivider(variant = MDividerVariant.fullWidth) {
                    css {
                        margin = 5.px.toString()
                    }
                }
                child(ChatComponent::class) { }
            }

            child(MainMapBlock::class) {
                attrs {
                    gameMap = GameMap
                    selectedTicket = state.selectedTicket
                }
            }

            styledDiv {
                css {
                    +ComponentStyles.verticalPanel
                }
                child(MyCardsComponent::class) {
                    attrs {
                        cards = props.gameState.myCards
                        myTurn = props.gameState.myTurn
                    }
                }
                mDivider(variant = MDividerVariant.fullWidth) {
                    css {
                        margin = 5.px.toString()
                    }
                }
                child(MyTickets::class) {
                    attrs {
                        tickets = props.gameState.myTicketsOnHand
                        pendingChoice = props.gameState.myPendingTicketsChoice
                        hoveredTicket = state.selectedTicket
                        onHoveredTicketChanged = { setState { selectedTicket = it } }
                        onConfirmTicketsChoice = { tickets -> props.sendRequest(ConfirmTicketsChoiceRequest(tickets)) }
                    }
                }
            }

            child(CardsDeck::class) {
                attrs {
                    myTurn = props.gameState.myTurn
                    openCards = props.gameState.openCards
                    onPickLoco = {
                        props.sendRequest(PickCardsRequest.Loco)
                    }
                    onPickCards = {
                        props.sendRequest(PickCardsRequest.TwoCards(it))
                    }
                    onPickTickets = {
                        props.sendRequest(PickTicketsRequest)
                    }
                }
            }
        }
    }

    private object ComponentStyles : StyleSheet("GameScreen", isStatic = true) {
        val screen by css {
            height = 100.pct
            width = 100.pct
            display = Display.grid
            gridTemplateColumns = GridTemplateColumns(GridAutoRows("0.2fr"), GridAutoRows.auto, GridAutoRows(360.px))
            gridTemplateRows = GridTemplateRows(GridAutoRows.auto, GridAutoRows(120.px))
        }
        val verticalPanel by css {
            display = Display.flex
            flexDirection = FlexDirection.column
            flexWrap = FlexWrap.nowrap
            minWidth = 300.px
            minHeight = LinearDimension.minContent
            overflow = Overflow.auto
        }
    }
}