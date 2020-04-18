package ticketToRide

import kotlinx.coroutines.flow.*

fun runGame(firstPlayerName: PlayerName, requests: Flow<Pair<Request, Connection>>): Flow<GameState> {
    val initialState = GameState.initial().joinPlayer(firstPlayerName)
    return requests.scan(initialState) { game, (req, conn) ->
        when (req) {
            is JoinGameRequest ->
                game.joinPlayer(conn.playerName)
            is ConfirmTicketsChoiceRequest ->
                game.updatePlayer(conn.playerName) { confirmTicketsChoice(req.ticketsToKeep) }
            is PickCardsRequest ->
                game.pickCards(conn.playerName, req)
            is PickTicketsRequest ->
                game.pickTickets(conn.playerName)
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

fun GameState.advanceTurn(): GameState {
    val nextTurn = (turn + 1) % players.size
    val ticketsChoice = players[nextTurn].ticketsForChoice
    val skipsMove = ticketsChoice != null && !ticketsChoice.shouldChooseOnNextTurn;
    val nextState = GameState(players, openCards, nextTurn).updatePlayer(nextTurn) {
        copy(ticketsForChoice = ticketsForChoice?.copy(shouldChooseOnNextTurn = true))
    }
    return if (skipsMove) nextState.advanceTurn() else nextState
}

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
        (if (req is PickCardsRequest.TwoCards) req.cards.toList().filterNotNull().toMutableList()
        else mutableListOf<Card>(Card.Loco))
    val cardsToOpen = openCardsToReplace.map { Card.random() }
    val state = GameState(
        players,
        openCards.filter {
            if (openCardsToReplace.contains(it)) { openCardsToReplace -= it; false } else true
        } + cardsToOpen,
        turn)
    return state.advanceTurn()
}

fun GameState.pickTickets(playerName: PlayerName): GameState {
    val inTurn = players[turn].name == playerName
    val state = updatePlayer(playerName, { ticketsForChoice == null }) {
        copy(ticketsForChoice = PendingTicketsChoice(getRandomTickets(3, false), 1, inTurn))
    }
    return if (inTurn) state.advanceTurn() else state
}

fun Player.confirmTicketsChoice(ticketsToKeep: List<Ticket>) =
    if (ticketsForChoice == null) this
    else copy(
        ticketsOnHand = ticketsOnHand + ticketsForChoice.tickets.intersect(ticketsToKeep),
        ticketsForChoice = null
    )