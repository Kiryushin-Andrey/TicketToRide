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
            is BuildSegmentRequest ->
                game.buildSegment(conn.playerName, req.from, req.to, req.cards)
            else -> game
        }
    }
}

fun GameState.playerByName(name: PlayerName) = players.single { it.name == name }

fun GameState.joinPlayer(name: PlayerName): GameState {
    val color = Color.values()
        .filter { color -> !players.map { it.color }.contains(color) }
        .random()
    val cards = (1..4).map { Card.random() }
    val tickets = getRandomTickets(1, true) + getRandomTickets(3, false)
    val newPlayer = Player(
        name, color, CarsCountPerPlayer, cards, emptyList(),
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
    val openCardsToReplace =
        (if (req is PickCardsRequest.TwoCards) req.cards.toList().filterNotNull().toMutableList()
        else mutableListOf<Card>(Card.Loco))
    val cardsToOpen = openCardsToReplace.map { Card.random() }
    val newOpenCards = cardsToOpen + openCards
        .filter { if (openCardsToReplace.contains(it)) { openCardsToReplace -= it; false } else true }

    return updatePlayer(name) { copy(cards = cards + cardsToPick) }
        .copy(openCards = newOpenCards)
        .advanceTurn()
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

fun Player.occupySegment(segment: Segment, cardsToDrop: List<Card>): Player {
    val list = cardsToDrop.toMutableList()
    return copy(
        cards = cards.filter {
            if (list.contains(it)) { list -= it; false } else true
        },
        carsLeft = carsLeft - cardsToDrop.size,
        occupiedSegments = occupiedSegments + segment)
}

fun GameState.buildSegment(name: PlayerName, from: CityName, to: CityName, cards: List<Card>) =
    GameMap.getSegmentBetween(from, to)?.let {
        updatePlayer(name) { occupySegment(it, cards) }.advanceTurn()
    } ?: this