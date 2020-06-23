package ticketToRide

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import redisClient.Redis
import java.net.Socket

data class RedisCredentials(val host: String, val port: Int, val password: String?)

private val redisJson = Json(JsonConfiguration.Default.copy(allowStructuredMapKeys = true))
private fun mapKey(id: GameId) = "${id.value}-map"
private const val expireTimeSec = 3600.toString()

fun RedisCredentials.saveMap(id: GameId, map: GameMap) {
    exec { conn ->
        val mapJson = redisJson.stringify(GameMap.serializer(), map)
        conn.call<Any?>("SET", mapKey(id), mapJson, "EX", expireTimeSec)
    }
}

fun RedisCredentials.saveGame(state: GameState) {
    exec { conn ->
        val gameStateJson = redisJson.stringify(GameState.serializer(), state)
        conn.call<Any?>("SET", state.id.value, gameStateJson, "EX", expireTimeSec)
        conn.call<Any?>("EXPIRE", mapKey(state.id), expireTimeSec)
    }
}

fun RedisCredentials.loadGame(id: GameId) = exec { conn ->
    conn.call<ByteArray>("GET", id.value)?.let {
        val state = redisJson.parse(GameState.serializer(), String(it))
        conn.call<ByteArray>("GET", mapKey(id))?.let {
            val map = redisJson.parse(GameMap.serializer(), String(it))
            state to map
        }
    }
}

fun RedisCredentials.hasGame(id: GameId) = exec { conn ->
    conn.call<Long>("EXISTS", id.value)?.let { it == 1L }
        ?: throw Error("Unexpected answer from Redis EXISTS command")
}

private fun <T> RedisCredentials.exec(block: (Redis) -> T) = Socket(host, port).use { socket ->
    val conn = Redis(socket)
    if (password != null) {
        conn.call<Unit>("AUTH", password)
    }
    block(conn)
}