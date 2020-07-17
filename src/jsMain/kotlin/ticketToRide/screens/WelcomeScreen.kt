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
import styled.css
import styled.styledDiv
import ticketToRide.*
import ticketToRide.components.welcomeScreen.*

private val defaultMap = (kotlinext.js.require("default.map").default as String).let { GameMap.parse(it) }

class WelcomeScreen(props: Props) : RComponent<WelcomeScreen.Props, WelcomeScreen.State>(props) {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var peekPlayersConnection: ServerConnection? = null

    interface State : RState {
        var playerName: String
        var playerColor: PlayerColor?
        var otherPlayers: List<PlayerView>
        var errorText: String?
        var showSettings: Boolean
        var carsNumber: Int
        var customMap: CustomGameMap?
        var gameMapParseErrors: CustomGameMapParseErrors?
    }

    interface Props : RProps {
        var gameId: GameId?
        var locale: Locale
        var onLocaleChanged: (Locale) -> Unit
        var onStartGame: (GameMap, PlayerName, PlayerColor, Int) -> Unit
        var onJoinGame: (PlayerName, PlayerColor) -> Unit
        var onReconnect: (PlayerName) -> Unit
    }

    private val State.availableColors
        get() =
            otherPlayers.find { it.name.value == playerName }?.let {
                // if a player is reconnecting back into the game under the same name
                // she should keep the same color as before
                listOf(it.color)
            } ?: PlayerColor.values().filter { c -> !otherPlayers.map { it.color }.contains(c) }

    private val Props.startingNewGame get() = gameId == null

    override fun State.init(props: Props) {
        carsNumber = 45
        playerName = ""
        playerColor = PlayerColor.values().first()
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
                playerName()
                playerColor()
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
                mTooltip(str.settings) {
                    mIconButton("settings") {
                        css { marginRight = 15.px }
                        attrs {
                            onClick = { setState { showSettings = !showSettings } }
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

    private fun proceed() {
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
                    props.onStartGame(map, playerName, state.playerColor!!, state.carsNumber)
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
        peekPlayersConnection = ServerConnection(scope, gameId.webSocketUrl) {
            if (connect(ConnectRequest.Observe) is ConnectResponse.Success) {
                responses(GameStateForObservers.serializer()).collect {
                    setState {
                        otherPlayers = it.players
                        if (otherPlayers.map { it.color }.contains(playerColor)) {
                            playerColor = availableColors.firstOrNull()
                        }
                    }
                }
            }
        }
    }

    object ComponentStyles : ExpansionPanelStyleSheet("Welcome") {
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

fun RBuilder.welcomeScreen(builder: WelcomeScreen.Props.() -> Unit) {
    child(WelcomeScreen::class) {
        attrs {
            builder()
        }
    }
}