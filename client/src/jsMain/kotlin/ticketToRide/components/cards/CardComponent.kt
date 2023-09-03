package ticketToRide.components.cards

import csstype.*
import emotion.react.css
import mui.material.Paper
import mui.material.Tooltip
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import ticketToRide.Card
import ticketToRide.Locale
import ticketToRide.LocalizedStrings
import ticketToRide.getName

external interface CardComponentProps : Props {
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

val CardComponent = FC<CardComponentProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }
    var hovered by useState(false)

    Paper {
        elevation = if (props.enabled && hovered) 8 else 4
        sx {
            position = Position.relative
            padding = 8.px
            margin = 8.px
            borderColor = NamedColor.black
            borderStyle = LineStyle.solid
            borderWidth = 1.px
            if (!props.observing) {
                cursor = if (props.enabled) Cursor.pointer else Cursor.notAllowed
            }
            if (props.color != null) {
                backgroundColor = props.color!!
            }
            if (props.enabled && hovered) {
                transform = translate(3.px, (-3).px)
            }
        }

        if (!props.observing) {
            onMouseOver = { hovered = true }
            onMouseOut = { hovered = false }
            onClick = { if (props.enabled) props.onClick() }
        }

        Tooltip {
            val tooltip = props.tooltip?.let { msg ->
                if (props.enabled) props.assignedKey?.let { "$msg (${str.keyTip(it)})" } ?: msg else msg
            } ?: ""
            title = ReactNode(tooltip)

            img {
                src = props.imageUrl
                width = 140.0
                height = 70.0
            }
        }

        if (!props.observing) {
            if (props.checked) {
                checkedMark()
            }
            props.assignedKey?.let {
                assignedKeyHint(it)
            }
        }
    }
}

private fun ChildrenBuilder.checkedMark() {
    img {
        css {
            position = Position.absolute
            left = 130.px
            top = (-10).px
        }
        src = "/icons/card-check.svg"
        width = 32.0
        height = 32.0
    }
}

private fun ChildrenBuilder.assignedKeyHint(key: String) {
    div {
        css {
            position = Position.absolute
            left = 5.px
            top = 80.px
        }
        +key
    }
}

private fun strings(locale: Locale) = object : LocalizedStrings({ locale }) {

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

private fun ChildrenBuilder.card(card: Card?, locale: Locale, builder: CardComponentProps.() -> Unit) =
    CardComponent {
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
            null -> rgba(red = 0, green = 0, blue = 0, alpha = 0.1)
            is Card.Loco -> NamedColor.white
            is Card.Car -> Color(card.color.rgb + "7F")
        }
        this.tooltip = card?.getName(locale)
        builder()
    }

fun ChildrenBuilder.openCardForObserver(card: Card, locale: Locale) =
    card(card, locale) { observing = true }

fun ChildrenBuilder.openCard(
    card: Card,
    locale: Locale,
    canPickCards: Boolean,
    cardIx: Int,
    chosenCardIx: Int?,
    disabledTooltip: String?,
    clickHandler: () -> Unit
) =
    card(card, locale) {
        this.assignedKey = (cardIx + 1).toString()
        tooltip = disabledTooltip ?: card.getName(locale)
        enabled = canPickCards && (card is Card.Car || chosenCardIx == null)
        checked = cardIx == chosenCardIx
        onClick = clickHandler
    }

fun ChildrenBuilder.closedCardForObserver(locale: Locale) =
    card(null, locale) {
        observing = true
    }

fun ChildrenBuilder.closedCard(locale: Locale, disabledTooltip: String?, hasChosenCard: Boolean, clickHandler: () -> Unit) =
    card(null, locale) {
        assignedKey = "0"
        tooltip = disabledTooltip
            ?: strings(locale).run { if (hasChosenCard) takeOneClosedCard else takeTwoClosedCards }
        this.enabled = (disabledTooltip == null)
        onClick = clickHandler
    }

fun ChildrenBuilder.ticketsCard(locale: Locale, disabledTooltip: String?, clickHandler: () -> Unit) =
    CardComponent {
        this.locale = locale
        imageUrl = "/cards/routeFaceDown.png"
        color = rgba(red = 0, green = 0, blue = 0, alpha = 0.1)
        tooltip = disabledTooltip ?: strings(locale).takeTickets
        enabled = (disabledTooltip == null)
        onClick = clickHandler
    }