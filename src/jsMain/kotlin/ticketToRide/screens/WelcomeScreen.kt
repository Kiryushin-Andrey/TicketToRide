package ticketToRide.screens

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.dialog.mDialog
import com.ccfraser.muirwik.components.dialog.mDialogActions
import com.ccfraser.muirwik.components.dialog.mDialogContent
import com.ccfraser.muirwik.components.expansionpanel.mExpansionPanel
import com.ccfraser.muirwik.components.expansionpanel.mExpansionPanelDetails
import com.ccfraser.muirwik.components.expansionpanel.mExpansionPanelSummary
import com.ccfraser.muirwik.components.input.mInput
import com.ccfraser.muirwik.components.input.mInputLabel
import com.ccfraser.muirwik.components.input.type
import com.ccfraser.muirwik.components.menu.mMenuItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.css.*
import kotlinx.html.InputType
import org.w3c.dom.HTMLInputElement
import org.w3c.notifications.DEFAULT
import org.w3c.notifications.Notification
import org.w3c.notifications.NotificationPermission
import react.*
import styled.css
import styled.getClassName
import ticketToRide.*
import ticketToRide.components.*
import ticketToRide.components.welcomeScreen.CustomGameMap
import ticketToRide.components.welcomeScreen.CustomGameMapParseErrors
import ticketToRide.components.welcomeScreen.chooseGameMap
import ticketToRide.components.welcomeScreen.gameMapParseErrorsDialog

private val defaultMap = (kotlinext.js.require("default.map").default as String).let { GameMap.parse(it) }

class WelcomeScreen(props: Props) : RComponent<WelcomeScreen.Props, WelcomeScreen.State>(props) {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var peekPlayersConnection: ServerConnection? = null

    interface State : RState {
        var playerName: String
        var otherPlayers: List<PlayerView>
        var errorText: String?
        var carsNumber: Int
        var customMap: CustomGameMap?
        var gameMapParseErrors: CustomGameMapParseErrors?
    }

    interface Props : RProps {
        var gameId: GameId?
        var locale: Locale
        var onLocaleChanged: (Locale) -> Unit
        var onStartGame: (GameMap, PlayerName, Int) -> Unit
        var onJoinGame: (PlayerName) -> Unit
    }

    private val Props.startingNewGame get() = gameId == null

    override fun State.init(props: Props) {
        carsNumber = 45
        playerName = ""
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
                if (props.startingNewGame) {
                    mExpansionPanel {
                        attrs {
                            withClasses(
                                "root" to ComponentStyles.getClassName { it::expansionPanelRoot },
                                "expanded" to ComponentStyles.getClassName { it::expansionPanelExpanded })
                        }

                        mExpansionPanelSummary {
                            attrs {
                                css {
                                    padding = 0.px.toString()
                                }
                                expandIcon = buildElement { mIcon("expand_more") }!!
                                withClasses(
                                    "root" to ComponentStyles.getClassName { it::expansionPanelSummaryRoot },
                                    "content" to ComponentStyles.getClassName { it::expansionPanelSummaryContent },
                                    "expanded" to ComponentStyles.getClassName { it::expansionPanelExpanded })
                            }
                            mTypography(str.moreSettings, MTypographyVariant.body1)
                        }

                        mExpansionPanelDetails {
                            attrs {
                                withClasses(
                                    "root" to ComponentStyles.getClassName { it::expansionPanelDetailsRoot }
                                )
                            }

                            chooseGameMap(props.locale) {
                                customMap = state.customMap
                                onCustomMapChanged = { map -> setState { customMap = map } }
                                onShowParseErrors = { err -> setState { gameMapParseErrors = err } }
                            }
                            mDivider()
                            initialCarsOnHand()
                        }
                    }
                }
                if (Notification.permission == NotificationPermission.DEFAULT) {
                    mTypography(variant = MTypographyVariant.body1) {
                        +str.notificationNote
                    }
                }
            }
            mDialogActions {
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
                mButton(btnTitle, MColor.primary, MButtonVariant.contained,
                    disabled = state.errorText != null,
                    onClick = { proceed() })
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

    private fun RBuilder.initialCarsOnHand() {
        mInputLabel(str.numberOfCarsOnHand) {
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
        Notification.requestPermission()
        val playerName = PlayerName(state.playerName)
        if (props.startingNewGame) {
            val map = state.customMap?.map ?: (defaultMap as? Try.Success)?.value
            if (map != null) {
                for (entry in map.segments.groupingBy { it.color }
                    .fold(0) { acc, segment -> acc + segment.length }) {
                    console.log("${entry.key} - ${entry.value}")
                }
                props.onStartGame(map, playerName, state.carsNumber)
            } else {
                setState { errorText = str.noMapSelected }
            }
        } else {
            props.onJoinGame(playerName)
        }
    }

    private fun observeGamePlayers(gameId: GameId) {
        peekPlayersConnection = ServerConnection(scope, gameId.webSocketUrl) {
            if (connect(ConnectRequest.Observe) is ConnectResponse.Success) {
                responses(GameStateForObservers.serializer()).collect {
                    setState { otherPlayers = it.players }
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

        val moreSettings by loc(
            Locale.En to "More settings",
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
            Locale.En to "Please load game map file (see \"${moreSettings}\" section)",
            Locale.Ru to "Загрузите файл с картой для игры (в разделе \"${moreSettings}\")"
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