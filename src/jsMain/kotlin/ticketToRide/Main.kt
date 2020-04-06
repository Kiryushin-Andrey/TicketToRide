package ticketToRide

import react.dom.*
import kotlin.browser.*
import kotlinext.js.*
import kotlinx.css.*
import styled.*
import google.map.react.*
import ticketToRide.components.MainMapBlock

object ComponentStyles : StyleSheet("ComponentStyles", isStatic = true) {
    val playersList by css {
        width = 30.pct
    }
    val map by css {
        width = 90.pct
        height = 100.pct
    }
}

fun main() {
    render(document.getElementById("root")) {
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
}