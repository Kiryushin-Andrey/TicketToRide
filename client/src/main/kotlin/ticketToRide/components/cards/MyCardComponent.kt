package ticketToRide.components.cards

import csstype.*
import emotion.react.css
import mui.material.Paper
import mui.material.Tooltip
import mui.system.sx
import react.*
import ticketToRide.Card
import ticketToRide.Locale
import ticketToRide.getName

external interface MyCardComponentProps : Props {
    var card: Card
    var locale: Locale
}

private val MyCardComponent = FC<MyCardComponentProps> { props ->
    val card = props.card
    Tooltip {
        title = ReactNode(card.getName(props.locale))

        Paper {
            elevation = 4
            sx {
                padding = 12.px
                marginRight = 6.px
                height = 6.px
                borderColor = NamedColor.black
                borderStyle = LineStyle.solid
                borderWidth = 1.px
                if (card is Card.Car) {
                    backgroundColor = Color(card.color.rgb)
                } else {
                    background = linearGradient(
                        NamedColor.orange, NamedColor.yellow, NamedColor.green, NamedColor.cyan, NamedColor.blue, NamedColor.violet
                    )
                }
            }
        }
    }
}

fun ChildrenBuilder.myCard(card: Card, locale: Locale) {
    MyCardComponent {
        this.card = card
        this.locale = locale
    }
}
