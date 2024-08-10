package ticketToRide.composables.welcomeScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import ticketToRide.*


class NewGameVM {
    var name by mutableStateOf("")
    var color by mutableStateOf(PlayerColor.RED)
    var carsNumber by mutableStateOf(45)
    var calculateScoreInProgress by mutableStateOf(true)
    var gameMap by mutableStateOf<ConnectRequest.StartGameMap?>(ConnectRequest.StartGameMap.BuiltIn(listOf("default")))
    var customMapParseErrors by mutableStateOf<List<GameMapParseError>?>(null)
    var errorMessage by mutableStateOf<String?>(null)
}

class GameSettings(
    val carsCount: Int,
    val calculateScoreInProgress: Boolean
)

@Composable
fun StartNewGameDialog(
    serverHost: String,
    onJoinGame: () -> Unit,
    onStartGame: (String, PlayerColor, ConnectRequest.StartGameMap, GameSettings) -> Unit,
) {
    val vm = remember { NewGameVM() }
    var showSettings by remember { mutableStateOf(false) }
    val textInputFocusRequester = remember { FocusRequester() }
    val startGame = remember(onStartGame, vm) {
        {
            val gameMap = vm.gameMap
            when {
                vm.name.isBlank() ->
                    vm.errorMessage = "Enter your name"
                gameMap == null ->
                    vm.errorMessage = "Choose or upload the game map"
                else -> {
                    val gameSettings = GameSettings(vm.carsNumber, vm.calculateScoreInProgress)
                    onStartGame(vm.name, vm.color, gameMap, gameSettings)
                }
            }
        }
    }

    val appActions = LocalAppActions.current
    LaunchedEffect(appActions, textInputFocusRequester) {
        textInputFocusRequester.requestFocus()
        appActions.onEnter(startGame)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Start a new game", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))

        PlayerNameInput(vm.name, { vm.name = it}, Modifier.focusRequester(textInputFocusRequester))
        Spacer(Modifier.height(8.dp))

        PlayerColorSelector(vm.color, ticketToRide.PlayerColor.entries, { vm.color = it })
        Spacer(Modifier.height(8.dp))

        GameMapSelector(vm, serverHost)
        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(showSettings) {
            NewGameSettings(vm)
            Spacer(Modifier.height(8.dp))
        }

        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            IconToggleButton(
                checked = showSettings,
                onCheckedChange = { showSettings = it },
            ) {
                Icon(
                    imageVector = if (showSettings) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings"
                )
            }
            Button(onClick = startGame) {
                Text("Start game")
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onJoinGame) {
                Text("Join game")
            }
        }

        vm.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error))
        }
    }
}
