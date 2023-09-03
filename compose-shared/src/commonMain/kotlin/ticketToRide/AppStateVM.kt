package ticketToRide

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource
import java.io.Serializable
import java.nio.charset.Charset
import java.util.logging.Logger

class AppStateVM(rootScope: CoroutineScope, override val serverHost: String) : AppState {

    private val state = object {
        val locale = mutableStateOf(Locale.En)
        val connectionState = mutableStateOf(ConnectionState.NotConnected)
        val errorMessage = mutableStateOf("")
        val showErrorMessage = mutableStateOf(false)
        val screen = mutableStateOf<Screen>(Screen.Welcome)
        val map = mutableStateOf<GameMap?>(null)
        val chatMessages = mutableStateListOf<Response.ChatMessage>()
    }

    @OptIn(ExperimentalResourceApi::class)
    private val loadDefaultMapJob = rootScope.async {
        resource("default.map").readBytes().toString(Charset.defaultCharset())
            .let { (GameMap.parse(it) as Try.Success).value }
            .also {
                if (state.map.value == null) {
                    state.map.value = it
                }
            }
    }

    override val screen: Screen
        get() = state.screen.value

    override val locale: Locale
        get() = state.locale.value

    @OptIn(ExperimentalCoroutinesApi::class)
    override val map: GameMap
        get() = state.map.value ?: loadDefaultMapJob.getCompleted()

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

    override fun initMap(map: GameMap) {
        state.map.value = map
        state.showErrorMessage.value = false
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