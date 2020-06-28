package ticketToRide

import ticketToRide.Response.*
import ticketToRide.PlayerAction.*

fun PlayerAction.chatMessage(locale: Locale) = with(str(locale)) {
    when (this@chatMessage) {
        is JoinGame ->
            ChatMessage(playerName, hello)
        is LeaveGame ->
            ChatMessage(playerName, disconnected)
        is ConfirmTicketsChoice ->
            ChatMessage(playerName, confirmTicketsChoice(ticketsToKeep))
        is PickCards.Loco ->
            ChatMessage(playerName, pickLocoCard)
        is PickCards.TwoCards ->
            ChatMessage(
                playerName,
                if (cards.first is PickedCard.Closed && cards.second is PickedCard.Closed) pickTwoFaceDownCards
                else pickCards(cards.first.getName(locale) to cards.second.getName(locale))
            )
        is PickTickets ->
            ChatMessage(playerName, pickTickets)
        is BuildSegment -> {
            val cardsByCount = cards.groupingBy { it }.eachCount().toList()
                .joinToString { (card, count) -> "$count ${card.getName(locale)}" }
            ChatMessage(
                playerName,
                buildSegment(Triple(from.value, to.value, cardsByCount))
            )
        }
        is BuildStation -> ChatMessage(playerName, buildStation(target.value))
    }
}

fun PickedCard.getName(locale: Locale) = when (this) {
    is PickedCard.Closed -> str(locale).closedCard
    is PickedCard.Open -> card.getName(locale)
}

fun Card.getName(locale: Locale) = when (this) {
    is Card.Loco -> str(locale).loco
    is Card.Car -> str(locale).cardColor(color)
}

private class Strings(getLocale: () -> Locale) : LocalizedStrings(getLocale) {

    val closedCard by loc(
        Locale.En to "face down",
        Locale.Ru to "закрытая"
    )

    val loco by loc(
        Locale.En to "locomotive",
        Locale.Ru to "паровоз"
    )

    val cardColor by loc(
        Locale.En to { color: CardColor ->
            when (color) {
                CardColor.RED -> "red"
                CardColor.GREEN -> "green"
                CardColor.BLUE -> "blue"
                CardColor.BLACK -> "black"
                CardColor.WHITE -> "white"
                CardColor.YELLOW -> "yellow"
                CardColor.ORANGE -> "orange"
                CardColor.MAGENTO -> "magento"
            }
        },
        Locale.Ru to { color: CardColor ->
            when (color) {
                CardColor.RED -> "красная"
                CardColor.GREEN -> "зеленая"
                CardColor.BLUE -> "синяя"
                CardColor.BLACK -> "черная"
                CardColor.WHITE -> "белая"
                CardColor.YELLOW -> "желтая"
                CardColor.ORANGE -> "оранжевая"
                CardColor.MAGENTO -> "фиолетовая"
            }
        }
    )

    val hello by loc(
        Locale.En to "hello :)",
        Locale.Ru to "всем привет :)"
    )

    val disconnected by loc(
        Locale.En to "disconnected",
        Locale.Ru to "отключился"
    )

    val confirmTicketsChoice by locWithParam<Int>(
        Locale.En to { n -> "I keep $n tickets" },
        Locale.Ru to { n -> "оставляю $n маршрутов" }
    )

    val pickLocoCard by loc(
        Locale.En to "I pick loco card",
        Locale.Ru to "беру паровоз"
    )

    val pickCards by locWithParam<Pair<String, String>>(
        Locale.En to { (first, second) -> "I pick $first and $second cards" },
        Locale.Ru to { (first, second) -> "беру карты - $first и $second" }
    )

    val pickTwoFaceDownCards by loc(
        Locale.En to "I pick two face down cards",
        Locale.Ru to "беру две закрытые карты"
    )

    val pickTickets by loc(
        Locale.En to "I pick more tickets",
        Locale.Ru to "беру еще маршруты"
    )

    val buildSegment by locWithParam<Triple<String, String, String>>(
        Locale.En to { (from, to, cards) -> "I build $from - $to segment by using $cards cards" },
        Locale.Ru to { (from, to, cards) -> "строю участок $from - $to и сбрасываю карты: $cards" }
    )

    val buildStation by locWithParam<String>(
        Locale.En to { city -> "I put a station to $city" },
        Locale.Ru to { city -> "ставлю станцию в $city" }
    )
}

private val str = mapOf(
    Locale.En to Strings { Locale.En },
    Locale.Ru to Strings { Locale.Ru }
)

private fun str(locale: Locale) = str[locale] ?: throw Error("Locale $locale not supported")