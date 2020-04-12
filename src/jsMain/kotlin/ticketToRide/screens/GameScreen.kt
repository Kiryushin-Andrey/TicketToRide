package ticketToRide.screens

import kotlinx.css.*
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import styled.StyleSheet
import styled.css
import styled.styledDiv
import ticketToRide.GameId
import ticketToRide.GameStateView
import ticketToRide.components.CardsDeck
import ticketToRide.components.MainMapBlock
import ticketToRide.components.PlayersList

external interface GameScreenProps : RProps {
    var gameId: GameId
    var gameState: GameStateView
}

class GameScreen(props: GameScreenProps) : RComponent<GameScreenProps, RState>(props) {
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
            child(PlayersList::class) {
                attrs {
                    players = props.gameState.players
                }
            }
            child(MainMapBlock::class) {}
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
    }
}