package ticketToRide.components.tickets

import com.ccfraser.muirwik.components.mPaper
import kotlinx.css.*
import react.*
import styled.StyleSheet
import styled.css

class PointsLabelComponent : RComponent<PointsLabelComponent.Props, RState>() {

    interface Props : RProps {
        var color: Color
        var text: String
    }

    override fun RBuilder.render() {
        mPaper {
            attrs { elevation = 4 }
            css {
                +ComponentStyles.pointsLabel
                marginLeft = 10.px
                backgroundColor = props.color
            }
            +props.text
        }
    }

    object ComponentStyles : StyleSheet("pointsLabel", isStatic = true) {
        val pointsLabel by css {
            width = 30.px
            height = 20.px
            textAlign = TextAlign.center
            verticalAlign = VerticalAlign.middle
            padding = 5.px.toString()
            borderRadius = 5.px
        }
    }
}

fun RBuilder.pointsLabel(text: String, color: Color) {
    child(PointsLabelComponent::class) {
        attrs {
            this.text = text
            this.color = color
        }
    }
}

fun RBuilder.pointsLabel(value: Int, color: Color) = pointsLabel(value.toString(), color)