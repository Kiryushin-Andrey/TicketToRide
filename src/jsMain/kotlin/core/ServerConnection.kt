package core

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import org.w3c.dom.WebSocket
import org.w3c.dom.events.Event
import kotlin.browser.window

class ServerConnection<TConnectRequest, TCannotJoinReason>(
    parentScope: CoroutineScope,
    private val url: String,
    private val connectRequestSerializer: KSerializer<TConnectRequest>,
    private val cannotJoinReasonSerializer: KSerializer<TCannotJoinReason>,
    establishConnection: suspend ServerConnection<TConnectRequest, TCannotJoinReason>.() -> Unit
) {
    companion object {
        const val MaxRetriesCount = 5
    }

    private val scope = CoroutineScope(parentScope.coroutineContext + Job())

    private val _state = MutableStateFlow(ConnectionState.NotConnected)
    val connectionState: StateFlow<ConnectionState> get() = _state

    private val _responses = Channel<String>()

    var onDisconnected: (String?) -> Unit = {}

    private var wsDeferred = CompletableDeferred<WebSocket?>()
    private fun getWebSocketOrNull() = wsDeferred.takeIf { it.isCompleted }?.getCompleted()
    private val closeWebSocket: (Event) -> Unit = {
        getWebSocketOrNull()?.apply {
            onclose = null
            close()
        }
    }

    private var sendRequestsJob: Job? = null
    private val pingTimerHandle: Int

    init {
        pingTimerHandle = window.setInterval({
            getWebSocketOrNull()?.takeIf { it.readyState == 1.toShort() /* OPEN */ }?.send(PingPong.Ping)
        }, 10000)

        window.addEventListener("onbeforeunload", closeWebSocket)
        window.onbeforeunload = {
            getWebSocketOrNull()?.apply {
                onclose = null
                close()
            }
            undefined
        }

        scope.launch { establishConnection() }
    }

    suspend fun reconnect(
        request: TConnectRequest,
        displayConnectionOutcome: (ConnectResponse<TCannotJoinReason>) -> Unit
    ): ConnectResponse<TCannotJoinReason> {
        assertState(
            ConnectionState.Reconnecting,
            ConnectionState.CannotConnect
        )
        _state.value = ConnectionState.Reconnecting
        wsDeferred = CompletableDeferred()
        return connect(request, displayConnectionOutcome)
    }

    fun <T> runRequestSendingLoop(requests: ReceiveChannel<T>, serializer: SerializationStrategy<T>) {
        assertState(ConnectionState.Connected)
        sendRequestsJob?.cancel()
        sendRequestsJob = scope.launch {
            for (req in requests) {
                wsDeferred.await()?.send(json.stringify(serializer, req))
            }
        }
    }

    fun <T> responses(serializer: DeserializationStrategy<T>) =
        _responses.consumeAsFlow().map { json.parse(serializer, it) }

    fun close() {
        window.removeEventListener("onbeforeunload", closeWebSocket)
        window.clearInterval(pingTimerHandle)
        scope.cancel()
        getWebSocketOrNull()?.apply {
            onopen = null
            onmessage = null
            onclose = null
            close()
        }
    }

    suspend fun connect(
        connectRequest: TConnectRequest,
        displayConnectionOutcome: (ConnectResponse<TCannotJoinReason>) -> Unit = {}
    ): ConnectResponse<TCannotJoinReason> {
        fun createWebSocket(retriesCount: Int = 0) {
            val protocol = if (window.location.protocol == "https:") "wss:" else "ws:"
            WebSocket("$protocol//" + window.location.host + url).apply {
                onopen = {
                    wsDeferred.complete(this)
                }
                onclose = {
                    onopen = null
                    onmessage = null
                    onclose = null
                    _state.value = ConnectionState.Reconnecting
                    if (retriesCount < MaxRetriesCount) {
                        createWebSocket(retriesCount + 1)
                    } else {
                        _state.value = ConnectionState.CannotConnect
                        wsDeferred.complete(null)
                    }
                }
            }
        }

        assertState(
            ConnectionState.NotConnected,
            ConnectionState.Reconnecting
        )
        createWebSocket()
        val ws = wsDeferred.await() ?: return ConnectResponse.CannotConnect
        val response = CompletableDeferred<ConnectResponse<TCannotJoinReason>>()
        ws.onmessage = { msg ->
            kotlin.runCatching {
                val reqStr = msg.data as? String
                if (reqStr == null) {
                    response.completeExceptionally(Error("Unexpected response from server: ${msg.data}"))
                } else {
                    val message = json.parse(ConnectResponse.serializer(cannotJoinReasonSerializer), reqStr)
                    when (message) {
                        is ConnectResponse.Success -> {
                            ws.onopen = null
                        }
                        is ConnectResponse.CannotJoin<TCannotJoinReason> -> {
                            ws.onclose = null
                            ws.close()
                            wsDeferred = CompletableDeferred()
                        }
                        else -> throw Error("Unexpected connect response from server")
                    }
                    response.complete(message)
                }
            }.onFailure { response.completeExceptionally(it) }
        }
        ws.send(json.stringify(connectRequestSerializer, connectRequest))
        return response.await().also { resp ->
            _state.value = when (resp) {
                is ConnectResponse.Success -> {
                    ws.onmessage = { msg ->
                        scope.launch {
                            (msg.data as? String)?.takeUnless { it == PingPong.Pong }?.let { reqStr ->
                                _responses.send(reqStr)
                            }
                        }
                    }
                    ws.onclose = { e ->
                        ws.apply {
                            onopen = null
                            onmessage = null
                            onclose = null
                        }
                        wsDeferred = CompletableDeferred()
                        _state.value = ConnectionState.Reconnecting
                        onDisconnected(e.asDynamic().reason as? String)
                    }
                    ConnectionState.Connected
                }
                is ConnectResponse.CannotConnect ->
                    ConnectionState.CannotConnect
                is ConnectResponse.CannotJoin ->
                    ConnectionState.CannotJoin
            }
            displayConnectionOutcome(resp)
        }
    }

    private fun assertState(vararg required: ConnectionState) {
        if (!required.contains(connectionState.value))
            throw Error("This operation was called in $connectionState state but is valid only in one of the following states: ${required.joinToString()}")
    }
}