package ticketToRide

import ticketToRide.Response.*
import ticketToRide.Response.PlayerAction.*
import kotlin.Error

fun PlayerAction.chatMessage() = when (this) {
    is JoinGame ->
        ChatMessage(playerName, "всем привет :)")
    is LeaveGame ->
        ChatMessage(playerName, "отключился")
    is ConfirmTicketsChoice ->
        ChatMessage(playerName, "оставляю $ticketsToKeep маршрутов")
    is PickCards.Loco ->
        ChatMessage(playerName, "беру паровоз")
    is PickCards.TwoCards ->
        ChatMessage(playerName,
            if (cards.first == null && cards.second == null) "беру две закрытые карты"
            else "беру карты - ${cards.first.name} и ${cards.second.name}")
    is PickTickets ->
        ChatMessage(playerName, "беру еще маршруты")
    is BuildSegment -> {
        val cardsByCount = cards.groupingBy { it }.eachCount().toList().joinToString { (card, count) -> "$count ${card.name}" }
        ChatMessage(
            playerName,
            "строю участок ${from.value} - ${to.value} и сбрасываю карты: $cardsByCount"
        )
    }
}

val Card?.name get() = when(this) {
    null -> "закрытая"
    is Card.Loco -> "паровоз"
    is Card.Car -> when (this.color) {
        Color.RED -> "красная"
        Color.GREEN -> "зеленая"
        Color.BLUE -> "синяя"
        Color.BLACK -> "черная"
        Color.WHITE -> "белая"
        Color.YELLOW -> "желтая"
        Color.ORANGE -> "оранжевая"
        Color.MAGENTO -> "фиолетовая"
    }
}