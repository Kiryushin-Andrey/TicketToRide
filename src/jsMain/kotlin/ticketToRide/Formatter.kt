package ticketToRide

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.protobuf.ProtoBuf
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket

interface Formatter {
    fun <T> send(webSocket: WebSocket, value: T, serializer: SerializationStrategy<T>)
    fun <T> deserialize(msg: MessageEvent, serializer: DeserializationStrategy<T>): T
}

class JsonFormatter : Formatter {
    override fun <T> send(webSocket: WebSocket, value: T, serializer: SerializationStrategy<T>) {
        webSocket.send(json.stringify(serializer, value))
    }

    override fun <T> deserialize(msg: MessageEvent, serializer: DeserializationStrategy<T>): T {
        if (msg.data as? String == null)
            throw Error("Unexpected response from server: ${msg.data}")

        return json.parse(serializer, msg.data as String)
    }
}

class ProtobufFormatter : Formatter {
    private val protobuf = ProtoBuf(false)

    override fun <T> send(webSocket: WebSocket, value: T, serializer: SerializationStrategy<T>) {
        webSocket.send(Uint8Array(protobuf.dump(serializer, value).toTypedArray()))
    }

    override fun <T> deserialize(msg: MessageEvent, serializer: DeserializationStrategy<T>): T {
        val bytes = Int8Array(msg.data as ArrayBuffer).unsafeCast<ByteArray>()
        return protobuf.load(serializer, bytes)
    }
}

val formatterByType = mapOf(
    WireFormat.JSON to JsonFormatter(),
    WireFormat.PROTOBUF to ProtobufFormatter()
)