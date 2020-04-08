package ticketToRide.components

import kotlinx.css.*
import react.*
import react.dom.*
import styled.*
import ticketToRide.*

external interface GameScreenProps : RProps {
    var gameState: GameState
}

class GameScreen() : RComponent<GameScreenProps, RState>() {
    override fun RBuilder.render() {
        styledDiv {
            css { +ComponentStyles.playersList }
            ul {
                li { +"Player1" }
                li { +"Player2" }
                li { +"Player3" }
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