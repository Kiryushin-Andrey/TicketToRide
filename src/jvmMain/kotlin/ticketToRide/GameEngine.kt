package ticketToRide

import kotlinx.coroutines.flow.*

fun runGame(firstPlayer: Player, requests: Flow<Request>) : Flow<GameState> {
    val initialState = GameState(listOf(firstPlayer), emptyList())
    return requests.scan(initialState) { game, req ->
        when (req) {
            is JoinGameRequest -> game.joinPlayer(req.playerName, Color.randomForPlayer(game))
            is WentAwayRequest -> game.player(req.playerName) { copy(away = true) }
            is CameBackRequest -> game.player(req.playerName) { copy(away = false) }
            else -> game
        }
    }
}

fun GameState.joinPlayer(name: PlayerName, color: Color) =
    GameState(players + Player(name, color), openCoaches)

fun GameState.player(name: PlayerName, block: Player.() -> Player) =
    GameState(players.map { if (it.name == name) it.block() else it }, openCoaches)
