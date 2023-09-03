package ticketToRide.composables.welcomeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging
import ticketToRide.*
import ticketToRide.localization.AppStrings

class JoinGameVM {
    var otherPlayers by mutableStateOf(emptyList<PlayerView>())
    var gameStartedBy by mutableStateOf("")
    var name by mutableStateOf("")
    var color by mutableStateOf<PlayerColor?>(PlayerColor.RED)
    var joinAsObserver by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
}

sealed class JoinGameDialogResult {
    class AsPlayer(val name: PlayerName, val color: PlayerColor) : JoinGameDialogResult()
    data object AsObserver : JoinGameDialogResult()
    data object StartNewGame : JoinGameDialogResult()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JoinGameDialog(
    serverHost: String,
    gameId: GameId,
    appStrings: AppStrings,
    showErrorMessage: (String) -> Unit,
    onSubmit: (JoinGameDialogResult) -> Unit
) {
    val vm = remember { JoinGameVM() }
    val textInputFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    val processGameStateUpdate = remember<(GameStateForObserver) -> Unit> {
        { response ->
            vm.gameStartedBy = response.gameStartedBy
            vm.otherPlayers = response.players
            val availableColors = vm.otherPlayers.getNotUsedColors(vm.name)
            if (availableColors.isEmpty()) {
                vm.errorMessage = "Maximum number of 5 players for the game has been reached"
                vm.color = null
            }
            else if (availableColors.size == 1 || vm.color == null) {
                vm.color = availableColors[0]
            }
            else if (vm.otherPlayers.any { it.color == vm.color }) {
                vm.color = availableColors.firstOrNull()
            }
        }
    }

    val availableColors = remember(vm.otherPlayers, vm.name) {
        vm.otherPlayers.getNotUsedColors(vm.name).also {
            if (it.size == 1) {
                vm.color = it[0]
            }
        }
    }

    DisposableEffect(gameId) {
        val peekPlayersConnection = ServerConnection(
            coroutineScope,
            serverHost,
            gameId.webSocketUrl,
            ConnectRequest.Observe,
            appStrings,
            showErrorMessage,
            log = { log.info { it } }
        ) {
            connect { response ->
                if (response is ConnectResponse.ObserverConnected) {
                    processGameStateUpdate(response.state)
                    run<GameStateForObserver>(onServerResponse = processGameStateUpdate)
                }
            }
        }
        onDispose {
            peekPlayersConnection.close()
        }
    }

    val submit = remember(vm, onSubmit) {
        {
            onSubmit(
                if (vm.joinAsObserver)
                    JoinGameDialogResult.AsObserver
                else
                    JoinGameDialogResult.AsPlayer(PlayerName(vm.name), vm.color!!)
            )
        }
    }

    val backToNewGame = remember(onSubmit) {
        { onSubmit(JoinGameDialogResult.StartNewGame) }
    }

    val appActions = LocalAppActions.current
    LaunchedEffect(appActions, textInputFocusRequester) {
        textInputFocusRequester.requestFocus()
        appActions.run {
            onEnter(submit)
            onEsc(backToNewGame)
        }
    }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Join a game started by ${vm.gameStartedBy}", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        PlayerNameInput(vm.name, { vm.name = it }, Modifier.focusRequester(textInputFocusRequester))
        Spacer(Modifier.height(8.dp))
        if (availableColors.isNotEmpty()) {
            PlayerColorSelector(vm.color, availableColors, {
                vm.color = it
            })
            Spacer(Modifier.height(8.dp))
            if (vm.otherPlayers.isNotEmpty()) {
                FlowRow {
                    vm.otherPlayers.forEachIndexed { ix, player ->
                        Box(
                            Modifier.size(8.dp).clip(CircleShape).background(player.color.color)
                                .align(Alignment.CenterVertically)
                        )
                        Spacer(Modifier.width(4.dp))
                        val separator =
                            if (ix == vm.otherPlayers.size - 2) " and" else if (ix < vm.otherPlayers.size - 2) "," else ""
                        Text(player.name.value + separator + " ")
                    }
                    // render separately to define borders for wrapping to the next line
                    val verb = if (vm.otherPlayers.size == 1) "is" else "are"
                    listOf(verb, "waiting", "for you!").forEach {
                        Text("$it ")
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        vm.errorMessage?.let {
            Text(it, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error))
            Spacer(Modifier.height(8.dp))
        }
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            Button(
                enabled = vm.color != null,
                onClick = submit
            ) {
                Text("Join game!")
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = backToNewGame) {
                Text("Start a new game")
            }
        }
    }
}

private val log = KotlinLogging.logger("JoinGameDialog")