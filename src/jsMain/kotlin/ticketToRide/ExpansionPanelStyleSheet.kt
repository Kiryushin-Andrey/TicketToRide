package ticketToRide

import kotlinx.css.*
import kotlinx.css.properties.BoxShadows
import styled.StyleSheet

open class ExpansionPanelStyleSheet(name: String) : StyleSheet(name, isStatic = true) {
    val expansionPanelRoot by css {
        borderStyle = BorderStyle.none
        boxShadow = BoxShadows.none
        before { display = Display.none }
        "&.Mui-expanded" {
            minHeight = 0.px
            margin = 0.px.toString()
        }
    }
    val expansionPanelSummaryRoot by css {
        "&.Mui-expanded" {
            minHeight = 0.px
            margin = 0.px.toString()
        }
    }
    val expansionPanelSummaryContent by css {
        margin = 0.px.toString()
        padding = 0.px.toString()
        "&.Mui-expanded" {
            minHeight = 0.px
            margin = 0.px.toString()
        }
    }
    val expansionPanelDetailsRoot by css {
        padding = 0.px.toString()
        flexDirection = FlexDirection.column
    }
    val expansionPanelExpanded by css {}
}