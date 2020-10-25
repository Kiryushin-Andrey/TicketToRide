package ticketToRide

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.protobuf.ProtoBuf
import org.khronos.webgl.Uint8Array
import org.w3c.dom.ARRAYBUFFER
import org.w3c.dom.BinaryType
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket

interface Formatter {
    fun prepare(webSocket: WebSocket) = Unit
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

class ProtoBufFormatter : Formatter {
    override fun prepare(webSocket: WebSocket) {
        webSocket.binaryType = BinaryType.ARRAYBUFFER
    }

    override fun <T> send(webSocket: WebSocket, value: T, serializer: SerializationStrategy<T>) {
        webSocket.send(ProtoBuf.dump(serializer, value) as Uint8Array)
    }

    override fun <T> deserialize(msg: MessageEvent, serializer: DeserializationStrategy<T>): T {
        return ProtoBuf.load(serializer, msg.data as ByteArray)
    }
}