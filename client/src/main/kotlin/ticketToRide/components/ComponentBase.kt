package ticketToRide.components

import react.Props
import ticketToRide.GameMap
import ticketToRide.GameStateView
import ticketToRide.Locale
import ticketToRide.playerState.PlayerState

external interface GameComponentProps : Props {
    var locale: Locale
    var connected: Boolean
    var playerState: PlayerState
    var gameMap: GameMap
    var gameState: GameStateView
    var act: (PlayerState.() -> PlayerState) -> Unit
}

val GameComponentProps.players get() = gameState.players
val GameComponentProps.me get() = gameState.me
val GameComponentProps.lastRound get() = gameState.lastRound
val GameComponentProps.turn get() = gameState.turn
val GameComponentProps.myTurn get() = gameState.myTurn
val GameComponentProps.myCards get() = gameState.myCards
val GameComponentProps.myTickets get() = gameState.myTicketsOnHand
val GameComponentProps.openCards get() = gameState.openCards
val GameComponentProps.canPickCards get() = myTurn && playerState !is PlayerState.ChoosingTickets

fun GameComponentProps.copyFrom(another: GameComponentProps) {
    this.locale = another.locale
    this.connected = another.connected
    this.playerState = another.playerState
    this.gameMap = another.gameMap
    this.gameState = another.gameState
    this.act = another.act
}