package ticketToRide

import kotlinx.serialization.Serializable

const val InitialStationsCount = 3
const val OpenCardsCount = 5

@Serializable
data class Player(
    val name: PlayerName,
    val color: PlayerColor,
    val carsLeft: Int,
    val stationsLeft: Int,
    val cards: List<Card>,
    val occupiedSegments: List<Segment>,
    val placedStations: List<CityName>,
    val ticketsForChoice: PendingTicketsChoice?,
    val ticketsOnHand: List<Ticket> = emptyList(),
    val away: Boolean = false
) {
    fun toPlayerView() =
        PlayerView(
            name,
            color,
            carsLeft,
            stationsLeft,
            cards.size,
            ticketsOnHand.size,
            away,
            occupiedSegments,
            placedStations,
            ticketsForChoice.toState()
        )
}

@Serializable
data class GameState(
    val id: GameId,
    val players: List<Player>,
    val openCards: List<Card>,
    val turn: Int,
    val endsOnPlayer: Int?,
    val initialCarsCount: Int
) {
    companion object {
        fun initial(id: GameId, initialCarsCount: Int) =
            GameState(id, emptyList(), (1..OpenCardsCount).map { Card.random() }, 0, null, initialCarsCount)
    }

    fun getRandomTickets(count: Int, long: Boolean): List<Ticket> {
        val available = (if (long) GameMap.longTickets else GameMap.shortTickets)
            .filter { ticket ->
                !players
                    .flatMap { p -> p.ticketsOnHand + (p.ticketsForChoice?.tickets ?: emptyList()) }
                    .any { it == ticket || (long && it.points >= GameMap.longTicketMinPoints && it.sharesCityWith(ticket)) }
            }
            .distinct()
        return if (available.size >= count) (1..count).map { available.random() }
        else throw InvalidActionError("Game full, no more players allowed (no tickets left)")
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

    fun updatePlayer(name: PlayerName, block: Player.() -> Player) =
        copy(players = players.map { if (it.name == name) it.block() else it })

    fun updatePlayer(ix: Int, block: Player.() -> Player) =
        copy(players = players.mapIndexed { i, player -> if (i == ix) player.block() else player })
}