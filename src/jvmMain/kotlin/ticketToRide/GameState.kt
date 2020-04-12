package ticketToRide

const val CarsCountPerPlayer = 45

data class Player(
    val name: PlayerName,
    val color: Color,
    val carsLeft: Int,
    val cards: Map<Card, Int>,
    val ticketsForChoice: PendingTicketsChoice?,
    val ticketsOnHand: List<Ticket> = emptyList(),
    val away: Boolean = false
) {
    fun toPlayerView() =
        PlayerView(name, color, carsLeft, cards.values.sum(), ticketsOnHand.size, ticketsForChoice.toState(), away)
}

data class GameState(
    val players: List<Player>,
    val openCards: List<Card>,
    val turn: Int
) {
    companion object {
        fun initial() = GameState(
            emptyList(),
            (1..5).map { Card.random() },
            0
        )
    }

    fun getRandomTickets(count: Int, long: Boolean): List<Ticket> {
        val available = (if (long) GameMap.longTickets else GameMap.shortTickets)
            .filter { !players.flatMap { p -> p.ticketsOnHand }.contains(it) }
        return (1..count).map { available.random() }
    }

    fun toPlayerView(myName: PlayerName): GameStateView {
        val me = players.single { it.name == myName }
        return GameStateView(
            players.map { it.toPlayerView() },
            openCards,
            turn,
            me.cards,
            me.ticketsOnHand,
            me.ticketsForChoice
        )
    }
}