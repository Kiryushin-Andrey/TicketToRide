package ticketToRide

import ticketToRide.playerState.PlayerState

sealed class Screen {

    data class Welcome(val joinGameFailure: JoinGameFailure? = null) : Screen()

    data class ShowGameId(val gameId: GameId, val gameState: GameStateView) : Screen()

    data class GameInProgress(
        val gameId: GameId,
        val gameMap: GameMap,
        val gameState: GameStateView,
        val playerState: PlayerState
    ) : Screen()

    data class GameOver(
        val gameId: GameId,
        val gameMap: GameMap,
        val players: List<Pair<PlayerView, List<Ticket>>>,
        val me: PlayerView
    ) : Screen()
}