package ticketToRide.components

import react.*
import ticketToRide.GameStateView
import ticketToRide.Locale
import ticketToRide.playerState.PlayerState

external interface ComponentBaseProps : RProps {
    var locale: Locale
    var connected: Boolean
    var playerState: PlayerState
    var gameState: GameStateView
    var onAction: (PlayerState) -> Unit
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
abstract class ComponentBase<P, S> : RComponent<P, S>() where P : ComponentBaseProps, S : RState {

    val gameState get() = props.gameState
    val playerState get() = props.playerState

    val players get() = gameState.players
    val me get() = gameState.me
    val lastRound get() = gameState.lastRound
    val turn get() = gameState.turn
    val myTurn get() = gameState.myTurn
    val myCards get() = gameState.myCards
    val myTickets get() = gameState.myTicketsOnHand
    val openCards get() = gameState.openCards
    val canPickCards get() = myTurn && playerState !is PlayerState.ChoosingTickets

    protected fun act(block: PlayerState.() -> PlayerState) {
        if (props.connected) props.onAction(playerState.block())
    }
}

inline fun <reified T : ComponentBase<P, *>, P : ComponentBaseProps> RBuilder.componentBase(
    props: ComponentBaseProps,
    crossinline builder: P.() -> Unit = {}
) =
    child(T::class) {
        attrs {
            this.locale = props.locale
            this.connected = props.connected
            this.gameState = props.gameState
            this.playerState = props.playerState
            this.onAction = props.onAction
            builder()
        }
    }