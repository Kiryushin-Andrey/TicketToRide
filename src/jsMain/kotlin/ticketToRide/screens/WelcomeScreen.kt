package ticketToRide.screens

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.*
import com.ccfraser.muirwik.components.dialog.*
import com.ccfraser.muirwik.components.expansionpanel.*
import com.ccfraser.muirwik.components.input.*
import com.ccfraser.muirwik.components.menu.mMenuItem
import kotlinx.css.*
import kotlinx.css.properties.BoxShadows
import kotlinx.html.InputType
import org.w3c.dom.HTMLInputElement
import org.w3c.notifications.*
import react.*
import styled.*
import ticketToRide.*
import ticketToRide.components.withClasses
import kotlin.browser.window

class WelcomeScreen(props: Props) : RComponent<WelcomeScreen.Props, WelcomeScreen.State>(props) {

    interface State : RState {
        var playerName: String
        var errorText: String?
        var carsNumber: Int
    }

    interface Props : RProps {
        var locale: Locale
        var onLocaleChanged: (Locale) -> Unit
        var onStartGame: (PlayerName, Int) -> Unit
        var onJoinGame: (GameId, PlayerName) -> Unit
    }

    private val gameId =
        if (window.location.pathname.startsWith("/game/")) window.location.pathname.substringAfterLast('/')
        else null

    override fun State.init(props: Props) {
        carsNumber = 45
    }

    override fun RBuilder.render() {
        mDialog {
            css {
                +ComponentStyles.welcomeDialog
            }
            attrs {
                open = true
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
                if (gameId == null) {
                    mExpansionPanel {
                        attrs {
                            withClasses(
                                "root" to ComponentStyles.getClassName { it::settingsPanel },
                                "expanded" to ComponentStyles.getClassName { it::settingsPanelExpanded })
                        }

                        mExpansionPanelSummary {
                            attrs {
                                css {
                                    padding = 0.px.toString()
                                }
                                expandIcon = buildElement { mIcon("expand_more") }!!
                                withClasses(
                                    "root" to ComponentStyles.getClassName { it::settingsPanelSummaryRoot },
                                    "content" to ComponentStyles.getClassName { it::settingsPanelSummaryContent },
                                    "expanded" to ComponentStyles.getClassName { it::settingsPanelExpanded })
                            }
                            mInputLabel(str.moreSettings)
                        }

                        mExpansionPanelDetails {
                            attrs {
                                withClasses(
                                    "root" to ComponentStyles.getClassName { it::settingsPanelContent }
                                )
                            }
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
                val btnTitle = if (gameId == null) str.startGame else str.joinGame
                mButton(btnTitle, MColor.primary, MButtonVariant.contained,
                    disabled = state.errorText != null,
                    onClick = { proceed() })
            }
        }
    }

    private fun proceed() {
        if (state.playerName.isNotBlank()) {
            Notification.requestPermission()
            val playerName = PlayerName(state.playerName)
            if (gameId == null)
                props.onStartGame(playerName, state.carsNumber)
            else
                props.onJoinGame(GameId(gameId), playerName)
        }
    }

    object ComponentStyles : StyleSheet("Welcome", isStatic = true) {
        val welcomeDialog by css {
            width = 100.pct
            margin = "0"
        }
        val settingsPanel by css {
            borderStyle = BorderStyle.none
            boxShadow = BoxShadows.none
            before { display = Display.none }
            "&.Mui-expanded" {
                minHeight = 0.px
                margin = 0.px.toString()
            }
        }
        val settingsPanelSummaryRoot by ComponentStyles.css {
            "&.Mui-expanded" {
                minHeight = 0.px
                margin = 0.px.toString()
            }
        }
        val settingsPanelSummaryContent by ComponentStyles.css {
            margin = 0.px.toString()
            padding = 0.px.toString()
            "&.Mui-expanded" {
                minHeight = 0.px
                margin = 0.px.toString()
            }
        }
        val settingsPanelContent by ComponentStyles.css {
            padding = 0.px.toString()
        }
        val settingsPanelExpanded by ComponentStyles.css {}
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