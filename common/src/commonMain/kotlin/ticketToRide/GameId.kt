package ticketToRide

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.random.Random

@JvmInline
@Serializable
value class GameId(override val value: String): IGameId {
    override fun toString() = "Game-$value"
}

fun GameId.Companion.random() =
    GameId(Random.nextBytes(5).joinToString("") { it.toUByte().toString(16).padStart(2, '0') })

val GameId.webSocketUrl get() = "/game/$value/ws"