package ticketToRide

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.serialization.json.*
import org.w3c.dom.WebSocket
import react.*
import ticketToRide.screens.*
import kotlin.browser.window

enum class ActiveScreen {
    Welcome,
    ShowGameId,
    PlayGame
}

external interface AppState : RState {
    var activeScreen: ActiveScreen
    var joinGameFailure: JoinGameFailure?
    var gameId: GameId
    var gameState: GameStateView
}

private val json = Json(JsonConfiguration.Default.copy(allowStructuredMapKeys = true))

class App() : RComponent<RProps, AppState>() {
    private lateinit var scope: CoroutineScope
    private lateinit var requests: Channel<Request>

    override fun componentWillUnmount() {
        super.componentWillUnmount()
        scope.cancel()
    }

    override fun AppState.init() {
        activeScreen = ActiveScreen.Welcome
        scope = CoroutineScope(Dispatchers.Default + Job())
        requests = Channel()

        scope.launch {
            val webSocket = WebSocket("ws://" + window.location.host + "/ws")
            launch {
                for (req in requests) {
                    webSocket.send(json.stringify(Request.serializer(), req))
                }
            }
            webSocket.onmessage = {
                (it.data as String?)?.let {
                    when (val req = json.parse(Response.serializer(), it)) {
                        is FailureResponse -> setState {
                            activeScreen = ActiveScreen.Welcome
                            joinGameFailure = req.reason
                        }
                        is GameStateResponse -> {
                            setState {
                                if (activeScreen == ActiveScreen.Welcome) {
                                    activeScreen = if (req.state.players.size == 1) {
                                        val url = "${window.location.origin}/game/${req.gameId.value}"
                                        window.history.pushState(null, window.document.title, url)
                                        ActiveScreen.ShowGameId
                                    } else {
                                        ActiveScreen.PlayGame
                                    }
                                    joinGameFailure = null
                                    gameId = req.gameId
                                }
                                gameState = req.state
                            }
                        }
                    }
                }
            }
        }
    }

    override fun RBuilder.render() {
        when (state.activeScreen) {
            ActiveScreen.Welcome ->
                child(WelcomeScreen::class) {
                    attrs {
                        onStartGame = ::startGame
                        onJoinGame = ::joinGame
                    }
                }
            ActiveScreen.ShowGameId ->
                child(ShowGameIdScreen::class) {
                    attrs {
                        gameId = state.gameId
                        onClosed = { setState { activeScreen = ActiveScreen.PlayGame } }
                    }
                }
            ActiveScreen.PlayGame ->
                child(GameScreen::class) {
                    attrs {
                        gameState = state.gameState
                        sendRequest = { requests.offer(it) }
                    }
                }
        }
    }

    private fun startGame(playerName: PlayerName) {
        requests.offer(StartGameRequest(playerName))
    }

    private fun joinGame(gameId: GameId, playerName: PlayerName) {
        requests.offer(JoinGameRequest(gameId, playerName))
    }
}