package ticketToRide.components

import com.ccfraser.muirwik.components.mPaper
import kotlinx.css.*
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.RDOMBuilder
import react.dom.img
import styled.StyleSheet
import styled.css
import styled.styledDiv
import ticketToRide.Card
import ticketToRide.Color
import ticketToRide.isLoko

external interface CardsDeckProps : RProps {
    var openCards: List<Card>
}

class CardsDeck : RComponent<CardsDeckProps, RState>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                +ComponentStyles.cardsDeck
            }
            styledDiv {
                css {
                    +ComponentStyles.cards
                }
                for (card in props.openCards) {
                    card(
                        "/cards/" + when (card.value) {
                            Color.NONE -> "loco.jpg"
                            Color.RED -> "red.png"
                            Color.GREEN -> "green.png"
                            Color.BLUE -> "blue.jpg"
                            Color.BLACK -> "black.jfif"
                            Color.WHITE -> "white.jfif"
                            Color.YELLOW -> "yellow.jfif"
                            Color.ORANGE -> "orange.jpg"
                            Color.MAGENTO -> "magento.jfif"
                        },
                        if (card.isLoko) null else Color(card.value.rgb).withAlpha(0.5)
                    )
                }
                card("/cards/faceDown.png", blackAlpha(0.1))
            }
            styledDiv {
                card("/cards/routeFaceDown.png", blackAlpha(0.1))
            }
        }
    }

    private fun RDOMBuilder<*>.card(imageUrl: String, color: kotlinx.css.Color?) {
        mPaper(elevation = 4) {
            css {
                +ComponentStyles.openCard
                if (color != null) {
                    backgroundColor = color
                }
            }
            img {
                attrs {
                    src = imageUrl
                    width = "140px"
                    height = "70px"
                }
            }
        }
    }

    private object ComponentStyles : StyleSheet("CardsDeck", isStatic = true) {
        val cardsDeck by css {
            gridColumnStart = GridColumnStart("1")
            gridColumnEnd = GridColumnEnd("3")
            display = Display.flex
            flexDirection = FlexDirection.row
            flexWrap = FlexWrap.wrap
            justifyContent = JustifyContent.spaceBetween
        }
        val cards by css {
            display = Display.flex
            flexDirection = FlexDirection.row
            flexWrap = FlexWrap.wrap
            justifyContent = JustifyContent.flexStart
        }
        val openCard by css {
            padding = "10px"
            margin = "10px"
            borderColor = kotlinx.css.Color.black
            borderStyle = BorderStyle.solid
            borderWidth = 1.px
            cursor = Cursor.grab
        }
    }
}