import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import ticketToRide.AppActions
import ticketToRide.LocalAppActions
import ticketToRide.MainView

fun main() = application {
    val gameId = remember { mutableStateOf<String?>(null) }
    val appActions = remember { AppActionsImpl() }

    Window(
        state = WindowState(WindowPlacement.Fullscreen),
        icon = painterResource("favicon.ico"),
        onKeyEvent = appActions::handleKeyEvent,
        onCloseRequest = ::exitApplication,
        title = "Ticket to Ride"
    ) {
        CompositionLocalProvider(LocalAppActions provides appActions) {
            MainView(gameId.value)
        }
    }
}

private class AppActionsImpl : AppActions {
    private var enterHandler: (() -> Unit)? = null
    private var escapeHandler: (() -> Unit)? = null

    fun handleKeyEvent(keyEvent: KeyEvent): Boolean {
        when (keyEvent.key) {
            Key.Enter -> enterHandler?.invoke()
            Key.Escape -> escapeHandler?.invoke()
        }
        return true
    }

    override fun onEnter(handler: () -> Unit) {
        enterHandler = handler
    }

    override fun onEsc(handler: () -> Unit) {
        escapeHandler = handler
    }
}
