package ticketToRide

import io.ktor.http.cio.websocket.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.protobuf.ProtoBuf

interface Formatter {
    val id: String;
    suspend fun <T> send(webSocketSession: WebSocketSession, msg: T, serializer: SerializationStrategy<T>)
    fun <T> deserialize(msg: Frame, deserializer: DeserializationStrategy<T>): T
}

class JsonFormatter : Formatter {
    override val id = "JSON"

    override suspend fun <T> send(webSocketSession: WebSocketSession, msg: T, serializer: SerializationStrategy<T>) =
        webSocketSession.send(json.stringify(serializer, msg))

    override fun <T> deserialize(msg: Frame, deserializer: DeserializationStrategy<T>): T {
        val reqStr = (msg as Frame.Text).readText()
        return json.parse(deserializer, reqStr)
    }
}

class ProtobufFormatter : Formatter {
    override val id = "ProtoBuf"

    override suspend fun <T> send(webSocketSession: WebSocketSession, msg: T, serializer: SerializationStrategy<T>) =
        webSocketSession.send(ProtoBuf.dump(serializer, msg))

    override fun <T> deserialize(msg: Frame, deserializer: DeserializationStrategy<T>): T =
        ProtoBuf.load(deserializer, (msg as Frame.Binary).data)
}