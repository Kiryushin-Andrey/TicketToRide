package ticketToRide

import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class GameId(val value: String) {
    override fun toString() = "Game-$value"
}

fun GameId.Companion.random() =
    GameId(Random.nextBytes(5).joinToString("") { it.toUByte().toString(16).padStart(2, '0') })

val GameId.webSocketUrl get() = "/game/$value/ws"