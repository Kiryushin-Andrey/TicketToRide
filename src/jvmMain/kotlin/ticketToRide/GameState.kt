package ticketToRide

const val CarsCountPerPlayer = 5

data class Player(
    val name: PlayerName,
    val color: Color,
    val carsLeft: Int,
    val cards: List<Card>,
    val occupiedSegments: List<Segment>,
    val ticketsForChoice: PendingTicketsChoice?,
    val ticketsOnHand: List<Ticket> = emptyList()
) {
    fun toPlayerView() =
        PlayerView(name, color, carsLeft, cards.size, ticketsOnHand.size, occupiedSegments, ticketsForChoice.toState())
}

data class GameState(
    val players: List<Player>,
    val openCards: List<Card>,
    val turn: Int,
    val endsOnPlayer: Int?
) {
    companion object {
        fun initial() = GameState(emptyList(), (1..5).map { Card.random() }, 0, null)
    }

    fun getRandomTickets(count: Int, long: Boolean): List<Ticket> {
        val available = (if (long) GameMap.longTickets else GameMap.shortTickets)
            .filter { ticket ->
                !players
                    .flatMap { p -> p.ticketsOnHand + (p.ticketsForChoice?.tickets ?: emptyList()) }
                    .any { it == ticket || (long && it.points >= GameMap.longTicketMinPoints && it.sharesCityWith(ticket)) }
            }
            .distinct()
        return (1..count).map { available.random() }
    }

    fun toPlayerView(myName: PlayerName): GameStateView {
        val me = players.single { it.name == myName }
        return GameStateView(
            players.map { it.toPlayerView() },
            openCards,
            turn,
            endsOnPlayer != null,
            myName,
            me.cards,
            me.ticketsOnHand,
            me.ticketsForChoice
        )
    }

    fun updatePlayer(name: PlayerName, predicate: Player.() -> Boolean = { true }, block: Player.() -> Player) =
        copy(players = players.map { if (it.name == name && it.predicate()) it.block() else it })

    fun updatePlayer(ix: Int, predicate: Player.() -> Boolean = { true }, block: Player.() -> Player) =
        copy(players = players.mapIndexed { i, player -> if (i == ix && player.predicate()) player.block() else player })
}