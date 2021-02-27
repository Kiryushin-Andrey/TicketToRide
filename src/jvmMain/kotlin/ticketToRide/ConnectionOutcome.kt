package ticketToRide

sealed class ConnectionOutcome(val response: ConnectResponse) {
    class PlayerConnected(
        val game: Game,
        val connection: PlayerConnection,
        response: ConnectResponse.PlayerConnected
    ) : ConnectionOutcome(response)

    class ObserverConnected(
        val game: Game,
        val connection: ObserverConnection,
        response: ConnectResponse.ObserverConnected
    ) : ConnectionOutcome(response)

    class Failure(reason: ConnectResponse.Failure) : ConnectionOutcome(reason)
}