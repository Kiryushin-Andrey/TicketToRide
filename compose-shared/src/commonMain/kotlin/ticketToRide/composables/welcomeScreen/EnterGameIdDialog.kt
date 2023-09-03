package ticketToRide.composables.welcomeScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ticketToRide.GameId
import ticketToRide.LocalAppActions
import ticketToRide.ServerConnection
import java.net.ConnectException

@Composable
fun EnterGameIdDialog(serverHost: String, onSubmit: (GameId?) -> Unit) {
    val text = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val showLoader = remember { mutableStateOf(false) }

    val textInputFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    val submit = remember {
        {
            coroutineScope.submitButtonClick(text, serverHost, showLoader, errorMessage, onSubmit)
        }
    }

    val backToNewGame = remember {
        { onSubmit(null) }
    }

    val appActions = LocalAppActions.current
    LaunchedEffect(appActions, textInputFocusRequester) {
        textInputFocusRequester.requestFocus()
        appActions.run {
            onEnter(submit)
            onEsc(backToNewGame)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Join a game started by someone else", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = text.value,
            onValueChange = { text.value = it },
            singleLine = true,
            placeholder = {
                Text("Game url or id", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
            },
            modifier = Modifier.fillMaxWidth().focusRequester(textInputFocusRequester)
        )
        errorMessage.value?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error))
        }
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (showLoader.value) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(8.dp))
            Button(modifier = Modifier, onClick = submit) {
                Text("Join")
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = backToNewGame) {
                Text("Start new game")
            }
        }
    }
}

private fun CoroutineScope.submitButtonClick(
    text: MutableState<String>,
    serverHost: String,
    showLoader: MutableState<Boolean>,
    errorMessage: MutableState<String?>,
    onSubmit: (GameId?) -> Unit
) {
    errorMessage.value = null
    val gameId = text.value.takeUnless { it.isBlank() }?.removePrefix("$serverHost/game/")?.let { GameId(it) }
    if (gameId == null) {
        errorMessage.value = "Enter game url or id"
        return
    }

    launch {
        showLoader.value = true
        try {
            val startedBy = ServerConnection.getStartedByNameForGameId(serverHost, gameId)
            if (startedBy == null) {
                errorMessage.value = "Cannot find specified game"
            } else {
                errorMessage.value = null
                onSubmit(gameId)
            }
        } catch (ex: ConnectException) {
            logger.warn(ex) { "Cannot connect to the server" }
            errorMessage.value = "Cannot connect to the server"
        } catch (ex: Exception) {
            logger.warn(ex) { "Error trying to check whether a game with specified id exists on the server" }
            errorMessage.value = ex.message ?: ex.javaClass.simpleName
        } finally {
            showLoader.value = false
        }
    }
}

private val logger = KotlinLogging.logger("EnterGameIdDialog")
