package ticketToRide

import ticketToRide.playerState.PlayerState

sealed class Screen {

    object Welcome : Screen()

    data class ShowGameId(override val gameId: GameId, val gameState: GameStateView) : Screen(), InGame {
        override val me: PlayerView get() = gameState.me
    }

    data class GameInProgress(
        override val gameId: GameId,
        val gameState: GameStateView,
        val playerState: PlayerState
    ) : Screen(), InGame {
        override val me: PlayerView get() = gameState.me
    }

    data class GameOver(
        override val gameId: GameId,
        override val me: PlayerView,
        val gameMap: GameMap,
        val players: List<Pair<PlayerView, List<Ticket>>>
    ) : Screen(), InGame

    interface InGame {
        val gameId: GameId
        val me: PlayerView
    }
}