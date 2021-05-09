package ticketToRide.components.cards

import com.ccfraser.muirwik.components.mPaper
import com.ccfraser.muirwik.components.mTooltip
import kotlinx.css.*
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import styled.css
import ticketToRide.Card
import ticketToRide.Locale
import ticketToRide.getName

external interface MyCardComponentProps : RProps {
    var card: Card
    var locale: Locale
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
class MyCardComponent : RComponent<MyCardComponentProps, RState>() {

    override fun RBuilder.render() {
        val card = props.card
        mTooltip(card.getName(props.locale)) {
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
                        backgroundColor = Color(card.color.rgb)
                    } else {
                        background = "linear-gradient(to right, orange , yellow, green, cyan, blue, violet)"
                    }
                }
            }
        }
    }
}

fun RBuilder.myCard(card: Card, locale: Locale) {
    child(MyCardComponent::class) {
        attrs {
            this.card = card
            this.locale = locale
        }
    }
}