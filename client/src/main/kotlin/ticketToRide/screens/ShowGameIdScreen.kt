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
import ticketToRide.PlayerColor
import ticketToRide.GameStateView

external interface ShowGameIdScreenProps : Props {
    var gameId: GameId
    var locale: Locale
    var onClosed: () -> Unit
    var gameState: GameStateView
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
val ShowGameIdScreen = FC<ShowGameIdScreenProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }
    val gameUrl = window.location.href
    val currentPlayer = props.gameState.me

    // navigator.clipboard can actually be undefined
    @Suppress("UNNECESSARY_SAFE_CALL")
    window.navigator.clipboard?.writeText(gameUrl)

    fun ChildrenBuilder.coloredCircle(color: PlayerColor) {
        Box {
            sx {
                width = 20.px
                height = 20.px
                borderRadius = 50.pct
                backgroundColor = Color(color.rgb)
                display = Display.inlineBlock
                marginRight = 10.px
                verticalAlign = VerticalAlign.middle
            }
        }
    }

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
            Box {
                sx {
                    display = Display.flex
                    alignItems = AlignItems.center
                    marginBottom = 16.px
                }
                Typography {
                    variant = TypographyVariant.body1
                    +str.currentPlayer
                }
            }
            Box {
                sx {
                    display = Display.flex
                    alignItems = AlignItems.center
                    marginBottom = 16.px
                }
                coloredCircle(currentPlayer.color)
                Typography {
                    variant = TypographyVariant.body1
                    +currentPlayer.name.value
                }
            }
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

    val currentPlayer by loc(
        Locale.En to "Current player:",
        Locale.Ru to "Текущий игрок:"
    )
}
