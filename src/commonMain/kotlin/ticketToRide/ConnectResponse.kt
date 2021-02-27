package ticketToRide

import kotlinx.serialization.Serializable

@Serializable
sealed class ConnectResponse {

    @Serializable
    class PlayerConnected(val id: GameId, val map: GameMap, val state: GameStateView) : ConnectResponse()

    @Serializable
    class ObserverConnected(val id: GameId, val map: GameMap, val state: GameStateForObserver) : ConnectResponse()

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