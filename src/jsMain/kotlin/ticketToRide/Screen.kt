package ticketToRide

import kotlinx.coroutines.channels.SendChannel
import ticketToRide.playerState.PlayerState

sealed class Screen {

    object Welcome : Screen()

    class ShowGameId(
        override val gameId: GameId,
        override val requests: SendChannel<Request>,
        val gameState: GameStateView
    ) : Screen(), InGame {

        override val me: PlayerView get() = gameState.me

        fun withGameState(newState: GameStateView) = ShowGameId(gameId, requests, newState)

        override fun onReconnect(requests: SendChannel<Request>) = ShowGameId(gameId, requests, gameState)
    }

    class GameInProgress(
        override val gameId: GameId,
        override val requests: SendChannel<Request>,
        val gameState: GameStateView,
        val playerState: PlayerState
    ) : Screen(), InGame {

        override val me: PlayerView get() = gameState.me

        fun copy(gameState: GameStateView? = null, playerState: PlayerState? = null) =
            GameInProgress(gameId, requests, gameState ?: this.gameState, playerState ?: this.playerState)

        override fun onReconnect(requests: SendChannel<Request>) = GameInProgress(gameId, requests, gameState, playerState)
    }

    class GameOver(
        override val gameId: GameId,
        override val me: PlayerView,
        override val requests: SendChannel<Request>,
        val gameMap: GameMap,
        val players: List<Pair<PlayerView, List<Ticket>>>
    ) : Screen(), InGame {

        override fun onReconnect(requests: SendChannel<Request>) = GameOver(gameId, me, requests, gameMap, players)
    }

    interface InGame {
        val gameId: GameId
        val me: PlayerView
        val requests: SendChannel<Request>
        fun onReconnect(requests: SendChannel<Request>): Screen
    }
}