package ticketToRide.components.tickets

import csstype.Color
import csstype.TextAlign
import csstype.VerticalAlign
import csstype.px
import emotion.react.css
import mui.material.Paper
import mui.system.sx
import react.*

external interface PointsLabelComponentProps : Props {
    var color: Color
    var text: String
}

val PointsLabelComponent = FC<PointsLabelComponentProps> { props ->
    Paper {
        elevation = 4
        sx {
            width = 30.px
            height = 20.px
            textAlign = TextAlign.center
            verticalAlign = VerticalAlign.middle
            padding = 5.px
            borderRadius = 5.px
            marginLeft = 10.px
            backgroundColor = props.color
        }
        +props.text
    }
}

fun ChildrenBuilder.pointsLabel(text: String, color: Color) {
    PointsLabelComponent {
        this.text = text
        this.color = color
    }
}

fun ChildrenBuilder.pointsLabel(value: Int, color: Color) {
    pointsLabel(value.toString(), color)
}
