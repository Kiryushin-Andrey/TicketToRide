package core

import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.send
import kotlinx.serialization.SerializationStrategy

open class ListenerConnection<TResponse>(
    private val webSocket: WebSocketSession,
    private val responseSerializer: SerializationStrategy<TResponse>
) {
    suspend fun send(resp: TResponse) = webSocket.send(json.stringify(responseSerializer, resp))
    suspend fun ping() = webSocket.send(PingPong.Pong)
}