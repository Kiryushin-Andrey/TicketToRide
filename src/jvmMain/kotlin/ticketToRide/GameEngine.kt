package ticketToRide

import kotlinx.coroutines.flow.*

fun runGame(firstPlayerName: PlayerName, requests: Flow<Request>): Flow<GameState> {
    val initialState = GameState.initial().joinPlayer(firstPlayerName)
    return requests.scan(initialState) { game, req ->
        when (req) {
            is JoinGameRequest -> game.joinPlayer(req.playerName)
            is WentAwayRequest -> game.updatePlayer(req.playerName) { copy(away = true) }
            is CameBackRequest -> game.updatePlayer(req.playerName) { copy(away = false) }
            else -> game
        }
    }
}

fun GameState.joinPlayer(name: PlayerName): GameState {
    val color = Color.values()
        .filter { color -> color != Color.NONE && !players.map { it.color }.contains(color) }
        .random()
    val cards = (1..5).map { Card.random() }.groupingBy { it }.eachCount()
    val tickets = PendingTicketsChoice(
        getRandomTickets(1, true) + getRandomTickets(3, false),
        true
    )
    val newPlayer = Player(name, color, CarsCountPerPlayer, cards, tickets)
    return GameState(players + newPlayer, openCards, turn)
}

fun GameState.updatePlayer(name: PlayerName, block: Player.() -> Player) =
    GameState(players.map { if (it.name == name) it.block() else it }, openCards, turn)
