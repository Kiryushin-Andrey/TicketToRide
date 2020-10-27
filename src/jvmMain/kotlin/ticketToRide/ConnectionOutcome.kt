package ticketToRide

sealed class ConnectionOutcome {
    class Success(val game: Game, val connection: ClientConnection) : ConnectionOutcome()
    class Failure(val reason: ConnectResponse.Failure) : ConnectionOutcome()
}