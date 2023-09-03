package ticketToRide

sealed class Screen {

    data object Welcome : Screen()

    class ShowGameId(
        override val gameId: GameId,
        val gameState: GameStateView
    ) : Screen(), InGame {

        override val me: PlayerView get() = gameState.me

        fun withGameState(newState: GameStateView) = ShowGameId(gameId, newState)
    }

    class GameInProgress(
        override val gameId: GameId,
        val gameState: GameStateView,
        val playerState: PlayerState
    ) : Screen(), InGame {

        override val me: PlayerView get() = gameState.me

        fun copy(gameState: GameStateView? = null, playerState: PlayerState? = null) =
            GameInProgress(gameId, gameState ?: this.gameState, playerState ?: this.playerState)
    }

    class ObserveGameInProgress(val gameState: GameStateForObserver) : Screen()

    class GameOver(
        override val gameId: GameId,
        val observing: Boolean,
        val players: List<Pair<PlayerView, List<Ticket>>>
    ) : Screen(), InGame

    interface InGame {
        val gameId: GameId
        val me: PlayerView? get() = null
    }
}