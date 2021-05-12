package ticketToRide

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Response {

    @Serializable
    @SerialName("state")
    class GameState(val state: GameStateView, val action: PlayerAction? = null) : Response()

    @Serializable
    @SerialName("end")
    class GameEnd(
        val players: List<Pair<PlayerView, List<Ticket>>> = emptyList(),
        val action: PlayerAction? = null
    ) : Response()

    @Serializable
    @SerialName("error")
    class ErrorMessage(val text: String) : Response()

    @Serializable
    @SerialName("message")
    class ChatMessage(val from: PlayerName, val message: String) : Response()
}

@Serializable
sealed class PlayerAction {

    @Serializable
    @SerialName("join")
    class JoinGame(val playerName: PlayerName) : PlayerAction()

    @Serializable
    @SerialName("leave")
    class LeaveGame(val playerName: PlayerName) : PlayerAction()

    @Serializable
    @SerialName("confirmTickets")
    class ConfirmTicketsChoice(val playerName: PlayerName, val ticketsToKeep: Int) : PlayerAction()

    @Serializable
    sealed class PickCards : PlayerAction() {

        @Serializable
        @SerialName("pickLoco")
        class Loco(val playerName: PlayerName) : PickCards()

        @Serializable
        @SerialName("pickTwoCards")
        class TwoCards(val playerName: PlayerName, val cards: Pair<PickedCard, PickedCard>) : PickCards()
    }

    @Serializable
    @SerialName("pickTickets")
    class PickTickets(val playerName: PlayerName) : PlayerAction()

    @Serializable
    @SerialName("build")
    class BuildSegment(
        val playerName: PlayerName,
        val segment: Segment,
        val cards: List<Card> = emptyList()
    ) : PlayerAction()

    @Serializable
    @SerialName("station")
    class BuildStation(val playerName: PlayerName, val target: CityId) : PlayerAction()
}

fun Request.toAction(playerName: PlayerName) = when (this) {
    is JoinPlayer -> PlayerAction.JoinGame(playerName)
    is LeaveGameRequest -> PlayerAction.LeaveGame(playerName)
    is ConfirmTicketsChoiceRequest -> PlayerAction.ConfirmTicketsChoice(playerName, ticketsToKeep.size)
    is PickCardsRequest.Loco -> PlayerAction.PickCards.Loco(playerName)
    is PickCardsRequest.TwoCards -> PlayerAction.PickCards.TwoCards(playerName, cards)
    is PickTicketsRequest -> PlayerAction.PickTickets(playerName)
    is BuildSegmentRequest -> PlayerAction.BuildSegment(playerName, segment, cards)
    is BuildStationRequest -> PlayerAction.BuildStation(playerName, target)
    is ChatMessage -> null
    is Callback -> null
}