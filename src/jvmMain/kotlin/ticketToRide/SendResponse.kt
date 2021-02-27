package ticketToRide

sealed class SendResponse {
    class ForAll(val resp: (to: PlayerName) -> Response) : SendResponse()
    class ForObservers(val resp: GameStateForObserver) : SendResponse()
    class ForPlayer(val to: PlayerName, val resp: Response) : SendResponse()
}