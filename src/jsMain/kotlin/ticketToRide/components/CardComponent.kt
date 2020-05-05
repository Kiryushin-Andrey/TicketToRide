package ticketToRide.components

import com.ccfraser.muirwik.components.mPaper
import com.ccfraser.muirwik.components.mTooltip
import kotlinx.css.*
import kotlinx.css.properties.transform
import kotlinx.css.properties.translate
import react.*
import react.dom.img
import styled.css
import styled.styledImg
import ticketToRide.Card

interface CardProps : RProps {
    var imageUrl: String
    var color: Color?
    var enabled: Boolean
    var checked: Boolean
    var tooltip: String?
    var onClick: () -> Unit
}

interface CardState : RState {
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
                position = Position.relative
                padding = "8px"
                margin = "8px"
                borderColor = Color.black
                borderStyle = BorderStyle.solid
                borderWidth = 1.px
                cursor = if (props.enabled) Cursor.pointer else Cursor.notAllowed
                if (props.color != null) {
                    backgroundColor = props.color!!
                }
                if (props.enabled && state.hovered) {
                    transform { translate(3.px, (-3).px) }
                }
            }
            mTooltip(props.tooltip ?: "") {
                attrs {
                    disableHoverListener = props.enabled
                }
                img {
                    attrs {
                        src = props.imageUrl
                        width = "140px"
                        height = "70px"
                    }
                }
            }
            if (props.checked) {
                styledImg {
                    css {
                        position = Position.absolute
                        left = 130.px
                        top = (-10).px
                    }
                    attrs {
                        src = "/icons/card-check.svg"
                        width = 32.px.toString()
                        height = 32.px.toString()
                    }
                }
            }
        }
    }
}

private fun RBuilder.card(
    disabledTooltip: String? = null,
    builder: CardProps.() -> Unit
) = child(CardComponent::class) {
    attrs {
        tooltip = disabledTooltip
        builder()
    }
}

fun RBuilder.openCard(
    card: Card,
    canPickCards: Boolean,
    cardIx: Int,
    chosenCardIx: Int?,
    disabledTooltip: String?,
    clickHandler: () -> Unit
): ReactElement {
    return card(disabledTooltip) {
        enabled = canPickCards && (card is Card.Car || chosenCardIx == null)
        checked = cardIx == chosenCardIx
        imageUrl = "/cards/" + when (card) {
            is Card.Loco -> "loco.jpg"
            is Card.Car -> when (card.color) {
                ticketToRide.CardColor.RED -> "red.jpg"
                ticketToRide.CardColor.GREEN -> "green.jpg"
                ticketToRide.CardColor.BLUE -> "blue.jpg"
                ticketToRide.CardColor.BLACK -> "black.jpg"
                ticketToRide.CardColor.WHITE -> "white.jpg"
                ticketToRide.CardColor.YELLOW -> "yellow.jpg"
                ticketToRide.CardColor.ORANGE -> "orange.jpg"
                ticketToRide.CardColor.MAGENTO -> "magento.jpg"
            }
        }
        if (card is Card.Car) {
            color = Color(card.color.rgb).withAlpha(0.5)
        }
        onClick = clickHandler
    }
}

fun RBuilder.closedCard(disabledTooltip: String?, clickHandler: () -> Unit) =
    card(disabledTooltip) {
        imageUrl = "/cards/faceDown.png"
        color = blackAlpha(0.1)
        this.enabled = (disabledTooltip == null)
        onClick = clickHandler
    }

fun RBuilder.ticketsCard(disabledTooltip: String?, clickHandler: () -> Unit) {
    card(disabledTooltip) {
        imageUrl = "/cards/routeFaceDown.png"
        color = blackAlpha(0.1)
        enabled = (disabledTooltip == null)
        onClick = clickHandler
    }
}
