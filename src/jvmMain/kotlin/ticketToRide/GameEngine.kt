package ticketToRide

import kotlinx.coroutines.flow.*

fun runGame(firstPlayer: Player, requests: Flow<ApiRequest>) : Flow<GameState> {
    val initialState = GameState(listOf(firstPlayer), emptyList())
    return requests.scan(initialState) { game, req ->
        when (req) {
            is JoinGame -> game.joinPlayer(req.playerId, req.playerName, Color.random(game))
            is WentAway -> game.player(req.playerId) { copy(away = true) }
            is CameBack -> game.player(req.playerId) { copy(away = false) }
            else -> game
        }
    }
}

fun GameState.joinPlayer(id: PlayerId, name: PlayerName, color: Color) =
    GameState(players + Player(id, name, color), openCoaches)

fun GameState.player(playerId: PlayerId, block: Player.() -> Player) =
    GameState(players.map { if (it.id == playerId) it.block() else it }, openCoaches)
