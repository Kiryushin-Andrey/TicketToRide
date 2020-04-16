package ticketToRide.screens

import com.ccfraser.muirwik.components.MDividerVariant
import com.ccfraser.muirwik.components.mDivider
import kotlinx.css.*
import react.*
import styled.StyleSheet
import styled.css
import styled.styledDiv
import ticketToRide.GameId
import ticketToRide.GameMap
import ticketToRide.GameStateView
import ticketToRide.Ticket
import ticketToRide.components.CardsDeck
import ticketToRide.components.MainMapBlock
import ticketToRide.components.MyTickets
import ticketToRide.components.PlayersList

external interface GameScreenProps : RProps {
    var gameId: GameId
    var gameState: GameStateView
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
            child(CardsDeck::class) {
                attrs {
                    openCards = props.gameState.openCards
                }
            }
            styledDiv {
                css {
                    +ComponentStyles.leftPanel
                }
                child(PlayersList::class) {
                    attrs {
                        players = props.gameState.players
                    }
                }
                mDivider(variant = MDividerVariant.fullWidth)
                child(MyTickets::class) {
                    attrs {
                        tickets = props.gameState.myTicketsOnHand
                        pendingChoice = props.gameState.myPendingTicketsChoice
                        hoveredTicket = state.selectedTicket
                        onHoveredTicketChanged = { setState { selectedTicket = it } }
                    }
                }
            }
            child(MainMapBlock::class) {
                attrs {
                    gameMap = GameMap
                    selectedTicket = state.selectedTicket
                }
            }
        }
    }

    private object ComponentStyles : StyleSheet("GameScreen", isStatic = true) {
        val screen by css {
            height = 100.pct
            width = 100.pct
            display = Display.grid
            gridTemplateColumns = GridTemplateColumns(GridAutoRows(400.px), GridAutoRows.auto)
            gridTemplateRows = GridTemplateRows(GridAutoRows(120.px), GridAutoRows.auto)
            gridTemplateAreas = GridTemplateAreas("cards cards")
        }
        val leftPanel by css {
            display = Display.flex
            flexDirection = FlexDirection.column
            flexWrap = FlexWrap.nowrap
        }
    }
}