package ticketToRide

import kotlinx.serialization.Serializable

@Serializable
class GameStateForObserver(
    val gameId: GameId,
    val players: List<PlayerView> = emptyList(),
    val tickets: List<List<Ticket>> = emptyList(),
    val openCards: List<Card> = emptyList(),
    val turn: Int,
    val lastRound: Boolean,
    val gameEnded: Boolean,
    val action: PlayerAction? = null)