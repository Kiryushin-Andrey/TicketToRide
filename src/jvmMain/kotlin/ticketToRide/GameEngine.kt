package ticketToRide

import kotlinx.coroutines.flow.*

fun runGame(firstPlayerName: PlayerName, requests: Flow<Pair<Request, Connection>>): Flow<GameState> {
    val initialState = GameState.initial().joinPlayer(firstPlayerName)
    return requests.scan(initialState) { game, (req, conn) ->
        when (req) {
            is JoinGameRequest -> game.joinPlayer(conn.playerName)
            is ConfirmTicketsChoiceRequest -> game.updatePlayer(conn.playerName) { confirmTicketsChoice(req.ticketsToKeep) }
            is PickCardsRequest -> game.pickCards(conn.playerName, req)
            is PickTicketsRequest -> game.updatePlayer(conn.playerName) { pickTickets(game.getRandomTickets(3, false)) }
            else -> game
        }
    }
}

fun GameState.joinPlayer(name: PlayerName): GameState {
    val color = Color.values()
        .filter { color -> !players.map { it.color }.contains(color) }
        .random()
    val cards = (1..4).map { Card.random() }
    val tickets = getRandomTickets(1, true) + getRandomTickets(3, false)
    val newPlayer = Player(
        name, color, CarsCountPerPlayer, cards,
        PendingTicketsChoice(tickets, 2, true)
    )
    return GameState(players + newPlayer, openCards, turn)
}

fun GameState.updatePlayer(name: PlayerName, block: Player.() -> Player) =
    GameState(players.map { if (it.name == name) it.block() else it }, openCards, turn)

fun GameState.pickCards(name: PlayerName, req: PickCardsRequest): GameState {
    val cardsToPick = when (req) {
        is PickCardsRequest.Loco -> listOf(Card.Loco)
        is PickCardsRequest.TwoCards -> req.cards.toList().map { it ?: Card.randomNoLoco() }
    }
    val players = players.map { player ->
        if (player.name == name) player.copy(cards = player.cards + cardsToPick)
        else player
    }

    val openCardsToReplace =
        (if (req is PickCardsRequest.TwoCards) req.cards.toList().filterNotNull() else emptyList()).toMutableList()
    val cardsToOpen = openCardsToReplace.map { Card.random() }
    return GameState(
        players,
        openCards.filter {
            if (openCardsToReplace.contains(it)) { openCardsToReplace -= it; false } else true
        } + cardsToOpen,
        turn)
}

fun Player.pickTickets(tickets: List<Ticket>) =
    if (ticketsForChoice != null) this
    else copy(ticketsForChoice = PendingTicketsChoice(tickets, 1, false))

fun Player.confirmTicketsChoice(ticketsToKeep: List<Ticket>) =
    if (ticketsForChoice == null) this
    else copy(
        ticketsOnHand = ticketsOnHand + ticketsForChoice.tickets.intersect(ticketsToKeep),
        ticketsForChoice = null
    )