package ticketToRide

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import org.w3c.dom.ARRAYBUFFER
import org.w3c.dom.BinaryType
import org.w3c.dom.WebSocket
import org.w3c.dom.events.Event
import kotlinx.browser.window

enum class ConnectionState {
    NotConnected,
    Connected,
    Reconnecting,
    CannotJoinGame,
    CannotConnect
}

interface IServerConnection {
    suspend fun reconnect(request: ConnectRequest): ConnectResponse
}

class ServerConnection<T>(
    parentScope: CoroutineScope,
    private val url: String,
    private val responseDeserializer: DeserializationStrategy<T>,
    establishConnection: suspend ServerConnection<T>.() -> Unit
) : IServerConnection {
    companion object {
        const val MaxRetriesCount = 5
    }

    private val scope = CoroutineScope(parentScope.coroutineContext + Job())

    private val _state = MutableStateFlow(ConnectionState.NotConnected)
    val connectionState: StateFlow<ConnectionState> get() = _state

    private val _responses = Channel<T>()

    var onDisconnected: (String?) -> Unit = {}

    private var wsDeferred = CompletableDeferred<Pair<WebSocket, Formatter>?>()
    private fun getWebSocketOrNull() = wsDeferred.takeIf { it.isCompleted }?.getCompleted()?.first
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
            getWebSocketOrNull()?.takeIf { it.readyState == 1.toShort() /* OPEN */ }?.send(Request.Ping)
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

    override suspend fun reconnect(request: ConnectRequest): ConnectResponse {
        assertState(ConnectionState.Reconnecting, ConnectionState.CannotConnect)
        _state.value = ConnectionState.Reconnecting
        wsDeferred = CompletableDeferred()
        return connect(request)
    }

    fun <T> runRequestSendingLoop(requests: ReceiveChannel<T>, requestSerializer: SerializationStrategy<T>) {
        assertState(ConnectionState.Connected)
        sendRequestsJob?.cancel()
        sendRequestsJob = scope.launch {
            for (req in requests) {
                wsDeferred.await()?.let { (ws, formatter) ->
                    formatter.send(ws, req, requestSerializer)
                }
            }
        }
    }

    fun responses() = _responses.consumeAsFlow()

    fun close() {
        window.removeEventListener("onbeforeunload", closeWebSocket)
//        window.clearInterval(pingTimerHandle)
        scope.cancel()
        getWebSocketOrNull()?.apply {
            onopen = null
            onmessage = null
            onclose = null
            close()
        }
    }

    suspend fun connect(connectRequest: ConnectRequest): ConnectResponse {
        fun createWebSocket(retriesCount: Int = 0) {
            val scheme = if (window.location.protocol == "https:") "wss:" else "ws:"
            val subProtocols = WireFormat.values().map { it.name }.toTypedArray()
            WebSocket("$scheme//" + window.location.host + url, subProtocols).apply {
                binaryType = BinaryType.ARRAYBUFFER

                onopen = {
                    val formatter = formatterByType.get(WireFormat.valueOf(protocol))
                            ?: throw Exception("Unsupported wire format sent by server - $protocol")
                    wsDeferred.complete(this to formatter)
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

        assertState(ConnectionState.NotConnected, ConnectionState.Reconnecting)
        createWebSocket()
        val (ws, formatter) = wsDeferred.await() ?: return ConnectResponse.Failure.CannotConnect
        val response = CompletableDeferred<ConnectResponse>()
        ws.onmessage = { msg ->
            kotlin.runCatching {
                val message = formatter.deserialize(msg, ConnectResponse.serializer())
                when (message) {
                    is ConnectResponse.PlayerConnected -> {
                        ws.onopen = null
                    }
                    is ConnectResponse.Failure -> {
                        ws.onclose = null
                        ws.close()
                        wsDeferred = CompletableDeferred()
                    }
                }
                response.complete(message)
            }.onFailure { response.completeExceptionally(it) }
        }
        formatter.send(ws, connectRequest, ConnectRequest.serializer())
        return response.await().also { resp ->
            _state.value = when (resp) {
                is ConnectResponse.Failure.CannotConnect ->
                    ConnectionState.CannotConnect
                is ConnectResponse.Failure ->
                    ConnectionState.CannotJoinGame
                else -> {
                    ws.onmessage = { msg ->
                        if (msg.data as? String != Response.Pong)
                            scope.launch {
                                _responses.send(formatter.deserialize(msg, responseDeserializer))
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
            }
        }
    }

    private fun assertState(vararg required: ConnectionState) {
        if (!required.contains(connectionState.value))
            throw Error("This operation was called in $connectionState state but is valid only in one of the following states: ${required.joinToString()}")
    }
}