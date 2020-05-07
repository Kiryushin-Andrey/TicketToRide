package ticketToRide.screens

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.*
import com.ccfraser.muirwik.components.dialog.*
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.a
import react.dom.p
import styled.*
import ticketToRide.*
import kotlin.browser.window

interface ShowGameIdScreenProps : RProps {
    var gameId: GameId
    var onClosed: () -> Unit
}

class ShowGameIdScreen : RComponent<ShowGameIdScreenProps, RState>() {
    override fun RBuilder.render() {
        val gameUrl = window.location.href
        window.navigator.clipboard?.writeText(gameUrl)
        mDialog {
            css {
                +WelcomeScreen.ComponentStyles.welcomeDialog
            }
            attrs {
                open = true
                maxWidth = "sm"
                fullWidth = true
                onKeyDown = { e ->
                    if (e.keyCode == 13) props.onClosed()
                }
            }
            mDialogContent {
                p {
                    +"Send this link to other players"
                    if (window.navigator.clipboard != undefined)
                        +" (it is already in your clipboard)"
                }
                p {
                    a {
                        attrs {
                            href = gameUrl
                            target = "_blank"
                        }
                        +gameUrl
                    }
                }
            }
            mDialogActions {
                mButton("OK", MColor.primary, MButtonVariant.contained, onClick = { props.onClosed() })
            }
        }
    }
}

fun RBuilder.showGameIdScreen(builder: ShowGameIdScreenProps.() -> Unit) {
    child(ShowGameIdScreen::class) {
        attrs {
            builder()
        }
    }
}