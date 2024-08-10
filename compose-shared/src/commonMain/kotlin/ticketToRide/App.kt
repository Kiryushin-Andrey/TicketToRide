package ticketToRide

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import ticketToRide.screens.GameInProgressScreen
import ticketToRide.screens.ShowGameIdScreen
import ticketToRide.screens.WelcomeScreen

@Composable
internal fun App(serverHost: String, initialGameId: GameId?, windowSizeClass: WindowSizeClass) {
    val appState = remember { AppStateVM(serverHost) }

    MaterialTheme(colorScheme) {
        CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
            when (val screenState = appState.screen) {
                is Screen.Welcome ->
                    WelcomeScreen(serverHost, initialGameId, appState)

                is Screen.ShowGameId ->
                    ShowGameIdScreen(serverHost, appState, screenState)

                is Screen.GameInProgress ->
                    GameInProgressScreen(screenState, appState, windowSizeClass)

                is Screen.ObserveGameInProgress -> {}

                is Screen.GameOver -> {}
            }
        }
    }
}

interface AppActions {
    fun onEnter(handler: () -> Unit)
    fun onEsc(handler: () -> Unit)
}

val LocalAppActions = compositionLocalOf<AppActions> {
    object : AppActions {
        override fun onEnter(handler: () -> Unit) {}
        override fun onEsc(handler: () -> Unit) {}
    }
}

enum class WindowSizeClass {
    Compact, Large
}

val LocalWindowSizeClass = compositionLocalOf { WindowSizeClass.Compact }
