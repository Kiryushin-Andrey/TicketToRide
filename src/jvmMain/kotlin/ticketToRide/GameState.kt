package ticketToRide

import kotlinx.serialization.Serializable

const val OpenCardsCount = 5

@Serializable
data class Player(
    override val name: PlayerName,
    override val color: PlayerColor,
    val points: Int? = null,
    val carsLeft: Int,
    val stationsLeft: Int,
    val cards: List<Card> = emptyList(),
    val occupiedSegments: List<Segment> = emptyList(),
    val placedStations: List<CityName> = emptyList(),
    val ticketsForChoice: PendingTicketsChoice? = null,
    val ticketsOnHand: List<Ticket> = emptyList(),
    val away: Boolean = false
) : PlayerId {
    fun toPlayerView() =
        PlayerView(
            name,
            color,
            points,
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
    val players: List<Player> = emptyList(),
    val openCards: List<Card> = emptyList(),
    val turn: Int,
    val endsOnPlayer: Int? = null,
    val initialCarsCount: Int,
    val calculateScoresInProcess: Boolean
) {
    companion object {
        fun initial(id: GameId, initialCarsCount: Int, calculateScoresInProcess: Boolean, map: GameMap) =
            GameState(
                id,
                emptyList(),
                (1..OpenCardsCount).map { Card.random(map) },
                0,
                null,
                initialCarsCount,
                calculateScoresInProcess
            )
    }

    fun getRandomTickets(map: GameMap, count: Int, long: Boolean): List<Ticket> {
        val available = (if (long) map.longTickets else map.shortTickets)
            .filter { ticket ->
                !players
                    .flatMap { p -> p.ticketsOnHand + (p.ticketsForChoice?.tickets ?: emptyList()) }
                    .any { it == ticket || (long && it.points >= map.longTicketMinPoints && it.sharesCityWith(ticket)) }
            }
            .distinct()
        if (available.size < count)
            throw InvalidActionError("Game full, no more players allowed (no tickets left)")

        return (1..count).map { available.random() }.distinct()
            .let { if (it.size == count) it else getRandomTickets(map, count, long) }
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

    fun forObservers(action: PlayerAction?) = GameStateForObservers(
        id,
        players.map { it.toPlayerView() },
        openCards,
        turn,
        endsOnPlayer != null,
        endsOnPlayer == turn,
        action
    )

    fun updatePlayer(name: PlayerName, block: Player.() -> Player) =
        copy(players = players.map { if (it.name == name) it.block() else it })

    fun updatePlayer(ix: Int, block: Player.() -> Player) =
        copy(players = players.mapIndexed { i, player -> if (i == ix) player.block() else player })

    fun restored() =
        copy(players = players.map { it.copy(away = true) })
}