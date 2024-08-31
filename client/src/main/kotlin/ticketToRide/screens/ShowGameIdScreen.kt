package ticketToRide.screens

import csstype.pct
import csstype.px
import emotion.react.css
import kotlinx.browser.window
import mui.material.*
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.p
import ticketToRide.*
import web.window.WindowTarget

external interface ShowGameIdScreenProps : Props {
    var gameId: GameId
    var locale: Locale
    var onClosed: () -> Unit
    var playerName: PlayerName
    var playerColor: PlayerColor
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
val ShowGameIdScreen = FC<ShowGameIdScreenProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }
    val gameUrl = window.location.href

    // navigator.clipboard can actually be undefined
    @Suppress("UNNECESSARY_SAFE_CALL")
    window.navigator.clipboard?.writeText(gameUrl)

    Dialog {
        sx {
            width = 100.pct
            margin = 0.px
        }
        open = true
        maxWidth = "sm"
        fullWidth = true
        onKeyDown = { e ->
            if (e.key == "Esc") props.onClosed()
        }

        DialogContent {
            p {
                +str.sendThisLinkToOtherPlayers
                if (window.navigator.clipboard != undefined)
                    +str.itIsAlreadyInClipboard
            }
            p {
                a {
                    href = gameUrl
                    target = WindowTarget._blank
                    +gameUrl
                }
            }
            p {
                +"Player Name: ${props.playerName.value}"
            }
            p {
                +"Player Color: ${props.playerColor.name}"
            }
        }
        DialogActions {
            Button {
                +str.ok
                color = ButtonColor.primary
                variant = ButtonVariant.contained
                onClick = { props.onClosed() }
            }
        }
    }
}

private fun strings(locale: Locale) = object : LocalizedStrings({ locale }) {

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
