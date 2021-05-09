package ticketToRide

import kotlinx.serialization.Serializable

@Serializable
sealed class ConnectRequest {

    @Serializable
    class Start(
        val playerName: PlayerName,
        val playerColor: PlayerColor,
        val map: GameMap,
        val carsCount: Int,
        val calculateScoresInProcess: Boolean
    ) : ConnectRequest()

    @Serializable
    class Join(val playerName: PlayerName, val playerColor: PlayerColor) : ConnectRequest()

    @Serializable
    class Reconnect(val playerName: PlayerName) : ConnectRequest()

    @Serializable
    object Observe : ConnectRequest()
}