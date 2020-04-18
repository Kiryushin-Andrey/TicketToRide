package ticketToRide.components

import com.ccfraser.muirwik.components.mPaper
import kotlinx.css.*
import react.*
import react.dom.img
import styled.css
import ticketToRide.Card

external interface CardProps : RProps {
    var imageUrl: String
    var color: Color?
    var enabled: Boolean
    var onClick: () -> Unit
}

external interface CardState : RState {
    var hovered: Boolean
}

class CardComponent : RComponent<CardProps, CardState>() {
    override fun RBuilder.render() {
        mPaper {
            attrs {
                elevation = if (props.enabled && state.hovered) 8 else 4
                with(asDynamic()) {
                    onMouseOver = { setState { hovered = true } }
                    onMouseOut = { setState { hovered = false } }
                    onClick = if (props.enabled) props.onClick else null
                }
            }
            css {
                padding = "8px"
                margin = "8px"
                borderColor = Color.black
                borderStyle = BorderStyle.solid
                borderWidth = 1.px
                cursor = if (props.enabled) Cursor.pointer else Cursor.default
                if (props.color != null) {
                    backgroundColor = props.color!!
                }
            }
            img {
                attrs {
                    src = props.imageUrl
                    width = "140px"
                    height = "70px"
                }
            }
        }
    }
}

fun RBuilder.card(builder: CardProps.() -> Unit): ReactElement {
    return child(CardComponent::class) {
        attrs {
            builder()
        }
    }
}

fun RBuilder.card(card: Card, builder: CardProps.() -> Unit) = card {
    imageUrl = "/cards/" + when (card) {
        is Card.Loco -> "loco.jpg"
        is Card.Car -> when (card.value) {
            ticketToRide.Color.RED -> "red.png"
            ticketToRide.Color.GREEN -> "green.png"
            ticketToRide.Color.BLUE -> "blue.jpg"
            ticketToRide.Color.BLACK -> "black.jfif"
            ticketToRide.Color.WHITE -> "white.jfif"
            ticketToRide.Color.YELLOW -> "yellow.jfif"
            ticketToRide.Color.ORANGE -> "orange.jpg"
            ticketToRide.Color.MAGENTO -> "magento.jfif"
        }
    }
    if (card is Card.Car) {
        color = Color(card.value.rgb).withAlpha(0.5)
    }
    builder()
}