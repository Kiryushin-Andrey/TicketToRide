package ticketToRide

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import redisClient.Redis
import java.net.Socket

data class RedisCredentials(val host: String, val port: Int, val password: String?)

private val redisJson = Json(JsonConfiguration.Default.copy(allowStructuredMapKeys = true))

fun RedisCredentials.saveToRedis(state: GameState) {
    Socket(host, port).use { socket ->
        val conn = Redis(socket)
        if (password != null) {
            conn.call<Unit>("AUTH", password)
        }
        val gameStateJson = redisJson.stringify(GameState.serializer(), state)
        conn.call<Any?>("SET", state.id.value, gameStateJson, "EX", "3600")
    }
}

fun RedisCredentials.loadFromRedis(id: GameId) : GameState? {
    Socket(host, port).use { socket ->
        val conn = Redis(socket)
        if (password != null) {
            conn.call<Unit>("AUTH", password)
        }
        val data = conn.call<ByteArray>("GET", id.value)
        return if (data != null) redisJson.parse(GameState.serializer(), String(data)) else null
    }
}