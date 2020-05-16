package ticketToRide

import kotlinx.serialization.Serializable

@Serializable
sealed class Response {

    @Serializable
    class GameMap(val map: ticketToRide.GameMap) : Response()

    @Serializable
    class GameState(val gameId: GameId, val state: GameStateView, val action: PlayerAction?) : Response()

    @Serializable
    class GameEnd(val gameId: GameId, val players: List<Pair<PlayerView, List<Ticket>>>, val action: PlayerAction? = null) : Response()

    @Serializable
    class ErrorMessage(val text: String) : Response()

    @Serializable
    class ChatMessage(val from: PlayerName, val message: String) : Response()

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
        class BuildSegment(val playerName: PlayerName, val from: CityName, val to: CityName, val cards: List<Card>) : PlayerAction()

        @Serializable
        class BuildStation(val playerName: PlayerName, val target: CityName) : PlayerAction()
    }
}

fun GameRequest.toAction(playerName: PlayerName) = when (this) {
    is JoinGameRequest -> Response.PlayerAction.JoinGame(playerName)
    is LeaveGameRequest -> Response.PlayerAction.LeaveGame(playerName)
    is ConfirmTicketsChoiceRequest -> Response.PlayerAction.ConfirmTicketsChoice(playerName, ticketsToKeep.size)
    is PickCardsRequest.Loco -> Response.PlayerAction.PickCards.Loco(playerName)
    is PickCardsRequest.TwoCards -> Response.PlayerAction.PickCards.TwoCards(playerName, cards)
    is PickTicketsRequest -> Response.PlayerAction.PickTickets(playerName)
    is BuildSegmentRequest -> Response.PlayerAction.BuildSegment(playerName, from, to, cards)
    is BuildStationRequest -> Response.PlayerAction.BuildStation(playerName, target)
}