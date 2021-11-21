package ticketToRide.screens

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.*
import com.ccfraser.muirwik.components.dialog.*
import com.ccfraser.muirwik.components.input.*
import com.ccfraser.muirwik.components.menu.mMenuItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.css.*
import kotlinx.html.InputType
import org.w3c.dom.HTMLInputElement
import org.w3c.notifications.*
import react.*
import styled.StyleSheet
import styled.css
import styled.styledDiv
import ticketToRide.*
import ticketToRide.components.welcomeScreen.*

private val defaultMap = (kotlinext.js.require("default.map").default as String).let { GameMap.parse(it) }

external interface WelcomeScreenProps : RProps {
    var gameIdBoxed: IGameId?
    var locale: Locale
    var onLocaleChanged: (Locale) -> Unit
    var onStartGame: (GameMap, PlayerName, PlayerColor, Int, Boolean) -> Unit
    var onJoinGame: (PlayerName, PlayerColor) -> Unit
    var onJoinAsObserver: () -> Unit
    var onReconnect: (PlayerName) -> Unit
}
val WelcomeScreenProps.gameId get() = gameIdBoxed?.unboxed

external interface WelcomeScreenState : RState {
    var playerName: String
    var playerColor: PlayerColor?
    var otherPlayers: List<PlayerView>
    var errorText: String?
    var showSettings: Boolean
    var carsNumber: Int
    var calculateScoresInProcess: Boolean
    var joinAsObserver: Boolean
    var customMap: CustomGameMap?
    var gameMapParseErrors: CustomGameMapParseErrors?
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
class WelcomeScreen(props: WelcomeScreenProps) : RComponent<WelcomeScreenProps, WelcomeScreenState>(props) {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var peekPlayersConnection: ServerConnection<GameStateForObserver>? = null

    private val WelcomeScreenState.availableColors
        get() =
            otherPlayers.find { it.name.value == playerName }?.let {
                // if a player is reconnecting back into the game under the same name
                // she should keep the same color as before
                listOf(it.color)
            } ?: PlayerColor.values().filter { c -> !otherPlayers.map { it.color }.contains(c) }

    private val WelcomeScreenProps.startingNewGame get() = gameIdBoxed == null

    override fun WelcomeScreenState.init(props: WelcomeScreenProps) {
        playerName = ""
        playerColor = PlayerColor.values().first()
        showSettings = false
        carsNumber = 45
        calculateScoresInProcess = false
        otherPlayers = emptyList()
    }

    override fun componentDidMount() {
        props.gameId?.let { observeGamePlayers(it) }
    }

    override fun componentWillUnmount() {
        peekPlayersConnection?.close()
        scope.cancel()
    }

    override fun RBuilder.render() {
        mDialog {
            css {
                +ComponentStyles.welcomeDialog
            }
            attrs {
                open = state.gameMapParseErrors == null
                maxWidth = "sm"
                fullWidth = true
            }
            mDialogContent {
                if (!state.joinAsObserver) {
                    playerName()
                    playerColor()
                }
                if (!props.startingNewGame) {
                    joinAsObserver()
                }
                if (state.showSettings) {
                    settings()
                }
                if (Notification.permission == NotificationPermission.DEFAULT) {
                    mTypography(variant = MTypographyVariant.body1) {
                        +str.notificationNote
                    }
                }
            }
            mDialogActions {
                if (props.startingNewGame) {
                    mTooltip(str.settings) {
                        mIconButton("settings") {
                            css { marginRight = 15.px }
                            attrs {
                                onClick = { setState { showSettings = !showSettings } }
                            }
                        }
                    }
                }
                mSelect(props.locale.toString()) {
                    css { marginRight = 15.px }
                    attrs {
                        onChange = { e, _ ->
                            val value = e.target?.asDynamic().value
                            props.onLocaleChanged(Locale.valueOf(value))
                        }
                    }
                    Locale.values().forEach {
                        mMenuItem {
                            attrs {
                                value = it.name
                                selected = props.locale == it
                            }
                            +it.name
                        }
                    }
                }

                val btnTitle = if (props.startingNewGame) str.startGame else str.joinGame
                mButton(btnTitle, MColor.primary, MButtonVariant.contained) {
                    attrs {
                        disabled = state.errorText != null
                        onClick = { proceed() }
                    }
                }
            }
        }

        state.gameMapParseErrors?.let {
            gameMapParseErrorsDialog {
                locale = props.locale
                filename = it.filename
                errors = it.errors
                onClose = { setState { gameMapParseErrors = null } }
            }
        }
    }

    private fun RBuilder.playerName() {
        mTextField(str.yourName, fullWidth = true) {
            attrs {
                error = state.errorText != null
                helperText = state.errorText ?: ""
                autoFocus = true
                onChange = {
                    val value = it.targetInputValue.trim()
                    setState {
                        playerName = value
                        errorText = if (value.isBlank()) str.enterYourName else null
                        otherPlayers.find { it.name.value == value }?.let {
                            playerColor = it.color
                        }
                    }
                }
                onKeyDown = { e ->
                    if (e.keyCode == 13) {
                        val text = (e.target as HTMLInputElement).value
                        setState({ it.apply { playerName = text } }, ::proceed)
                    }
                }
            }
        }
    }

    private fun RBuilder.playerColor() {
        mRadioGroup(state.playerColor?.name, row = true) {
            css {
                alignItems = Align.center
            }
            attrs {
                onChange = { _, newValue -> setState { playerColor = PlayerColor.valueOf(newValue) } }
            }
            mInputLabel(str.yourColor)
            state.availableColors.forEach {
                mRadio(color = MOptionColor.default) {
                    css {
                        color = Color(it.rgb)
                    }
                    attrs { value = it.name }
                }
            }
        }
    }

    private fun RBuilder.settings() {
        mPaper {
            css {
                marginTop = 15.px
                padding = 10.px.toString()
            }
            attrs { elevation = 2 }
            mTypography(str.settings, MTypographyVariant.h5)
            if (props.startingNewGame) {
                initialCarsOnHand()
                calculateScoresInProcess()
                chooseGameMap()
            }
        }
    }

    private fun RBuilder.chooseGameMap() {
        styledDiv {
            css { marginTop = 15.px }
            chooseGameMap(props.locale) {
                customMap = state.customMap
                onCustomMapChanged = { map -> setState { customMap = map } }
                onShowParseErrors = { err -> setState { gameMapParseErrors = err } }
            }
        }
    }

    private fun RBuilder.initialCarsOnHand() {
        mInputLabel(str.numberOfCarsOnHand) {
            css {
                marginTop = 15.px
                color = Color.black
            }
            mInput {
                css {
                    marginLeft = 10.px
                    width = 40.px
                }
                attrs {
                    type = InputType.number
                    value = state.carsNumber.toString()
                    asDynamic().min = 5
                    asDynamic().max = 60
                    onChange = { e ->
                        e.targetInputValue.trim().toIntOrNull()?.let {
                            setState { carsNumber = it }
                        }
                    }
                }
            }
        }
    }

    private fun RBuilder.calculateScoresInProcess() {
        mCheckboxWithLabel(str.calculateScoresInProcess, state.calculateScoresInProcess, MOptionColor.primary) {
            attrs {
                onChange = { _, value -> setState { calculateScoresInProcess = value } }
            }
        }
    }

    private fun RBuilder.joinAsObserver() {
        mCheckboxWithLabel(str.joinAsObserver, state.joinAsObserver, MOptionColor.primary) {
            attrs {
                onChange = { _, value -> setState { joinAsObserver = value } }
            }
        }
    }

    private fun proceed() {
        if (state.joinAsObserver) {
            props.onJoinAsObserver()
            return
        }

        if (state.playerName.isBlank()) {
            setState { errorText = str.enterYourName }
            return
        }
        if (state.playerColor == null) {
            setState { errorText = str.chooseYourColor }
            return
        }
        Notification.requestPermission()
        val playerName = PlayerName(state.playerName)
        when {
            props.startingNewGame -> {
                val map = state.customMap?.map ?: (defaultMap as? Try.Success)?.value
                if (map != null) {
                    for (entry in map.segments.groupingBy { it.color }
                        .fold(0) { acc, segment -> acc + segment.length }) {
                        console.log("${entry.key} - ${entry.value}")
                    }
                    props.onStartGame(
                        map,
                        playerName,
                        state.playerColor!!,
                        state.carsNumber,
                        state.calculateScoresInProcess
                    )
                } else {
                    setState { errorText = str.noMapSelected }
                }
            }

            state.otherPlayers.any { it.name == playerName } ->
                props.onReconnect(playerName)

            else ->
                props.onJoinGame(playerName, state.playerColor!!)
        }
    }

    private fun observeGamePlayers(gameId: GameId) {
        peekPlayersConnection = ServerConnection(scope, gameId.webSocketUrl, GameStateForObserver.serializer()) {
            val response = connect(ConnectRequest.Observe)
            if (response is ConnectResponse.ObserverConnected) {
                processGameState(response.state)
                responses().collect(::processGameState)
            }
        }
    }

    private fun processGameState(gameState: GameStateForObserver) {
        setState {
            otherPlayers = gameState.players
            if (otherPlayers.map { it.color }.contains(playerColor)) {
                playerColor = availableColors.firstOrNull()
            }
        }
    }

    object ComponentStyles : StyleSheet("Welcome", isStatic = true) {
        val welcomeDialog by css {
            width = 100.pct
            margin = "0"
        }
    }

    private inner class Strings : LocalizedStrings({ props.locale }) {

        val yourName by loc(
            Locale.En to "Your name is",
            Locale.Ru to "Ваше имя"
        )

        val enterYourName by loc(
            Locale.En to "Enter your name",
            Locale.Ru to "Введите ваше имя"
        )

        val yourColor by loc(
            Locale.En to "Your color",
            Locale.Ru to "Ваш цвет"
        )

        val chooseYourColor by loc(
            Locale.En to "Choose your color",
            Locale.Ru to "Выберите ваш цвет"
        )

        val settings by loc(
            Locale.En to "Settings",
            Locale.Ru to "Настройки"
        )

        val numberOfCarsOnHand by loc(
            Locale.En to "Initial number of cars on hand",
            Locale.Ru to "Количество вагонов в начале игры"
        )

        val calculateScoresInProcess by loc(
            Locale.En to "Calculate scores during the game",
            Locale.Ru to "Подсчет очков в процессе игры"
        )

        val joinAsObserver by loc(
            Locale.En to "I'm an observer",
            Locale.Ru to "Наблюдать за игрой"
        )

        val notificationNote by loc(
            Locale.En to "Allow notifications to be notified when it's your turn to move even if the browser tab is inactive",
            Locale.Ru to "Уведомления подскажут, когда до вас дошла очередь ходить"
        )

        val noMapSelected by loc(
            Locale.En to "Please load game map file (see \"${settings}\" section)",
            Locale.Ru to "Загрузите файл с картой для игры (в разделе \"${settings}\")"
        )

        val startGame by loc(
            Locale.En to "Start game!",
            Locale.Ru to "Начать игру!"
        )

        val joinGame by loc(
            Locale.En to "Join game!",
            Locale.Ru to "Присоединиться к игре!"
        )
    }

    private val str = Strings()
}

fun RBuilder.welcomeScreen(builder: WelcomeScreenProps.() -> Unit) {
    child(WelcomeScreen::class) {
        attrs {
            builder()
        }
    }
}