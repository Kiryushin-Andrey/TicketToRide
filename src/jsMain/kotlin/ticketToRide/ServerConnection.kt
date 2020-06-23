package ticketToRide

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.w3c.dom.WebSocket
import kotlin.browser.window

private val json = Json(JsonConfiguration.Default.copy(allowStructuredMapKeys = true))
private const val MaxRetriesCount = 5
private const val RetryTimeoutSecs = 5

class ServerConnection(
    private val app: AppComponent,
    private val webSocket: WebSocket,
    private val gameId: GameId,
    scope: CoroutineScope
) {
    val requests: Channel<Request> = Channel<Request>(Channel.CONFLATED).also {
        window.onbeforeunload = {
            webSocket.send(json.stringify(Request.serializer(), LeaveGameRequest))
            webSocket.onclose = null
            webSocket.close()
            undefined
        }
    }

    private val sendRequestsJob: Job

    private val pingTimerHandle = window.setInterval({ webSocket.send(Request.Ping) }, 10000)

    init {

        sendRequestsJob = scope.launch {
            for (req in requests) {
                webSocket.send(json.stringify(Request.serializer(), req))
            }
        }

        webSocket.onmessage = { msg ->
            (msg.data as? String)?.takeUnless { it == Response.Pong }?.let { reqStr ->
                val message = json.parse(Response.serializer(), reqStr)
                app.processMessageFromServer(message, requests)
            }
        }

        webSocket.onclose = { e ->
            webSocket.apply {
                onopen = null
                onmessage = null
                onclose = null
            }
            sendRequestsJob.cancel()
            window.clearInterval(pingTimerHandle)
            val reason = e.asDynamic().reason as? String
            scope.launch { showCountdown(app, reason) }.invokeOnCompletion {
                app.me?.let { me -> scope.connectToServer(app, gameId, ConnectRequest.JoinGame(me.name), 0) }
            }
        }
    }

    private suspend fun showCountdown(app: AppComponent, reason: String?) {
        app.onReconnecting(reason, RetryTimeoutSecs)
        for (countdown in (RetryTimeoutSecs - 1 downTo 0)) {
            delay(1000)
            app.onReconnecting(reason, countdown)
        }
    }
}

interface AppComponent {
    val me: PlayerView?
    fun onConnected(connection: ServerConnection)
    fun onReconnecting(reason: String?, secsToReconnect: Int)
    fun cannotJoinGame(reason: ConnectResponse.Failure)
    fun processMessageFromServer(msg: Response, requests: SendChannel<Request>)
}

fun CoroutineScope.startGame(app: AppComponent, request: ConnectRequest.StartGame, retriesCount: Int) =
    connectToServer(app, GameId.random(), request, retriesCount)

fun CoroutineScope.joinGame(app: AppComponent, gameId: GameId, playerName: PlayerName) =
    connectToServer(app, gameId, ConnectRequest.JoinGame(playerName), 0)

// This method establishes WebSocket connection with server for the current game.
// The procedure consists of the following steps:
// * initiate WebSocket connection by creating an instance of the WebSocket class
// * wire up event handlers for open, message and close events of this class
// * wait for the open event signalling that connection has been successfully established
// * send first request (either StartGame or JoinGame)
// * wait for ConnectResponse (either Connected or CannotJoinGame)
// * establish requests queue processing coroutine and timer for ping requests, then pack both into a ServerConnection instance
private fun CoroutineScope.connectToServer(app: AppComponent, gameId: GameId, connectRequest: ConnectRequest, retriesCount: Int) {

    val protocol = if (window.location.protocol == "https:") "wss:" else "ws:"
    val webSocket = WebSocket("$protocol//" + window.location.host + "/game/${gameId.value}/ws")

    webSocket.onopen = {
        webSocket.send(json.stringify(ConnectRequest.serializer(), connectRequest))
    }

    webSocket.onmessage = { msg ->
        (msg.data as? String)?.let { reqStr ->
            when (val message = json.parse(ConnectResponse.serializer(), reqStr)) {
                is ConnectResponse.Success -> {
                    webSocket.onopen = null
                    ServerConnection(app, webSocket, gameId, this).also {
                        app.onConnected(it)
                    }
                }
                is ConnectResponse.Failure -> {
                    webSocket.onclose = null
                    webSocket.close()
                    if (message is ConnectResponse.Failure.GameIdTaken && retriesCount < MaxRetriesCount)
                        connectToServer(app, GameId.random(), connectRequest, retriesCount + 1)
                    else
                        app.cannotJoinGame(message)
                }
            }
        }
    }

    webSocket.onclose = { e ->
        val reason = e.asDynamic().reason as? String
        webSocket.apply {
            onopen = null
            onmessage = null
            onclose = null
        }
        app.onReconnecting(reason, 0)
        if (retriesCount < MaxRetriesCount)
            connectToServer(app, gameId, connectRequest, retriesCount + 1)
        else
            app.cannotJoinGame(ConnectResponse.Failure.CannotConnect)
    }
}