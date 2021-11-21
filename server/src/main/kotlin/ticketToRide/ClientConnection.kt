package ticketToRide

import kotlinx.serialization.SerializationStrategy

interface ClientConnection {
    suspend fun isConnected(): Boolean
    suspend fun <T> send(msg: T, serializer: SerializationStrategy<T>)
}

interface PlayerConnection : ClientConnection {
    val name: PlayerName
}

interface ObserverConnection : ClientConnection