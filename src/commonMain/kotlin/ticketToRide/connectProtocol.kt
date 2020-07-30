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

@Serializable
sealed class ConnectResponse {

    @Serializable
    object Success : ConnectResponse()

    @Serializable
    sealed class Failure : ConnectResponse() {

        @Serializable
        object NoSuchGame : Failure()

        @Serializable
        object NoSuchPlayer : Failure()

        @Serializable
        object GameIdTaken : Failure()

        @Serializable
        object PlayerNameTaken : Failure()

        @Serializable
        object PlayerColorTaken : Failure()

        @Serializable
        object CannotConnect : Failure()
    }
}