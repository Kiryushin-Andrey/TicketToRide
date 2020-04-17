package ticketToRide.components

import com.ccfraser.muirwik.components.mPaper
import kotlinx.css.*
import react.*
import react.dom.img
import styled.css

external interface CardProps : RProps {
    var imageUrl: String
    var color: Color?
    var enabled: Boolean
    var onClick: () -> Unit
}

external interface CardState : RState {
    var hovered: Boolean
}

class Card : RComponent<CardProps, CardState>() {
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
    return child(Card::class) {
        attrs {
            builder()
        }
    }
}