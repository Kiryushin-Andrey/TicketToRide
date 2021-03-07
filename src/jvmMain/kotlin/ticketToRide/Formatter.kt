package ticketToRide

import io.ktor.http.cio.websocket.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.protobuf.ProtoBuf

interface Formatter {
    val type: WireFormat;
    suspend fun <T> send(webSocketSession: WebSocketSession, msg: T, serializer: SerializationStrategy<T>)
    fun <T> deserialize(msg: Frame, deserializer: DeserializationStrategy<T>): T
}

class JsonFormatter : Formatter {
    override val type = WireFormat.JSON

    override suspend fun <T> send(webSocketSession: WebSocketSession, msg: T, serializer: SerializationStrategy<T>) =
        webSocketSession.send(json.encodeToString(serializer, msg))

    override fun <T> deserialize(msg: Frame, deserializer: DeserializationStrategy<T>): T {
        val reqStr = (msg as Frame.Text).readText()
        return json.decodeFromString(deserializer, reqStr)
    }
}

class ProtobufFormatter : Formatter {
    private val protobuf = ProtoBuf { encodeDefaults = false }

    override val type = WireFormat.PROTOBUF

    override suspend fun <T> send(webSocketSession: WebSocketSession, msg: T, serializer: SerializationStrategy<T>) =
        webSocketSession.send(protobuf.encodeToByteArray(serializer, msg))

    override fun <T> deserialize(msg: Frame, deserializer: DeserializationStrategy<T>): T =
        protobuf.decodeFromByteArray(deserializer, (msg as Frame.Binary).data)
}