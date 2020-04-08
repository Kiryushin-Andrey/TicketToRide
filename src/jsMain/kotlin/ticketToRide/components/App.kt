package ticketToRide.components

import react.*
import ticketToRide.*

external interface AppState : RState {
    var joinedGame: Boolean
    var gameState: GameState
}

class App() : RComponent<RProps, AppState>() {
    override fun AppState.init() {
        joinedGame = false
    }

    override fun RBuilder.render() {
        if (state.joinedGame) {
            child(GameScreen::class) {
                attrs {
                    gameState = state.gameState
                }
            }
        } else {
            child(WelcomeScreen::class) {
                attrs {
                    onStartGame = ::startGame
                    onJoinGame = ::joinGame
                }
            }
        }
    }

    private fun startGame() {
        setState { joinedGame = true }
    }

    private fun joinGame(gameId: GameId): JoinGameFailure? {
        setState { joinedGame = true }
        return null
    }
}