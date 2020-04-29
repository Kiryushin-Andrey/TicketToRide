package ticketToRide

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.serialization.json.*
import org.w3c.dom.WebSocket
import react.*
import ticketToRide.playerState.PlayerState
import ticketToRide.screens.*
import kotlin.browser.window

interface AppState : RState {
    var screen: Screen
    var chatMessages: MutableList<Response.ChatMessage>
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
        chatMessages = mutableListOf()
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
                    processMessageFromServer(json.parse(Response.serializer(), reqStr))
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
                    chatMessages = state.chatMessages
                    onSendMessage = { message -> requests.offer(ChatMessageRequest(message)) }
                }

            is Screen.GameOver ->
                endScreen {
                    gameMap = it.gameMap
                    players = it.players.map { (player, tickets) -> PlayerFinalStats(player, tickets) }
                    chatMessages = state.chatMessages
                    onSendMessage = { message -> requests.offer(ChatMessageRequest(message)) }
                }
        }
    }

    private fun processMessageFromServer(msg: Response) = when (msg) {
        is Response.Error -> setState {
            screen = Screen.Welcome(msg.reason)
        }

        is Response.ChatMessage -> setState {
            chatMessages = chatMessages.apply { add(msg) }
        }

        is Response.GameState -> with(state.screen) {
            when (this) {

                is Screen.Welcome -> {
                    setState {
                        screen = if (msg.state.players.size == 1) {
                            val url = "${window.location.origin}/game/${msg.gameId.value}"
                            window.history.pushState(null, window.document.title, url)
                            Screen.ShowGameId(msg.gameId, msg.state)
                        } else {
                            Screen.GameInProgress(
                                msg.gameId,
                                GameMap,
                                msg.state,
                                PlayerState.initial(GameMap, msg.state, requests)
                            )
                        }
                        chatMessages = chatMessages.apply { add(msg.action.chatMessage()) }
                    }
                }

                is Screen.ShowGameId -> setState {
                    screen = copy(gameState = msg.state)
                    chatMessages = chatMessages.apply { add(msg.action.chatMessage()) }
                }

                is Screen.GameInProgress -> {
                    if (!gameState.myTurn && msg.state.myTurn) {
                        kotlin.browser.document.title = "ВАШ ХОД  - Ticket to Ride!"
                        kotlin.browser.window.setTimeout({ kotlin.browser.document.title = "Ticket to Ride!" }, 3000)
                    }
                    val newPlayerState =
                        if (playerState is PlayerState.ChoosingTickets) playerState
                        else PlayerState.initial(gameMap, msg.state, requests)
                    setState {
                        screen = copy(gameState = msg.state, playerState = newPlayerState)
                        chatMessages = chatMessages.apply { add(msg.action.chatMessage()) }
                    }
                }

                is Screen.GameOver -> {}
            }

        }

        is Response.GameEnd -> setState {
            (state.screen as? Screen.GameInProgress)?.let {
                screen = Screen.GameOver(
                    msg.gameId,
                    GameMap,
                    msg.players,
                    it.gameState.me
                )
                msg.action?.chatMessage()?.let {
                    chatMessages = chatMessages.apply { add(it) }
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