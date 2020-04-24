package ticketToRide.components

import kotlinx.css.*
import react.RBuilder
import react.RState
import styled.css
import styled.styledDiv

class MyCardsComponent : ComponentBase<ComponentBaseProps, RState>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                display = Display.flex
                flexDirection = FlexDirection.row
                flexWrap = FlexWrap.wrap
            }
            for ((card, count) in myCards.groupingBy { it }.eachCount()) {
                styledDiv {
                    css {
                        display = Display.flex
                        flexDirection = FlexDirection.row
                        flexWrap = FlexWrap.nowrap
                        alignItems = Align.center
                        margin = 12.px.toString()
                    }
                    myCard(card)
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

fun RBuilder.myCards(props: ComponentBaseProps) = child(MyCardsComponent::class) {
    attrs {
        this.gameState = props.gameState
        this.playerState = props.playerState
        this.onAction = props.onAction
    }
}