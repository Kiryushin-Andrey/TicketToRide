package ticketToRide

sealed class SendResponse {
    data class ForAll(val resp: (to: PlayerName) -> Response) : SendResponse()
    data class ForPlayer(val to: PlayerName, val resp: Response) : SendResponse()
}

fun Response.toAll() = SendResponse.ForAll { this }

fun GameState.processRequest(req: GameRequest, fromPlayerName: PlayerName): Pair<GameState, List<SendResponse>> {
    if (turn == endsOnPlayer)
        return this to listOf(Response.GameEnd(id, players.map { it.toPlayerView() to it.ticketsOnHand }).toAll())

    val newState = when (req) {
        is JoinGameRequest ->
            joinPlayer(fromPlayerName)
        is LeaveGameRequest ->
            leavePlayer(fromPlayerName)
        is ConfirmTicketsChoiceRequest ->
            updatePlayer(fromPlayerName) { confirmTicketsChoice(req.ticketsToKeep) }
        is PickTicketsRequest ->
            pickTickets(fromPlayerName)
        is PickCardsRequest ->
            inTurnOnly(fromPlayerName) { pickCards(fromPlayerName, req) }
        is BuildSegmentRequest ->
            inTurnOnly(fromPlayerName) { buildSegment(fromPlayerName, req.from, req.to, req.cards) }
    }
    val message = with(newState) {
        SendResponse.ForAll { toPlayerName ->
            if (turn != endsOnPlayer)
                Response.GameState(id, toPlayerView(toPlayerName), req.toAction(fromPlayerName))
            else
                Response.GameEnd(
                    id,
                    players.map { it.toPlayerView() to it.ticketsOnHand },
                    req.toAction(fromPlayerName)
                )
        }
    }
    return newState to listOf(message)
}

fun GameState.joinPlayer(name: PlayerName): GameState {
    if (players.any { it.name == name }) {
        return updatePlayer(name) { if (this.away) copy(away = false) else throw InvalidActionError("Name is taken") }
    }

    val availableColors = PlayerColor.values().filter { color -> !players.map { it.color }.contains(color) }
    if (availableColors.isEmpty()) throw InvalidActionError("Game full, no more players allowed")

    val color = availableColors.random()
    val cards = (1..4).map { Card.random() }
    val tickets = getRandomTickets(1, true) + getRandomTickets(3, false)
    val newPlayer = Player(
        name, color, CarsCountPerPlayer, cards, emptyList(),
        PendingTicketsChoice(tickets, 2, true)
    )
    return copy(players = players + newPlayer)
}

fun GameState.leavePlayer(name: PlayerName) =
    updatePlayer(name) { copy(away = true) }.let {
        if (players[turn].name == name) it.advanceTurn() else it
    }

fun GameState.inTurnOnly(name: PlayerName, block: GameState.() -> GameState) =
    if (players[turn].name == name) block() else throw InvalidActionError("Not your turn")

fun GameState.advanceTurn(): GameState {
    if (players.flatMap { it.occupiedSegments }.sumBy { it.length } == GameMap.totalSegmentsLength) {
        return copy(endsOnPlayer = turn)
    }

    val gameEndsOnPlayer = endsOnPlayer ?: if (players[turn].carsLeft < 3) turn else null
    val nextTurn = generateSequence(turn) { prev -> (prev + 1) % players.size }.drop(1)
        .dropWhile { players[it].away && it != turn }.first()
    val skipsMove = with(players[nextTurn]) { ticketsForChoice?.shouldChooseOnNextTurn == false }
    val nextState = copy(turn = nextTurn, endsOnPlayer = gameEndsOnPlayer)
        .updatePlayer(nextTurn) {
            copy(ticketsForChoice = ticketsForChoice?.copy(shouldChooseOnNextTurn = true))
        }
    return if (skipsMove) nextState.advanceTurn() else nextState
}

fun GameState.pickCards(name: PlayerName, req: PickCardsRequest): GameState {
    val indicesToReplace = req.getIndicesToReplace()
    val newOpenCards = openCards.mapIndexed { ix, card ->
        if (indicesToReplace.contains(ix)) Card.random() else card
    }.let {
        if (it.count { it is Card.Loco } >= 3) (1..OpenCardsCount).map { Card.random() }
        else it
    }

    return updatePlayer(name) { copy(cards = cards + req.getCardsToPick()) }
        .copy(openCards = newOpenCards)
        .advanceTurn()
}

fun GameState.pickTickets(playerName: PlayerName): GameState {
    val inTurn = players[turn].name == playerName
    val state = updatePlayer(playerName) {
        if (ticketsForChoice == null)
            copy(ticketsForChoice = PendingTicketsChoice(getRandomTickets(3, false), 1, inTurn))
        else
            throw InvalidActionError("Decide on your tickets first")
    }
    return if (inTurn) state.advanceTurn() else state
}

fun GameState.buildSegment(name: PlayerName, from: CityName, to: CityName, cards: List<Card>): GameState {
    val segment = GameMap.getSegmentBetween(from, to)
        ?: throw InvalidActionError("There is no segment from ${from.value} - ${to.value} on the map")
    return updatePlayer(name) { occupySegment(segment, cards) }.advanceTurn()
}

fun Player.confirmTicketsChoice(ticketsToKeep: List<Ticket>) = when {
    ticketsForChoice == null ->
        throw InvalidActionError("You do not have any pending tickets choice")
    ticketsToKeep.any { !ticketsForChoice.tickets.contains(it) } ->
        throw InvalidActionError("Invalid ticket chosen")
    ticketsToKeep.size < ticketsForChoice.minCountToKeep ->
        throw InvalidActionError("You should keep at least ${ticketsForChoice.minCountToKeep} tickets")
    else ->
        copy(
            ticketsOnHand = ticketsOnHand + ticketsToKeep,
            ticketsForChoice = null
        )
}

fun Player.occupySegment(segment: Segment, cardsToDrop: List<Card>) = when {
    segment.length > carsLeft ->
        throw InvalidActionError("Not enough wagons ($carsLeft) to build ${segment.from.value} - ${segment.to.value} segment")

    !segment.canBuildWith(cardsToDrop) -> {
        val cardsDesc = cards.groupingBy { it }.eachCount().entries.joinToString { "${it.key} - ${it.value}" }
        throw InvalidActionError("You cannot build ${segment.from.value} - ${segment.to.value} segment with the cards $cardsDesc")
    }

    else -> {
        val list = cardsToDrop.toMutableList()
        copy(
            cards = cards.filter {
                if (list.contains(it)) {
                    list -= it; false
                } else true
            },
            carsLeft = carsLeft - cardsToDrop.size,
            occupiedSegments = occupiedSegments + segment
        )
    }
}

fun Segment.canBuildWith(cardsToDrop: List<Card>): Boolean {
    if (cardsToDrop.size != length) {
        return false
    }

    val cardsCount = cardsToDrop.groupingBy { it }.eachCount()
    return when (cardsCount.size) {
        1 -> {
            when (val card = cardsCount.keys.single()) {
                is Card.Loco -> true
                is Card.Car -> color == null || color == card.color
            }
        }
        2 -> {
            length == cardsCount.entries.sumBy { (card, count) ->
                when {
                    card is Card.Loco -> count
                    card is Card.Car && (color == null || color == card.color) -> count
                    else -> 0
                }
            }
        }
        else -> false
    }
}