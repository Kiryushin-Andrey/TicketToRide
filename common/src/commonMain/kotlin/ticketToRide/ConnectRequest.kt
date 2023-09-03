package ticketToRide

import kotlinx.serialization.Serializable

@Serializable
sealed class ConnectRequest {

    @Serializable
    class Start(
        override val playerName: PlayerName,
        val playerColor: PlayerColor,
        val map: StartGameMap,
        val carsCount: Int,
        val calculateScoresInProcess: Boolean
    ) : ConnectRequest(), AsPlayer

    @Serializable
    sealed class StartGameMap {

        @Serializable
        class BuiltIn(val path: List<String>) : StartGameMap()

        @Serializable
        class Custom(val filename: String, val map: GameMap) : StartGameMap()
    }

    @Serializable
    class Join(override val playerName: PlayerName, val playerColor: PlayerColor) : ConnectRequest(), AsPlayer

    interface AsPlayer {
        val playerName: PlayerName
    }

    @Serializable
    class Reconnect(val playerName: PlayerName) : ConnectRequest()

    @Serializable
    data object Observe : ConnectRequest()
}