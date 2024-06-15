package ticketToRide

import kotlinx.serialization.Serializable

@Serializable
sealed class ConnectRequest {

    @Serializable
    class Start(
        val playerName: PlayerName,
        val playerColor: PlayerColor,
        val map: StartGameMap,
        val carsCount: Int,
        val calculateScoresInProcess: Boolean
    ) : ConnectRequest()

    @Serializable
    sealed class StartGameMap {

        @Serializable
        class BuiltIn(val path: List<String>) : StartGameMap()

        @Serializable
        class Custom(val filename: String, val map: GameMap) : StartGameMap()
    }

    @Serializable
    class Join(val playerName: PlayerName, val playerColor: PlayerColor) : ConnectRequest()

    @Serializable
    class Reconnect(val playerName: PlayerName) : ConnectRequest()

    @Serializable
    object Observe : ConnectRequest()
}