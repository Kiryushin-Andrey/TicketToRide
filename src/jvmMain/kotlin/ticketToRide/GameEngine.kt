package ticketToRide

import kotlinx.coroutines.flow.*

fun runEngine(requests: Flow<ApiRequest>) =
    requests.scan<ApiRequest, Game>(Welcome) { game, req ->
        when (game) {
            is Welcome ->
                when (req) {
                    is StartGame -> game.start(req.playerId, req.playerName, req.color, req.targetPlayersCount)
                    else -> game
                }
            is StartingGame ->
                when (req) {
                    is JoinGame -> game.joinPlayer(req.playerId, req.playerName, req.color).startIfDone()
                    is StartGameNow -> game.startNow()
                    else -> game
                }
            is GameInProgress ->
                when (req) {
                    is WentAway -> game.player(req.playerId) { copy(away = true) }
                    is CameBack -> game.player(req.playerId) { copy(away = false) }
                    else -> game
                }
        }
    }

fun Welcome.start(id: PlayerId, name: PlayerName, color: Color, targetPlayersCount: Int) =
    StartingGame(emptyList(), targetPlayersCount).joinPlayer(id, name, color)

fun StartingGame.joinPlayer(id: PlayerId, name: PlayerName, color: Color) =
    StartingGame(players + WannaBePlayer(id, name, color), targetCount)

fun StartingGame.startNow() =
    GameInProgress(players.map { Player(it.id, it.name, it.color) }, emptyList())

fun StartingGame.startIfDone() =
    if (players.size == targetCount) startNow() else this

fun GameInProgress.player(playerId: PlayerId, block: Player.() -> Player) =
    GameInProgress(players.map { if (it.id == playerId) it.block() else it }, openCoaches)
