package ticketToRide

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.readTextAsState
import ticketToRide.screens.GameInProgressScreen
import ticketToRide.screens.ShowGameIdScreen
import ticketToRide.screens.WelcomeScreen

@Composable
internal fun App(serverHost: String, initialGameId: GameId?, windowSizeClass: WindowSizeClass) {
    val defaultMap by ticketToRide.common.MR.files.defaultMap.readTextAsState()
    if (defaultMap == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(MR.images.background),
                contentDescription = "Background",
                Modifier.fillMaxSize(),
                Alignment.Center,
                ContentScale.Crop
            )
        }
        return
    }

    val appState = remember(defaultMap) { AppStateVM(serverHost, defaultMap!!) }

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
