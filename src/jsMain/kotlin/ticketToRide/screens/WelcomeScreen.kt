package ticketToRide.screens

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.*
import com.ccfraser.muirwik.components.dialog.*
import kotlinx.css.*
import org.w3c.dom.HTMLInputElement
import org.w3c.notifications.DEFAULT
import org.w3c.notifications.Notification
import org.w3c.notifications.NotificationPermission
import react.*
import react.dom.b
import styled.*
import ticketToRide.*
import kotlin.browser.window

interface WelcomeScreenProps : RProps {
    var onStartGame: (PlayerName) -> Unit
    var onJoinGame: (GameId, PlayerName) -> Unit
    var joinGameFailure: JoinGameFailure?
}

interface WelcomeScreenState : RState {
    var playerName: String
    var errorText: String?
}

class WelcomeScreen(props: WelcomeScreenProps) : RComponent<WelcomeScreenProps, WelcomeScreenState>(props) {
    private val gameId =
        if (window.location.pathname.startsWith("/game/")) window.location.pathname.substringAfterLast('/')
        else null

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
                props.onStartGame(playerName)
            else
                props.onJoinGame(GameId(gameId), playerName)
        }
    }

    object ComponentStyles : StyleSheet("Welcome", isStatic = true) {
        val welcomeDialog by css {
            width = 100.pct
            margin = "0"
        }
    }
}

fun RBuilder.welcomeScreen(builder: WelcomeScreenProps.() -> Unit) {
    child(WelcomeScreen::class) {
        attrs {
            builder()
        }
    }
}