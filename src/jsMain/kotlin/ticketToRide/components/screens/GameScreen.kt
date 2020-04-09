package ticketToRide.components.screens

import kotlinx.css.*
import react.*
import react.dom.*
import styled.*
import ticketToRide.*
import ticketToRide.components.MainMapBlock

external interface GameScreenProps : RProps {
    var gameId: GameId
    var gameState: GameState
}

class GameScreen(props: GameScreenProps) : RComponent<GameScreenProps, RState>(props) {
    override fun RBuilder.render() {
        styledDiv {
            css { +ComponentStyles.playersList }
            ul {
                for (player in props.gameState.players) {
                    li { +player.name.value }
                }
            }
        }
        styledDiv {
            css { +ComponentStyles.map }
            child(MainMapBlock::class) {}
        }
    }

    private object ComponentStyles : StyleSheet("GameScreen", isStatic = true) {
        val playersList by css {
            width = 30.pct
        }
        val map by css {
            width = 90.pct
            height = 100.pct
        }
    }
}