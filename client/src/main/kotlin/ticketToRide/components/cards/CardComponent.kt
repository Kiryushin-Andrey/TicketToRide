package ticketToRide.components.cards

import com.ccfraser.muirwik.components.*
import kotlinx.css.*
import kotlinx.css.properties.*
import react.*
import react.dom.*
import styled.*
import ticketToRide.Card
import ticketToRide.Locale
import ticketToRide.LocalizedStrings
import ticketToRide.getName

external interface CardComponentProps : RProps {
    var locale: Locale
    var imageUrl: String
    var assignedKey: String?
    var color: Color?
    var observing: Boolean
    var enabled: Boolean
    var checked: Boolean
    var tooltip: String?
    var onClick: () -> Unit
}

external interface CardComponentState : RState {
    var hovered: Boolean
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
class CardComponent : RComponent<CardComponentProps, CardComponentState>() {

    override fun RBuilder.render() {
        mPaper {
            attrs {
                elevation = if (props.enabled && state.hovered) 8 else 4
                if (!props.observing) {
                    with(asDynamic()) {
                        onMouseOver = { setState { hovered = true } }
                        onMouseOut = { setState { hovered = false } }
                        onClick = { if (props.enabled) props.onClick() }
                    }
                }
            }
            css {
                position = Position.relative
                padding = "8px"
                margin = "8px"
                borderColor = Color.black
                borderStyle = BorderStyle.solid
                borderWidth = 1.px
                if (!props.observing) {
                    cursor = if (props.enabled) Cursor.pointer else Cursor.notAllowed
                }
                if (props.color != null) {
                    backgroundColor = props.color!!
                }
                if (props.enabled && state.hovered) {
                    transform { translate(3.px, (-3).px) }
                }
            }

            mTooltip(tooltip) {
                img {
                    attrs {
                        src = props.imageUrl
                        width = "140px"
                        height = "70px"
                    }
                }
            }

            if (!props.observing) {
                checkedMark()
                assignedKeyHint()
            }
        }
    }

    private val tooltip
        get() = props.tooltip?.let { msg ->
            if (props.enabled) props.assignedKey?.let { "$msg (${str.keyTip(it)})" } ?: msg else msg
        } ?: ""

    private fun RBuilder.checkedMark() {
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

    private fun RBuilder.assignedKeyHint() {
        props.assignedKey?.let { ch ->
            styledDiv {
                css {
                    position = Position.absolute
                    left = 5.px
                    top = 80.px
                }
                +ch
            }
        }
    }

    private val str = Strings { props.locale }
}


private class Strings(getLocale: () -> Locale) : LocalizedStrings(getLocale) {

    val keyTip by locWithParam<String>(
        Locale.En to { key -> "key $key" },
        Locale.Ru to { key -> "клавиша $key" }
    )

    val takeOneClosedCard by loc(
        Locale.En to "Take face down card",
        Locale.Ru to "Взять закрытую карту"
    )

    val takeTwoClosedCards by loc(
        Locale.En to "Take two face down cards",
        Locale.Ru to "Взять две закрытые карты"
    )

    val takeTickets by loc(
        Locale.En to "Take tickets",
        Locale.Ru to "Взять маршруты"
    )
}

private fun RBuilder.card(card: Card?, locale: Locale, builder: CardComponentProps.() -> Unit) =
    child(CardComponent::class) {
        attrs {
            this.locale = locale
            this.imageUrl = "/cards/" + when (card) {
                null -> "faceDown.png"
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
            this.color = when (card) {
                null -> blackAlpha(0.1)
                is Card.Loco -> Color.white
                is Card.Car -> Color(card.color.rgb).withAlpha(0.5)
            }
            this.tooltip = card?.getName(locale)
            builder()
        }
    }

fun RBuilder.openCardForObserver(card: Card, locale: Locale): ReactElement {
    return card(card, locale) {
        observing = true
    }
}

fun RBuilder.openCard(
    card: Card,
    locale: Locale,
    canPickCards: Boolean,
    cardIx: Int,
    chosenCardIx: Int?,
    disabledTooltip: String?,
    clickHandler: () -> Unit
): ReactElement {
    return card(card, locale) {
        this.assignedKey = (cardIx + 1).toString()
        tooltip = disabledTooltip ?: card.getName(locale)
        enabled = canPickCards && (card is Card.Car || chosenCardIx == null)
        checked = cardIx == chosenCardIx
        onClick = clickHandler
    }
}

fun RBuilder.closedCardForObserver(locale: Locale) =
    card(null, locale) {
        observing = true
    }

fun RBuilder.closedCard(locale: Locale, disabledTooltip: String?, hasChosenCard: Boolean, clickHandler: () -> Unit) =
    card(null, locale) {
        assignedKey = "0"
        tooltip = disabledTooltip
            ?: Strings { locale }.run { if (hasChosenCard) takeOneClosedCard else takeTwoClosedCards }
        this.enabled = (disabledTooltip == null)
        onClick = clickHandler
    }

fun RBuilder.ticketsCard(locale: Locale, disabledTooltip: String?, clickHandler: () -> Unit) =
    child(CardComponent::class) {
        attrs {
            this.locale = locale
            imageUrl = "/cards/routeFaceDown.png"
            color = blackAlpha(0.1)
            tooltip = disabledTooltip ?: Strings { locale }.takeTickets
            enabled = (disabledTooltip == null)
            onClick = clickHandler
        }
    }