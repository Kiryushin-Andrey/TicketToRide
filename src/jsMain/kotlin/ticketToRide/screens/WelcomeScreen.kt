package ticketToRide.screens

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.*
import com.ccfraser.muirwik.components.dialog.*
import com.ccfraser.muirwik.components.expansionpanel.mExpansionPanel
import com.ccfraser.muirwik.components.expansionpanel.mExpansionPanelDetails
import com.ccfraser.muirwik.components.expansionpanel.mExpansionPanelSummary
import com.ccfraser.muirwik.components.input.mInput
import com.ccfraser.muirwik.components.input.mInputLabel
import com.ccfraser.muirwik.components.input.type
import kotlinx.css.*
import kotlinx.css.properties.BoxShadows
import kotlinx.html.InputType
import org.w3c.dom.HTMLInputElement
import org.w3c.notifications.DEFAULT
import org.w3c.notifications.Notification
import org.w3c.notifications.NotificationPermission
import react.*
import react.dom.b
import styled.*
import ticketToRide.*
import ticketToRide.components.withClasses
import kotlin.browser.window

interface WelcomeScreenProps : RProps {
    var onStartGame: (PlayerName, Int) -> Unit
    var onJoinGame: (GameId, PlayerName) -> Unit
}

interface WelcomeScreenState : RState {
    var playerName: String
    var errorText: String?
    var carsNumber: Int
}

class WelcomeScreen(props: WelcomeScreenProps) : RComponent<WelcomeScreenProps, WelcomeScreenState>(props) {
    private val gameId =
        if (window.location.pathname.startsWith("/game/")) window.location.pathname.substringAfterLast('/')
        else null

    override fun WelcomeScreenState.init(props: WelcomeScreenProps) {
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
                mTextField("Your name is", fullWidth = true) {
                    attrs {
                        error = state.errorText != null
                        helperText = state.errorText ?: ""
                        autoFocus = true
                        onChange = {
                            val value = it.targetInputValue.trim()
                            setState {
                                playerName = value
                                errorText = if (value.isBlank()) "Enter your name" else null
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
                            mInputLabel("More settings")
                        }

                        mExpansionPanelDetails {
                            attrs {
                                withClasses(
                                    "root" to ComponentStyles.getClassName { it::settingsPanelContent }
                                )
                            }
                            mInputLabel("Initial number of cars on hand") {
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
                        b { +"Note: " }
                        +" allow notifications to be notified when it's your turn to move even if the browser tab is inactive"
                    }
                }
            }
            mDialogActions {
                val btnTitle = if (gameId == null) "Start the Game!" else "Join the Game!"
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
}

fun RBuilder.welcomeScreen(builder: WelcomeScreenProps.() -> Unit) {
    child(WelcomeScreen::class) {
        attrs {
            builder()
        }
    }
}