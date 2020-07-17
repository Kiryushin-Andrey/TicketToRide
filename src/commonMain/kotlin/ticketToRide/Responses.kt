package ticketToRide

import kotlinx.serialization.Serializable

@Serializable
class GameStateForObservers(
    val gameId: GameId,
    val players: List<PlayerView>,
    val openCards: List<Card>,
    val turn: Int,
    val lastRound: Boolean,
    val gameEnded: Boolean,
    val action: PlayerAction?
)

@Serializable
sealed class Response {

    @Serializable
    class GameStateWithMap(val gameId: GameId, val state: GameStateView, val map: GameMap) : Response()

    @Serializable
    class GameState(val state: GameStateView, val action: PlayerAction?) : Response()

    @Serializable
    class GameEnd(
        val players: List<Pair<PlayerView, List<Ticket>>>,
        val action: PlayerAction? = null
    ) : Response()

    @Serializable
    class ErrorMessage(val text: String) : Response()

    @Serializable
    class ChatMessage(val from: PlayerName, val message: String) : Response()
}

@Serializable
sealed class PlayerAction {

    @Serializable
    class JoinGame(val playerName: PlayerName) : PlayerAction()

    @Serializable
    class LeaveGame(val playerName: PlayerName) : PlayerAction()

    @Serializable
    class ConfirmTicketsChoice(val playerName: PlayerName, val ticketsToKeep: Int) : PlayerAction()

    @Serializable
    sealed class PickCards : PlayerAction() {

        @Serializable
        class Loco(val playerName: PlayerName) : PickCards()

        @Serializable
        class TwoCards(val playerName: PlayerName, val cards: Pair<PickedCard, PickedCard>) : PickCards()
    }

    @Serializable
    class PickTickets(val playerName: PlayerName) : PlayerAction()

    @Serializable
    class BuildSegment(val playerName: PlayerName, val from: CityName, val to: CityName, val cards: List<Card>) :
        PlayerAction()

    @Serializable
    class BuildStation(val playerName: PlayerName, val target: CityName) : PlayerAction()
}

fun GameRequest.toAction(playerName: PlayerName) = when (this) {
    is LeaveGameRequest -> PlayerAction.LeaveGame(playerName)
    is ConfirmTicketsChoiceRequest -> PlayerAction.ConfirmTicketsChoice(playerName, ticketsToKeep.size)
    is PickCardsRequest.Loco -> PlayerAction.PickCards.Loco(playerName)
    is PickCardsRequest.TwoCards -> PlayerAction.PickCards.TwoCards(playerName, cards)
    is PickTicketsRequest -> PlayerAction.PickTickets(playerName)
    is BuildSegmentRequest -> PlayerAction.BuildSegment(playerName, from, to, cards)
    is BuildStationRequest -> PlayerAction.BuildStation(playerName, target)
}