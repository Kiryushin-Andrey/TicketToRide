package ticketToRide.components.cards

import kotlinx.css.*
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import styled.StyleSheet
import styled.css
import styled.styledDiv
import ticketToRide.Card
import ticketToRide.Locale

class ObserveCardsDeckComponent : RComponent<ObserveCardsDeckComponent.Props, RState>() {

    interface Props : RProps {
        var openCards: List<Card>
        var locale: Locale
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                +ComponentStyles.cards
            }
            for (card in props.openCards) {
                openCardForObserver(card, props.locale)
            }
            closedCardForObserver(props.locale)
        }
    }

    private object ComponentStyles : StyleSheet("ObserveCardsDeck", isStatic = true) {
        val cards by css {
            height = 100.pct
            display = Display.flex
            flexDirection = FlexDirection.row
            flexWrap = FlexWrap.wrap
            justifyContent = JustifyContent.flexStart
        }
    }
}

fun RBuilder.observeCardsDeck(openCards: List<Card>, locale: Locale) {
    child(ObserveCardsDeckComponent::class) {
        attrs {
            this.openCards = openCards
            this.locale = locale
        }
    }
}