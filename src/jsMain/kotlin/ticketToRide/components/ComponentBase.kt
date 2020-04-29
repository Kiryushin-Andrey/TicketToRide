package ticketToRide.components

import react.RComponent
import react.RProps
import react.RState
import ticketToRide.GameStateView
import ticketToRide.playerState.PlayerState

interface ComponentBaseProps : RProps {
    var playerState: PlayerState
    var gameState: GameStateView
    var onAction: (PlayerState) -> Unit
}

abstract class ComponentBase<P, S> : RComponent<P, S> where P : ComponentBaseProps, S : RState {

    constructor() : super()

    private val gameState get() = props.gameState
    open val playerState get() = props.playerState

    val players get() = gameState.players
    val me get() = gameState.me
    val lastRound get() = gameState.lastRound
    val turn get() = gameState.turn
    val myTurn get() = gameState.myTurn
    val myCards get() = gameState.myCards
    val myTickets get() = gameState.myTicketsOnHand
    val openCards get() = gameState.openCards
    val canPickCards get() = myTurn && !(playerState is PlayerState.ChoosingTickets)

    protected fun act(block: PlayerState.() -> PlayerState) =
        props.onAction(playerState.block())
}