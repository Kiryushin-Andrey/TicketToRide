package ticketToRide

const val CarsCountPerPlayer = 45

data class Player(
    val name: PlayerName,
    val color: Color,
    val carsLeft: Int,
    val cards: List<Card>,
    val ticketsForChoice: PendingTicketsChoice?,
    val ticketsOnHand: List<Ticket> = emptyList(),
    val away: Boolean = false
) {
    fun toPlayerView() =
        PlayerView(name, color, carsLeft, cards.size, ticketsOnHand.size, ticketsForChoice.toState(), away)
}

data class GameState(
    val players: List<Player>,
    val openCards: List<Card>,
    val spannedSections: List<SpannedSection>,
    val turn: Int
) {
    companion object {
        fun initial() = GameState(
            emptyList(),
            (1..5).map { Card.random() },
            emptyList(),
            0
        )
    }

    fun getRandomTickets(count: Int, long: Boolean): List<Ticket> {
        val available = (if (long) GameMap.longTickets else GameMap.shortTickets)
            .filter {
                !players
                    .flatMap { p -> p.ticketsOnHand + (p.ticketsForChoice?.tickets ?: emptyList()) }
                    .contains(it)
            }
            .distinct()
        return (1..count).map { available.random() }
    }

    fun toPlayerView(myName: PlayerName): GameStateView {
        val me = players.single { it.name == myName }
        return GameStateView(
            players.map { it.toPlayerView() },
            openCards,
            spannedSections,
            turn,
            myName,
            me.cards,
            me.ticketsOnHand,
            me.ticketsForChoice
        )
    }

    fun updatePlayer(name: PlayerName, predicate: Player.() -> Boolean = { true }, block: Player.() -> Player) =
        GameState(players.map { if (it.name == name && it.predicate()) it.block() else it }, openCards, spannedSections, turn)

    fun updatePlayer(ix: Int, predicate: Player.() -> Boolean = { true }, block: Player.() -> Player) =
        GameState(players.mapIndexed { i, player -> if (i == ix && player.predicate()) player.block() else player }, openCards, spannedSections, turn)
}