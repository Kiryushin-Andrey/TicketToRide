package ticketToRide.components.cards

import csstype.*
import emotion.react.css
import kotlinx.browser.document
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import react.FC
import react.dom.html.ReactHTML.div
import react.useCallback
import react.useEffect
import react.useMemo
import ticketToRide.*
import ticketToRide.components.*
import ticketToRide.PlayerState
import ticketToRide.PlayerState.MyTurn.PickedFirstCard

val CardsDeckComponent = FC<GameComponentProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }
    val chosenCardIx = useMemo(props.playerState) { (props.playerState as? PickedFirstCard)?.chosenCardIx }

    val getDisabledTooltip = useCallback(props, str) { cardIx: Int? ->
        when {
            !props.connected ->
                str.disconnected
            !props.myTurn ->
                str.waitingForYouTurn
            props.playerState is PlayerState.ChoosingTickets ->
                str.chooseTicketsFirst
            cardIx != null && chosenCardIx != null && props.openCards[cardIx] is Card.Loco ->
                str.cannotTakeLocoWithAnotherCard
            else ->
                null
        }
    }

    val onKeyPress = useCallback(props) { e: Event ->
        with(e as KeyboardEvent) {
            if (props.canPickCards && charCode.toChar() in ('0'..'5')) {
                val cardIx = (key.toInt() - 1).takeIf { it >= 0 }
                if (getDisabledTooltip(cardIx) == null)
                    props.act { if (cardIx != null) pickedOpenCard(cardIx) else pickedClosedCard() }
            }
        }
    }

    useEffect(onKeyPress) {
        document.addEventListener("keypress", onKeyPress)
        cleanup {
            document.removeEventListener("keypress", onKeyPress)
        }
    }

    div {
        css {
            height = 100.pct
            display = Display.flex
            flexDirection = FlexDirection.row
            flexWrap = FlexWrap.wrap
            justifyContent = JustifyContent.spaceBetween
        }
        div {
            css {
                display = Display.flex
                flexDirection = FlexDirection.row
                flexWrap = FlexWrap.wrap
                justifyContent = JustifyContent.flexStart
            }
            for ((ix, card) in props.openCards.withIndex()) {
                openCard(card, props.locale, props.canPickCards, ix, chosenCardIx, getDisabledTooltip(ix)) {
                    props.act { pickedOpenCard(ix) }
                }
            }
            closedCard(props.locale, getDisabledTooltip(null), chosenCardIx != null) {
                props.act { pickedClosedCard() }
            }
        }
        div {
            val tooltipForTickets = getDisabledTooltip(null) ?: if (props.lastRound) str.lastRound else null
            ticketsCard(props.locale, tooltipForTickets) {
                props.act { pickedTickets() }
            }
        }
    }
}

private fun strings(locale: Locale) = object : LocalizedStrings({ locale }) {

    val disconnected by loc(
        Locale.En to "Server connection lost",
        Locale.Ru to "Нет соединения с сервером"
    )

    val waitingForYouTurn by loc(
        Locale.En to "Wait for you turn to move",
        Locale.Ru to "Ждем своего хода"
    )

    val chooseTicketsFirst by loc(
        Locale.En to "Choose tickets first",
        Locale.Ru to "Сначала надо выбрать маршруты"
    )

    val cannotTakeLocoWithAnotherCard by loc(
        Locale.En to "You cannot take loco together with another card",
        Locale.Ru to "Уже выбрана другая карта, локомотив брать нельзя"
    )

    val lastRound by loc(
        Locale.En to "Last round",
        Locale.Ru to "Идет последний круг"
    )
}
