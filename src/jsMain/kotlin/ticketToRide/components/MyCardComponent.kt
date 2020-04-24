package ticketToRide.components

import com.ccfraser.muirwik.components.mPaper
import kotlinx.css.*
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import styled.css
import ticketToRide.Card

interface MyCardProps : RProps {
    var card: Card
}

class MyCardComponent : RComponent<MyCardProps, RState>() {
    override fun RBuilder.render() {
        val card = props.card
        mPaper {
            attrs {
                elevation = 4
            }
            css {
                padding = 12.px.toString()
                marginRight = 6.px
                height = 6.px
                borderColor = Color.black
                borderStyle = BorderStyle.solid
                borderWidth = 1.px
                if (card is Card.Car) {
                    backgroundColor = Color(card.color.rgb).withAlpha(0.4)
                } else {
                    background = "linear-gradient(to right, orange , yellow, green, cyan, blue, violet)"
                }
            }
        }
    }
}

fun RBuilder.myCard(card: Card) {
    child(MyCardComponent::class) {
        attrs {
            this.card = card
        }
    }
}