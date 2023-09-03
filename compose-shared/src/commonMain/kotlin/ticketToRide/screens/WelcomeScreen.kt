package ticketToRide.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ticketToRide.*
import ticketToRide.composables.welcomeScreen.*
import ticketToRide.localization.AppStrings

@Composable
fun WelcomeScreen(
    serverHost: String,
    initialGameId: GameId?,
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val screenState = remember(initialGameId) {
        mutableStateOf(
            initialGameId?.let { WelcomeScreenState.JoinGame(it) } ?: WelcomeScreenState.StartNewGame
        )
    }
    val appStrings = remember(appState.locale) { AppStrings(appState.locale) }

    WelcomeScreen(appState, modifier) {
        when (val screen = screenState.value) {
            is WelcomeScreenState.StartNewGame ->
                StartNewGameDialog(
                    onJoinGame = {
                        screenState.value = initialGameId?.let { WelcomeScreenState.JoinGame(it) }
                            ?: WelcomeScreenState.EnterGameIdToJoin
                    },
                    onStartGame = { name, color, customMap, settings ->
                        customMap?.let { appState.initMap(it) }
                        startGame(
                            PlayerName(name),
                            color,
                            settings.carsCount,
                            settings.calculateScoreInProgress,
                            appState,
                            appStrings
                        )
                    }
                )

            is WelcomeScreenState.EnterGameIdToJoin ->
                EnterGameIdDialog(serverHost) { newGameId ->
                    screenState.value = newGameId?.let { WelcomeScreenState.JoinGame(it) } ?: WelcomeScreenState.StartNewGame
                }

            is WelcomeScreenState.JoinGame -> {
                JoinGameDialog(serverHost, screen.gameId, appStrings, appState::showErrorMessage) { choice ->
                    when (choice) {
                        is JoinGameDialogResult.AsPlayer ->
                            joinGame(
                                screen.gameId,
                                ConnectRequest.Join(choice.name, choice.color),
                                appState,
                                appStrings
                            )

                        is JoinGameDialogResult.AsObserver ->
                            joinAsObserver(screen.gameId, appState, appStrings)

                        is JoinGameDialogResult.StartNewGame ->
                            screenState.value = WelcomeScreenState.StartNewGame
                    }
                }
            }
        }
    }
}

private sealed class WelcomeScreenState {
    data object StartNewGame : WelcomeScreenState()
    data object EnterGameIdToJoin : WelcomeScreenState()
    data class JoinGame(val gameId: GameId) : WelcomeScreenState()
}

@Composable
fun ShowGameIdScreen(serverHost: String, appState: AppState, screenState: Screen.ShowGameId, modifier: Modifier = Modifier) {
    WelcomeScreen(appState, modifier) {
        ShowGameIdDialog("$serverHost/game/${screenState.gameId.value}") {
            appState.updateScreen(
                Screen.GameInProgress(
                    screenState.gameId,
                    screenState.gameState,
                    PlayerState.initial(appState.map, screenState.gameState)
                )
            )
        }
    }
}

@Composable
@OptIn(ExperimentalResourceApi::class)
private fun WelcomeScreen(appState: AppState, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxSize()) {
        Image(
            painter = painterResource("background.jpg"),
            contentDescription = "Background",
            Modifier.fillMaxSize(),
            Alignment.Center,
            ContentScale.Crop
        )

        val isCompactScreen = LocalWindowSizeClass.current == WindowSizeClass.Compact
        Card(
            modifier = if (isCompactScreen) {
                Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            } else {
                Modifier.widthIn(max = 600.dp).align(Alignment.Center)
            }
        ) {
            content()
        }

        if (appState.showErrorMessage) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = if (isCompactScreen) {
                    Modifier.fillMaxWidth().align(Alignment.TopCenter)
                } else {
                    Modifier.padding(bottom = 100.dp).widthIn(max = 600.dp).align(Alignment.BottomCenter)
                }
            ) {
                Text(appState.errorMessage, modifier = Modifier.padding(12.dp))
            }
        }
    }
}
