package ticketToRide

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.protobuf.ProtoBuf
import redisClient.Redis
import java.net.Socket

class RedisStorage(val host: String, val port: Int, val password: String?)

private val protobuf = ProtoBuf(false)
private fun mapKey(id: GameId) = "${id.value}-map"
private const val expireTimeSec = 3600.toString()

private fun <T> dump(serializationStrategy: SerializationStrategy<T>, value: T) =
    protobuf.dump(serializationStrategy, value)

private fun <T> parse(serializationStrategy: DeserializationStrategy<T>, value: ByteArray) =
    protobuf.load(serializationStrategy, value)

fun RedisStorage.saveMap(id: GameId, map: GameMap) {
    exec { conn ->
        val mapBytes = dump(GameMap.serializer(), map)
        conn.call<Any?>("SET", mapKey(id), mapBytes, "EX", expireTimeSec)
    }
}

fun RedisStorage.saveGame(state: GameState) {
    exec { conn ->
        val gameStateBytes = dump(GameState.serializer(), state)
        conn.call<Any?>("SET", state.id.value, gameStateBytes, "EX", expireTimeSec)
        conn.call<Any?>("EXPIRE", mapKey(state.id), expireTimeSec)
    }
}

fun RedisStorage.loadGame(id: GameId) = exec { conn ->
    conn.call<ByteArray>("GET", id.value)?.let {
        val state = parse(GameState.serializer(), it)
        conn.call<ByteArray>("GET", mapKey(id))?.let {
            val map = parse(GameMap.serializer(), it)
            state to map
        }
    }
}

fun RedisStorage.hasGame(id: GameId) = exec { conn ->
    conn.call<Long>("EXISTS", id.value)?.let { it == 1L }
        ?: throw Error("Unexpected answer from Redis EXISTS command")
}

private fun <T> RedisStorage.exec(block: (Redis) -> T) = Socket(host, port).use { socket ->
    val conn = Redis(socket)
    if (password != null) {
        conn.call<Unit>("AUTH", password)
    }
    block(conn)
}