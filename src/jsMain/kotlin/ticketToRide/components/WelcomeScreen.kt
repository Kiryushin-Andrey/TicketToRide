package ticketToRide.components

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.*
import com.ccfraser.muirwik.components.dialog.*
import kotlinx.css.*
import org.w3c.dom.url.URLSearchParams
import react.*
import styled.*
import ticketToRide.*
import kotlin.browser.window

external interface WelcomeScreenProps : RProps {
    var onStartGame: () -> Unit
    var onJoinGame: (GameId) -> JoinGameFailure?
}

external interface WelcomeScreenState : RState {
    var playerName: String
    var joinGameFailure: JoinGameFailure?
}

class WelcomeScreen() : RComponent<WelcomeScreenProps, WelcomeScreenState>() {
    override fun RBuilder.render() {
        val gameId = URLSearchParams(window.location.search).get("game")
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
                        if (gameId == null) {
                            props.onStartGame()
                        } else {
                            val failure = props.onJoinGame(GameId(gameId))
                            setState { joinGameFailure = failure }
                        }
                    })
            }
        }
    }

    private object ComponentStyles : StyleSheet("Welcome", isStatic = true) {
        val welcomeDialog by css {
            width = 100.pct
            margin = "0"
        }
    }
}