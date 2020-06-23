package ticketToRide

import kotlinx.serialization.Serializable

@Serializable
sealed class ConnectRequest() {

    abstract val playerName: PlayerName

    @Serializable
    class StartGame(override val playerName: PlayerName, val map: GameMap, val carsCount: Int) : ConnectRequest()

    @Serializable
    class JoinGame(override val playerName: PlayerName) : ConnectRequest()
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
        object GameIdTaken : Failure()

        @Serializable
        object PlayerNameTaken : Failure()

        @Serializable
        object CannotConnect : Failure()
    }
}