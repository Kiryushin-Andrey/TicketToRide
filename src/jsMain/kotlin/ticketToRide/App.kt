package ticketToRide

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.serialization.json.*
import org.w3c.dom.WebSocket
import react.*
import ticketToRide.playerState.PlayerState
import ticketToRide.screens.*
import kotlin.browser.document
import kotlin.browser.window

enum class ActiveScreen {
    Welcome,
    ShowGameId,
    PlayGame,
    TheEnd
}

interface AppState : RState {
    var screen: Screen
}

private val json = Json(JsonConfiguration.Default.copy(allowStructuredMapKeys = true))

class App() : RComponent<RProps, AppState>() {
    private lateinit var scope: CoroutineScope
    private lateinit var requests: Channel<Request>

    override fun componentWillUnmount() {
        scope.cancel()
    }

    override fun AppState.init() {
        screen = Screen.Welcome()
        scope = CoroutineScope(Dispatchers.Default + Job())
        requests = Channel()
        
        scope.launch {
            val webSocket = WebSocket("ws://" + window.location.host + "/ws")
            launch {
                for (req in requests) {
                    webSocket.send(json.stringify(Request.serializer(), req))
                }
            }
            webSocket.onmessage = { msg ->
                (msg.data as? String)?.let { reqStr ->
                    when (val req = json.parse(Response.serializer(), reqStr)) {
                        is FailureResponse -> setState {
                            screen = Screen.Welcome(req.reason)
                        }
                        is GameStateResponse -> with (state.screen) {
                            when (this) {
                                is Screen.Welcome -> {
                                    setState {
                                        screen = if (req.state.players.size == 1) {
                                            val url = "${window.location.origin}/game/${req.gameId.value}"
                                            window.history.pushState(null, window.document.title, url)
                                            Screen.ShowGameId(req.gameId, req.state)
                                        } else {
                                            Screen.GameInProgress(
                                                req.gameId,
                                                GameMap,
                                                req.state,
                                                PlayerState.initial(GameMap, req.state, requests)
                                            )
                                        }
                                    }
                                }
                                is Screen.ShowGameId -> {
                                    setState {
                                        screen = copy(gameState = req.state)
                                    }
                                }
                                is Screen.GameInProgress -> {
                                    if (!gameState.myTurn && req.state.myTurn) {
                                        document.title = "ВАШ ХОД  - Ticket to Ride!"
                                        window.setTimeout({ document.title = "Ticket to Ride!" }, 3000)
                                    }
                                    val newPlayerState =
                                        if (playerState is PlayerState.ChoosingTickets) playerState
                                        else PlayerState.initial(gameMap, req.state, requests)
                                    setState {
                                        screen = copy(gameState = req.state, playerState = newPlayerState)
                                    }
                                }
                                is Screen.GameOver -> {}
                            }
                        }
                        is GameEndResponse -> {
                            setState {
                                screen = Screen.GameOver(req.gameId, GameMap, req.players)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun RBuilder.render() = state.screen.let {
        when (it) {
            is Screen.Welcome ->
                welcomeScreen {
                    onStartGame = ::startGame
                    onJoinGame = ::joinGame
                }
            is Screen.ShowGameId ->
                showGameIdScreen {
                    gameId = it.gameId
                    onClosed = {
                        setState {
                            screen = Screen.GameInProgress(
                                it.gameId,
                                GameMap,
                                it.gameState,
                                PlayerState.initial(GameMap, it.gameState, requests)
                            )
                        }
                    }
                }
            is Screen.GameInProgress ->
                gameScreen {
                    gameMap = it.gameMap
                    gameState = it.gameState
                    playerState = it.playerState
                    onAction = { newState -> setState { screen = it.copy(playerState = newState) } }
                }
            is Screen.GameOver ->
                endScreen {
                    gameMap = it.gameMap
                    players = it.players.map { (player, tickets) -> PlayerFinalStats(player, tickets) }
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