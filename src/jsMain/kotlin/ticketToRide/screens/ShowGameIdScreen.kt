package ticketToRide.screens

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.*
import com.ccfraser.muirwik.components.dialog.*
import kotlinx.browser.window
import react.*
import react.dom.a
import react.dom.p
import styled.*
import ticketToRide.*

interface ShowGameIdScreenProps : RProps {
    var gameId: GameId
    var locale: Locale
    var onClosed: () -> Unit
}

class ShowGameIdScreen : RComponent<ShowGameIdScreenProps, RState>() {
    override fun RBuilder.render() {
        val gameUrl = window.location.href
        window.navigator.clipboard.writeText(gameUrl)
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
                    +str.sendThisLinkToOtherPlayers
                    if (window.navigator.clipboard != undefined)
                        +str.itIsAlreadyInClipboard
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
                mButton(str.ok, MColor.primary, MButtonVariant.contained, onClick = { props.onClosed() })
            }
        }
    }

    private inner class Strings : LocalizedStrings({ props.locale }) {

        val sendThisLinkToOtherPlayers by loc(
            Locale.En to "Send this link to other players",
            Locale.Ru to "Отправьте эту ссылку другим игрокам"
        )

        val itIsAlreadyInClipboard by loc(
            Locale.En to " (it is already in your clipboard)",
            Locale.Ru to " (она уже скопирована в буфер обмена)"
        )

        val ok by loc(Locale.En to "OK", Locale.Ru to "OK")
    }
    private val str = Strings()
}

fun RBuilder.showGameIdScreen(builder: ShowGameIdScreenProps.() -> Unit) {
    child(ShowGameIdScreen::class) {
        attrs {
            builder()
        }
    }
}