package ticketToRide

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import io.github.oshai.kotlinlogging.KotlinLogging

class AppStateVM(override val serverHost: String) : AppState {

    private val state = object {
        val locale = mutableStateOf(Locale.En)
        val connectionState = mutableStateOf(ConnectionState.NotConnected)
        val errorMessage = mutableStateOf("")
        val showErrorMessage = mutableStateOf(false)
        val screen = mutableStateOf<Screen>(Screen.Welcome)
        val chatMessages = mutableStateListOf<Response.ChatMessage>()
    }

    override val screen: Screen
        get() = state.screen.value

    override val locale: Locale
        get() = state.locale.value

    override val chatMessages: Collection<Response.ChatMessage>
        get() = state.chatMessages

    override val connectionState: ConnectionState
        get() = state.connectionState.value

    override val errorMessage: String
        get() = state.errorMessage.value

    override val showErrorMessage: Boolean
        get() = state.showErrorMessage.value

    override fun log(message: String) {
        log.info { message }
    }

    override fun setConnectionState(state: ConnectionState, errorMessage: String?) {
        this.state.connectionState.value = state
        if (errorMessage != null)
            this.state.errorMessage.value = errorMessage
    }

    override fun updateScreen(screen: Screen) {
        state.screen.value = screen
    }

    override fun showErrorMessage(message: String) {
        state.errorMessage.value = message
        state.showErrorMessage.value = true
    }

    override fun appendChatMessage(message: Response.ChatMessage) {
        state.chatMessages.add(message)
    }

    override fun notifyOnYourTurn() {
        TODO("Not yet implemented")
    }
}

private val log = KotlinLogging.logger(AppStateVM::class.simpleName!!)