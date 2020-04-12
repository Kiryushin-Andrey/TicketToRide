package ticketToRide.screens

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.*
import com.ccfraser.muirwik.components.dialog.*
import kotlinx.css.*
import react.*
import styled.*
import ticketToRide.*
import kotlin.browser.window

external interface WelcomeScreenProps : RProps {
    var onStartGame: (PlayerName) -> Unit
    var onJoinGame: (GameId, PlayerName) -> Unit
    var joinGameFailure: JoinGameFailure?
}

external interface WelcomeScreenState : RState {
    var playerName: String
    var joinGameFailure: JoinGameFailure?
}

class WelcomeScreen(props: WelcomeScreenProps) : RComponent<WelcomeScreenProps, WelcomeScreenState>(props) {
    override fun WelcomeScreenState.init(props: WelcomeScreenProps) {
        joinGameFailure = props.joinGameFailure
    }

    override fun RBuilder.render() {
        val gameId =
            if (window.location.pathname.startsWith("/game/")) window.location.pathname.substringAfterLast('/')
            else null
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
                        error = state.joinGameFailure != null
                        helperText = when (state.joinGameFailure) {
                            JoinGameFailure.GameNotExists -> "Game not found"
                            JoinGameFailure.PlayerNameEmpty -> "Enter your name"
                            JoinGameFailure.PlayerNameTaken -> "This name is already taken"
                            null -> ""
                        }
                        onChange = {
                            val value = it.targetInputValue.trim()
                            setState {
                                playerName = value
                                joinGameFailure = if (value == "") JoinGameFailure.PlayerNameEmpty else null
                            }
                        }
                    }
                }
            }
            mDialogActions {
                val btnTitle = if (gameId == null) "Start the Game!" else "Join the Game!"
                mButton(btnTitle, MColor.primary, MButtonVariant.contained,
                    disabled = state.joinGameFailure != null,
                    onClick = {
                        val playerName = PlayerName(state.playerName)
                        if (gameId == null)
                            props.onStartGame(playerName)
                        else
                            props.onJoinGame(GameId(gameId), playerName)
                    })
            }
        }
    }

    object ComponentStyles : StyleSheet("Welcome", isStatic = true) {
        val welcomeDialog by css {
            width = 100.pct
            margin = "0"
        }
    }
}