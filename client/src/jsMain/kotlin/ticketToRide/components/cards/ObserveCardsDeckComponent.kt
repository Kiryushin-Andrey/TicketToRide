package ticketToRide.components.cards

import csstype.*
import emotion.react.css
import react.*
import react.dom.html.ReactHTML.div
import ticketToRide.Card
import ticketToRide.Locale

external interface ObserveCardsDeckComponentProps : Props {
    var openCards: List<Card>
    var locale: Locale
}

private val ObserveCardsDeckComponent = FC<ObserveCardsDeckComponentProps> { props ->
    div {
        css {
            height = 100.pct
            display = Display.flex
            flexDirection = FlexDirection.row
            flexWrap = FlexWrap.wrap
            justifyContent = JustifyContent.flexStart
        }

        for (card in props.openCards) {
            openCardForObserver(card, props.locale)
        }
        closedCardForObserver(props.locale)
    }
}

fun ChildrenBuilder.observeCardsDeck(openCards: List<Card>, locale: Locale) {
    ObserveCardsDeckComponent {
        this.openCards = openCards
        this.locale = locale
    }
}
