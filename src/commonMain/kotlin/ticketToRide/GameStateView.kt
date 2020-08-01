package ticketToRide

import kotlinx.serialization.Serializable

@Serializable
class GameStateView(
    val players: List<PlayerView>,
    val openCards: List<Card>,
    val turn: Int,
    val lastRound: Boolean,
    val myName: PlayerName,
    val myCards: List<Card>,
    val myTicketsOnHand: List<Ticket>,
    val myPendingTicketsChoice: PendingTicketsChoice? = null
) {
    val me get() = players.find { it.name == myName }!!
    val myTurn get() = players[turn].name == myName
}