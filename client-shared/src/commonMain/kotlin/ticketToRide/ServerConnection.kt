package ticketToRide

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.util.reflect.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import ticketToRide.localization.AppStrings
import ticketToRide.serialization.json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

enum class ConnectionState {
    NotConnected,
    Connected,
    Reconnecting,
    CannotJoinGame,
    CannotConnect,
    Disconnected
}

interface IServerConnection {
    suspend fun reconnect()
}

private val client = HttpClient {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(json)
        pingInterval = 10_000
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ServerConnection(
    parentScope: CoroutineScope,
    private val serverUrl: String,
    private val path: String,
    private val connectRequest: ConnectRequest,
    private val appStrings: AppStrings,
    private val showErrorMessage: (String) -> Unit,
    val log: (String) -> Unit,
    val addExitAppListener: (handler: () -> Unit) -> Unit = {},
    establishConnection: suspend ServerConnection.() -> Unit
) : IServerConnection {

    companion object {
        suspend fun getStartedByNameForGameId(serverUrl: String, gameId: GameId): String? {
            val response = client.get(URLBuilder(serverUrl).apply { set(path = "/game-exists/${gameId.value}") }.build())
            return when (response.status) {
                HttpStatusCode.OK ->
                    response.bodyAsText()
                HttpStatusCode.NotFound ->
                    null
                else ->
                    error("Server responded with ${response.status}")
            }
        }
    }

    private val _scope = CoroutineScope(parentScope.coroutineContext + Job())

    private val _connectionState = MutableStateFlow(ConnectionState.NotConnected)

    private var connectRetriesCount = 0;

    private var wsDeferred = CompletableDeferred<DefaultClientWebSocketSession>()

    private fun getWebSocketOrNull() =
        wsDeferred.takeIf { it.isCompleted && it.getCompletionExceptionOrNull() == null }?.getCompleted()

    private suspend fun getWebSocketSession(timeout: Duration) = select {
        wsDeferred.onAwait { it }
        onTimeout(timeout) { null }
    }

    init {
        @Suppress("UNUSED_EXPRESSION")
        _scope.launch {
            establishConnection()
        }
    }

    override suspend fun reconnect() {
        _connectionState.value = ConnectionState.Reconnecting
        wsDeferred.completeExceptionally(ReconnectInProgressException())
        wsDeferred = CompletableDeferred()
        connect {
            if (it is ConnectResponse.Failure) {
                showErrorMessage(it.getErrorMessage(appStrings))
            }
        }
    }

    fun close() {
        _connectionState.value = ConnectionState.Disconnected
        _scope.launch {
            getWebSocketOrNull()?.close()
        }.invokeOnCompletion {
            _scope.cancel()
        }
    }

    suspend fun connect(handler: suspend ServerConnection.(ConnectResponse) -> Unit) {
        assertState(ConnectionState.NotConnected, ConnectionState.Reconnecting)
        val (session, connectResponse) = try {
            log("Establishing websocket connection")
            client.webSocketSession(host = Url(serverUrl).host, path = path).let { session ->
                session.sendSerialized(
                    if (_connectionState.value == ConnectionState.Reconnecting && connectRequest is ConnectRequest.AsPlayer)
                        ConnectRequest.Reconnect(connectRequest.playerName)
                    else
                        connectRequest
                )
                val connectResponse = session.receiveDeserialized<ConnectResponse>()
                if (connectResponse is ConnectResponse.Failure) {
                    _connectionState.value = if (connectResponse is ConnectResponse.Failure.CannotConnect)
                        ConnectionState.CannotConnect
                    else
                        ConnectionState.CannotJoinGame
                    session.close(CloseReason(CloseReason.Codes.NORMAL, connectResponse.toString()))
                } else {
                    _connectionState.value = ConnectionState.Connected
                }
                wsDeferred.complete(session)
                session to connectResponse
            }
        } catch (ex: Exception) {
            log("Caught error while trying to establish websocket connection (attempt $connectRetriesCount): ${ex::class.simpleName}, ${ex.message}")
            connectRetriesCount++
            if (connectRetriesCount < MAX_CONNECT_RETRIES_COUNT) {
                handleLostConnection(reason = null)
            } else {
                _connectionState.value = ConnectionState.CannotConnect
                showErrorMessage(appStrings.cannotConnect)
            }
            return
        }

        try {
            handler(this, connectResponse)
        } catch (ex: Exception) {
            log("Caught error while processing connect response: ${ex::class.simpleName}, ${ex.message}")
            if (_connectionState.value != ConnectionState.NotConnected) {
                val closeReason = session.closeReason.await()
                handleLostConnection(closeReason?.message)
            }
        }

        connectRetriesCount = 0
    }

    inline fun <reified T> run(
        requests: ReceiveChannel<Request>? = null,
        noinline onConnectionStateChange: (ConnectionState) -> Unit = {},
        noinline onServerResponse: (T) -> Unit,
    ) {
        run(requests, onConnectionStateChange, typeInfo<T>(), onServerResponse)
    }

    fun <T> run(
        requests: ReceiveChannel<Request>?,
        onConnectionStateChange: (ConnectionState) -> Unit,
        responseTypeInfo: TypeInfo,
        onServerResponse: (T) -> Unit
    ) {
        assertState(ConnectionState.Connected)
        if (requests != null) {
            _scope.launch {
                requests.consumeEach { send(it) }
            }
        }

        _scope.launch {
            _connectionState.collect(onConnectionStateChange)
        }

        _scope.launch {
            while (_scope.isActive) {
                val session = try {
                    getWebSocketSession(1.seconds)
                } catch (ex: ReconnectInProgressException) {
                    // reconnect is already in progress, wait for some time and retry
                    log("Got error while establishing websocket connection, reconnect is already in progress")
                    delay(1000)
                    break
                } catch (ex: Exception) {
                    // could not connect to websocket, initiate reconnect
                    log("Caught error while establishing websocket connection: ${ex::class.simpleName}, ${ex.message}")
                    handleLostConnection(reason = null)
                    delay(1000)
                    break
                }
                if (session == null) {
                    log("Timed out while trying to obtain websocket session")
                    delay(1000)
                    break
                }

                try {
                    val response = session.receiveDeserialized<T>(responseTypeInfo)
                    onServerResponse(response)
                } catch (ex: Exception) {
                    log("Caught error while receiving frames: ${ex::class.simpleName}, ${ex.message}")
                    if (_connectionState.value != ConnectionState.NotConnected) {
                        val closeReason = session.closeReason.await()
                        handleLostConnection(closeReason?.message)
                    }
                }
            }
        }
    }

    private fun handleLostConnection(reason: String?) {
        _connectionState.value = ConnectionState.Reconnecting
        if (!wsDeferred.isCompleted) {
            wsDeferred.completeExceptionally(ReconnectInProgressException())
        }
        wsDeferred = CompletableDeferred()
        _scope.launch {
            for (countdown in CONNECT_RETRY_TIMEOUT_SECS downTo 1) {
                showErrorMessage(appStrings.disconnected(reason to countdown))
                delay(1000)
            }
            showErrorMessage(appStrings.reconnecting)
            connect {
                if (it is ConnectResponse.Failure) {
                    showErrorMessage(it.getErrorMessage(appStrings))
                }
            }
        }
    }

    private suspend inline fun <reified T> send(request: T) {
        var webSocket = getWebSocketSession(1.seconds)
        while (webSocket == null) {
            webSocket = getWebSocketSession(1.seconds)
        }
        webSocket.sendSerialized<T>(request)
    }

    private fun assertState(vararg required: ConnectionState) {
        if (!required.contains(_connectionState.value))
            throw Error("This operation was called in ${_connectionState.value} state but is valid only in one of the following states: ${required.joinToString()}")
    }
}

private class ReconnectInProgressException : RuntimeException("Websocket connection is being re-established")

const val CONNECT_RETRY_TIMEOUT_SECS = 5
const val MAX_CONNECT_RETRIES_COUNT = 5
