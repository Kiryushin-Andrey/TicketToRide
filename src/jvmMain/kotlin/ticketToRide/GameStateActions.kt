package ticketToRide

fun GameState.processRequest(
    req: Request,
    map: GameMap,
    fromPlayerName: PlayerName,
    isAway: (PlayerName) -> Boolean
) = when (req) {

    is JoinPlayer ->
        joinPlayer(fromPlayerName, req.color, map)

    LeaveGameRequest ->
        advanceTurnFrom(fromPlayerName, map, isAway)

    is ConfirmTicketsChoiceRequest ->
        updatePlayer(fromPlayerName) { confirmTicketsChoice(req.ticketsToKeep) }.recalculatePlayerScores(map)

    PickTicketsRequest ->
        pickTickets(fromPlayerName, map).advanceTurnFrom(fromPlayerName, map, isAway)

    is PickCardsRequest ->
        inTurnOnly(fromPlayerName) {
            pickCards(fromPlayerName, req, map).advanceTurn(map, isAway)
        }

    is BuildSegmentRequest ->
        inTurnOnly(fromPlayerName) {
            if (!map.segmentExists(req.segment))
                throw InvalidActionError("There is no segment ${req.segment.from.value} - ${req.segment.to.value} of ${req.segment.color} on the map")
            buildSegment(fromPlayerName, req.segment, req.cards).recalculatePlayerScores(map).advanceTurn(map, isAway)
        }

    is BuildStationRequest ->
        inTurnOnly(fromPlayerName) {
            buildStation(fromPlayerName, req.target, req.cards).recalculatePlayerScores(map).advanceTurn(map, isAway)
        }

    is ChatMessage -> this
    is Callback -> this
}

fun GameState.joinPlayer(name: PlayerName, color: PlayerColor, map: GameMap): GameState {
    if (players.any { it.name == name }) throw InvalidActionError("Name is taken")

    if (players.map { it.color }.contains(color)) throw InvalidActionError("Color is taken")

    val availableColors = PlayerColor.values().filter { c -> !players.map { it.color }.contains(c) }
    if (availableColors.isEmpty()) throw InvalidActionError("Game full, no more players allowed")

    val cards = (1..4).map { Card.random(map) }
    val tickets = getRandomTickets(map, 1, true) + getRandomTickets(map, 3, false)
    val newPlayer = Player(
        name,
        color,
        if (calculateScoresInProcess) 0 else null,
        initialCarsCount,
        InitialStationsCount,
        cards,
        emptyList(),
        emptyList(),
        PendingTicketsChoice(tickets, 2, true)
    )
    return copy(players = players + newPlayer)
}

private fun GameState.inTurnOnly(name: PlayerName, block: GameState.() -> GameState) =
    if (players[turn].name == name) block() else throw InvalidActionError("Not your turn")

fun GameState.advanceTurn(map: GameMap, isAway: (PlayerName) -> Boolean) =
    advanceTurnFrom(players[turn].name, map, isAway)

fun GameState.advanceTurnFrom(name: PlayerName, map: GameMap, isAway: (PlayerName) -> Boolean): GameState {
    if (players[turn].name != name) {
        return this
    }

    if (players.flatMap { it.occupiedSegments }.sumOf { it.length } == map.totalSegmentsLength) {
        return copy(endsOnPlayer = turn)
    }

    val gameEndsOnPlayer = endsOnPlayer ?: if (players[turn].carsLeft < 3) turn else null
    val nextTurn = generateSequence(turn) { prev -> (prev + 1) % players.size }
        .drop(1)
        .dropWhile { isAway(players[it].name) && it != turn }
        .first()
    val skipsMove = with(players[nextTurn]) { ticketsForChoice?.shouldChooseOnNextTurn == false }
    val nextState = copy(turn = nextTurn, endsOnPlayer = gameEndsOnPlayer)
        .updatePlayer(nextTurn) {
            copy(ticketsForChoice = ticketsForChoice?.copy(shouldChooseOnNextTurn = true))
        }
    return if (skipsMove) nextState.advanceTurnFrom(players[nextTurn].name, map, isAway) else nextState
}

private fun GameState.pickCards(name: PlayerName, req: PickCardsRequest, map: GameMap): GameState {
    val indicesToReplace = req.getIndicesToReplace()
    val newOpenCards = openCards.mapIndexed { ix, card ->
        if (indicesToReplace.contains(ix)) Card.random(map) else card
    }.let {
        if (it.count { it is Card.Loco } >= 3) (1..OpenCardsCount).map { Card.random(map) }
        else it
    }

    return updatePlayer(name) { copy(cards = cards + req.getCardsToPick(map)) }
        .copy(openCards = newOpenCards)
}

private fun GameState.pickTickets(playerName: PlayerName, map: GameMap): GameState {
    val inTurn = players[turn].name == playerName
    return updatePlayer(playerName) {
        if (ticketsForChoice == null)
            copy(ticketsForChoice = PendingTicketsChoice(getRandomTickets(map, 3, false), 1, inTurn))
        else
            throw InvalidActionError("Decide on your tickets first")
    }
}

private fun GameState.buildSegment(
    name: PlayerName,
    segment: Segment,
    cards: List<Card>
): GameState {
    players.find { it.occupiedSegments.contains(segment) }?.let {
        throw InvalidActionError("Segment ${segment.from.value} - ${segment.to.value} is already occupied by ${it.name.value}")
    }
    return updatePlayer(name) { occupySegment(segment, cards) }
}

private fun GameState.buildStation(name: PlayerName, target: CityName, cards: List<Card>): GameState {
    players.find { it.placedStations.contains(target) }?.let {
        throw InvalidActionError("There is already a station in ${target.value} owned by ${it.name.value}")
    }

    return updatePlayer(name) { buildStation(target, cards) }
}

private fun GameState.recalculatePlayerScores(map: GameMap): GameState {
    val scores = players.map {
        val segmentsOfOtherPlayers = players.filter { p -> p != it }.flatMap { it.occupiedSegments }
        PlayerScore(
            it.name,
            it.color,
            it.occupiedSegments,
            it.placedStations,
            it.ticketsOnHand,
            segmentsOfOtherPlayers,
            map
        )
    }
    val longestRoute = scores.map { it.longestRoute }.maxOrNull()!!
    return copy(players = players.zip(scores) { player, score ->
        player.copy(points = score.getTotalPoints(longestRoute, true))
    })
}

private fun Player.confirmTicketsChoice(ticketsToKeep: List<Ticket>) = when {
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

private fun Player.occupySegment(segment: Segment, cardsToDrop: List<Card>) = when {
    segment.length > carsLeft ->
        throw InvalidActionError("Not enough wagons ($carsLeft) to build ${segment.from.value} - ${segment.to.value} segment")

    !cards.contains(cardsToDrop) ->
        throw InvalidActionError("Cards to drop do not match cards on hand")

    !segment.canBuildWith(cardsToDrop) -> {
        val cardsDesc = cards.groupingBy { it }.eachCount().entries.joinToString { "${it.key} - ${it.value}" }
        throw InvalidActionError("You cannot build ${segment.from.value} - ${segment.to.value} segment with the cards $cardsDesc")
    }

    else -> copy(
        cards = cards.drop(cardsToDrop),
        carsLeft = carsLeft - cardsToDrop.size,
        occupiedSegments = occupiedSegments + segment
    )
}

private fun Player.buildStation(target: CityName, cardsToDrop: List<Card>) = when {
    cardsToDrop.filterIsInstance<Card.Car>().distinct().size > 1 ->
        throw InvalidActionError("Only cards of the same color (or locos) are allowed to be dropped for building a station")

    stationsLeft == 0 ->
        throw InvalidActionError("No stations left on hand")

    !cards.contains(cardsToDrop) ->
        throw InvalidActionError("Cards to drop do not match cards on hand")

    cardsToDrop.size != placedStations.size + 1 ->
        throw InvalidActionError("Should drop 1 card for 1st station, 2 cards for 2nd station and 3 cards for 3rd station")

    else -> copy(
        cards = cards.drop(cardsToDrop),
        stationsLeft = stationsLeft - 1,
        placedStations = placedStations + target
    )
}

private fun List<Card>.drop(cardsToDrop: List<Card>) = cardsToDrop.toMutableList().let { list ->
    filter {
        if (list.contains(it)) {
            list -= it; false
        } else true
    }
}

private fun List<Card>.contains(other: List<Card>) = other.groupingBy { it }.eachCount().let { another ->
    groupingBy { it }.eachCount().all { (card, count) -> count >= (another[card] ?: 0) }
}

private fun Segment.canBuildWith(cardsToDrop: List<Card>): Boolean {
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
            length == cardsCount.entries.sumOf { (card, count) ->
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