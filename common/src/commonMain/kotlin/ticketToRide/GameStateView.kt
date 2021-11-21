package ticketToRide

import kotlinx.serialization.Serializable

@Serializable
class GameStateView(
    val players: List<PlayerView> = emptyList(),
    val openCards: List<Card> = emptyList(),
    val turn: Int,
    val lastRound: Boolean,
    val myName: PlayerName,
    val myCards: List<Card> = emptyList(),
    val myTicketsOnHand: List<Ticket> = emptyList(),
    val myPendingTicketsChoice: PendingTicketsChoice? = null
) {
    val me get() = players.find { it.name == myName }!!
    val myTurn get() = players[turn].name == myName
}