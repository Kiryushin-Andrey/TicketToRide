package ticketToRide.components

import com.ccfraser.muirwik.components.mPaper
import kotlinx.css.*
import react.*
import styled.*
import ticketToRide.Card

external interface MyCardsProps : RProps {
    var cards: List<Card>
    var myTurn: Boolean
}

class MyCardsComponent : RComponent<MyCardsProps, RState>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                display = Display.flex
                flexDirection = FlexDirection.row
                flexWrap = FlexWrap.wrap
            }
            for ((card, count) in props.cards.groupingBy { it }.eachCount()) {
                styledDiv {
                    css {
                        display = Display.flex
                        flexDirection = FlexDirection.row
                        flexWrap = FlexWrap.nowrap
                        alignItems = Align.center
                        margin = 12.px.toString()
                    }
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
                                backgroundColor = Color(card.value.rgb).withAlpha(0.4)
                            } else {
                                background = "linear-gradient(to right, orange , yellow, green, cyan, blue, violet)"
                            }
                        }
                    }
                    styledDiv {
                        css {
                            put("font-size", "large")
                        }
                        +count.toString()
                    }
                }
            }
        }
    }
}