package ticketToRide

import csstype.*
import emotion.react.css
import hookstate.Hookstate
import hookstate.HookstateRoot
import hookstate.useHookstate
import js.core.jso
import kotlinx.browser.window
import mui.icons.material.Autorenew
import mui.icons.material.ErrorOutline
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import org.w3c.notifications.Notification
import org.w3c.notifications.NotificationOptions
import react.*
import ticketToRide.ConnectionState.*
import ticketToRide.localization.AppStrings
import ticketToRide.screens.*

external interface AppProps: Props {
    var onGameStarted: () -> Unit
}

private const val ERROR_MESSAGE_TIMEOUT_SECS = 4

external interface AppReactState : HookstateRoot<AppReactStateValue> {
    var screen: Hookstate<Screen>
    var locale: Hookstate<Locale>
    var map: Hookstate<GameMap>
    var chatMessages: Hookstate<Array<Response.ChatMessage>>
    var connectionState: Hookstate<ConnectionState>
    var errorMessage: Hookstate<String>
    var showErrorMessage: Hookstate<Boolean>
}

external interface AppReactStateValue {
    var screen: Screen
    var locale: Locale
    var map: GameMap
    var chatMessages: Array<Response.ChatMessage>
    var errorMessage: String
    var connectionState: ConnectionState
    var showErrorMessage: Boolean
}

val App = FC<AppProps> { props ->
    val appReactState: AppReactState = useHookstate(jso<AppReactStateValue> {
        screen = Screen.Welcome
        locale = Locale.En
        connectionState = NotConnected
        errorMessage = ""
        chatMessages = emptyArray()
    })

    val str = useMemo(appReactState.locale.get()) { AppStrings(appReactState.locale.get()) }

    val appState: AppState = useMemo(appReactState) {
        object : AppState {
            override val serverHost = window.location.host

            override fun log(message: String) {
                console.log(message)
            }

            override val screen: Screen
                get() = appReactState.screen.get()
            override val locale: Locale
                get() = appReactState.locale.get()
            override val map: GameMap
                get() = appReactState.map.get()
            override val chatMessages: Collection<Response.ChatMessage>
                get() = appReactState.chatMessages.get().asList()
            override val connectionState: ConnectionState
                get() = appReactState.connectionState.get()
            override val errorMessage: String
                get() = appReactState.errorMessage.get()
            override val showErrorMessage: Boolean
                get() = appReactState.showErrorMessage.get()

            override fun initMap(map: GameMap) {
                appReactState.merge(jso<AppReactStateValue> {
                    this.map = map
                    showErrorMessage = false
                })
            }

            override fun setConnectionState(state: ConnectionState, errorMessage: String?) {
                appReactState.merge(jso<AppReactStateValue> {
                    connectionState = state
                    errorMessage?.let {
                        this.errorMessage = it
                    }
                })
            }

            override fun updateScreen(screen: Screen) {
                appReactState.screen.set(screen)
            }

            override fun showErrorMessage(message: String) {
                appReactState.merge(jso<AppReactStateValue> {
                    errorMessage = message
                    showErrorMessage = true
                })
            }

            override fun appendChatMessage(message: Response.ChatMessage) {
                appReactState.chatMessages.merge(arrayOf(message))
            }

            override fun notifyOnYourTurn() {
                Notification("Ticket to Ride", object : NotificationOptions {
                    override var body: String? = str.yourTurn
                    override var icon: String? = "/favicon.ico"
                    override var silent: Boolean? = true
                    override var renotify: Boolean? = false
                    override var tag: String? = "your-turn"
                })
            }
        }
    }

    val screen = appState.screen

    useEffect(screen::class.simpleName) {
        if (screen !is Screen.Welcome && screen !is Screen.ShowGameId) {
            props.onGameStarted()
        }
        if (screen is Screen.ShowGameId && screen.gameState.players.size == 1) {
            window.history.pushState(
                data = null,
                title = window.document.title,
                url = "${window.location.origin}/game/${screen.gameId.value}"
            )
        }
    }

    when (screen) {
        is Screen.Welcome -> {
            WelcomeScreen {
                locale = appState.locale
                onLocaleChanged = { appReactState.locale.set(it) }
                onStartGame = { map, playerName, playerColor, carsCount, calculateScoresInProcess ->
                    startGame(playerName, playerColor, map, carsCount, calculateScoresInProcess, appState, str)
                }
                onJoinGame = { gameId, name, color ->
                    joinGame(gameId, ConnectRequest.Join(name, color), appState, str)
                }
                onJoinAsObserver = { gameId ->
                    joinAsObserver(gameId, appState, str)
                }
                onReconnect = { gameId, name ->
                    joinGame(gameId, ConnectRequest.Reconnect(name), appState, str)
                }
                showErrorMessage = {
                    appState.showErrorMessage(it)
                }
            }
        }

        is Screen.ShowGameId ->
            ShowGameIdScreen {
                gameId = screen.gameId
                locale = appState.locale
                onClosed = {
                    props.onGameStarted()
                    appState.updateScreen(
                        Screen.GameInProgress(
                            screen.gameId,
                            screen.gameState,
                            PlayerState.initial(appState.map, screen.gameState)
                        )
                    )
                }
            }

        is Screen.GameInProgress ->
            GameScreen {
                locale = appState.locale
                connected = appState.connectionState == Connected
                gameMap = appState.map
                gameState = screen.gameState
                playerState = screen.playerState
                act = { action -> appReactState.screen.set(screen.copy(playerState = screen.playerState.action())) }
                chatMessages = appState.chatMessages
                onSendMessage = { sendToServer(ChatMessage(it)) }
            }

        is Screen.GameOver -> {
            val allPlayers = screen.players.map { it.first }
            FinalScreen {
                locale = appState.locale
                gameMap = appState.map
                chatMessages = appState.chatMessages
                observing = screen.observing
                players = screen.players.map { (player, tickets) ->
                    PlayerScore(
                        player.name,
                        player.color,
                        player.occupiedSegments,
                        player.placedStations,
                        tickets,
                        allPlayers.filter { it != player }.flatMap { it.occupiedSegments },
                        appState.map
                    )
                }
                onSendMessage = { sendToServer(ChatMessage(it)) }
            }
        }

        is Screen.ObserveGameInProgress ->
            ObserveGameScreen {
                locale = appState.locale
                gameMap = appState.map
                chatMessages = appState.chatMessages
                connected = appState.connectionState == Connected
                gameState = screen.gameState
            }
    }

    errorMessage(appState, str) { appReactState.showErrorMessage.set(false) }
}

private fun ChildrenBuilder.errorMessage(state: AppState, str: AppStrings, reset: () -> Unit) {
    Snackbar {
        anchorOrigin = jso {
            horizontal = SnackbarOriginHorizontal.center
            vertical = SnackbarOriginVertical.bottom
        }
        open = state.showErrorMessage || state.connectionState.showErrorMessage
        onClose = { _, _ -> reset() }
        if (state.connectionState == CannotJoinGame)
            autoHideDuration = ERROR_MESSAGE_TIMEOUT_SECS * 1000

        Paper {
            elevation = 6
            sx {
                display = Display.flex
                flexDirection = FlexDirection.row
                padding = 10.px
                color = NamedColor.white
                backgroundColor = NamedColor.orangered
                minWidth = 300.px
                maxWidth = 600.px

                alignItems = AlignItems.center
            }

            ErrorOutline {
                sx { marginRight = 12.px }
            }
            Typography {
                variant = TypographyVariant.body1
                +state.errorMessage
            }

            if (state.connectionState == CannotConnect) {
                Tooltip {
                    title = ReactNode(str.retryConnect)
                    IconButton {
                        sx { marginLeft = 10.pt }
                        onClick = { reconnect() }
                        Autorenew {
                            css { color = NamedColor.white }
                        }
                    }
                }
            }
        }
    }
}
