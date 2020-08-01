package ticketToRide

import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class GameId(val value: String)

fun GameId.Companion.random() =
    GameId(Random.nextBytes(5).joinToString("") { it.toUByte().toString(16).padStart(2, '0') })

val GameId.webSocketUrlForPlayers get() = "/game/$value/ws/play"
val GameId.webSocketUrlForObservers get() = "/game/$value/ws/observe"