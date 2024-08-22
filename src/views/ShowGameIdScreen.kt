import mui.material.Box
import mui.material.Dialog
import mui.material.DialogContent
import mui.material.DialogTitle
import mui.material.Typography
import mui.system.sx
import csstype.px
import csstype.rgb
import csstype.percentage
import react.FC
import react.Props
import ticket.to.ride.context.LocaleContext
import ticket.to.ride.context.ThemeContext
import ticket.to.ride.i18n.getString
import ticket.to.ride.model.GameStateView
import ticket.to.ride.model.PlayerColor

// Props interface for ShowGameIdScreen component
external interface ShowGameIdScreenProps : Props {
    var gameState: GameStateView
}

// Function to create a colored circle
fun coloredCircle(playerColor: PlayerColor): Box {
    return Box {
        sx {
            backgroundColor = when (playerColor) {
                PlayerColor.RED -> rgb(255, 0, 0)
                PlayerColor.GREEN -> rgb(0, 255, 0)
                PlayerColor.BLUE -> rgb(0, 0, 255)
                PlayerColor.YELLOW -> rgb(255, 255, 0)
            }
            borderRadius = percentage(50)
            width = 16.px
            height = 16.px
            display = "inline-block"
            marginRight = 8.px
        }
    }
}

// ShowGameIdScreen component
val ShowGameIdScreen = FC<ShowGameIdScreenProps> { props ->
    val locale = useContext(LocaleContext)
    val theme = useContext(ThemeContext)

    val currentPlayer = props.gameState.me

    Dialog {
        open = true
        DialogTitle {
            +getString("show_game_id_dialog_title", locale)
        }
        DialogContent {
            Box {
                +getString("current_player", locale)
                ": "
                coloredCircle(currentPlayer.color)
                Typography {
                    +currentPlayer.name
                }
            }
            Box {
                // Placeholder for other elements, if any
            }
        }
    }
}

// Localization strings (for reference)
// en.json
// {
//   "show_game_id_dialog_title": "Game ID",
//   "current_player": "Current player:"
// }

// ru.json
// {
//   "show_game_id_dialog_title": "ID игры",
//   "current_player": "Текущий игрок:"
// }
