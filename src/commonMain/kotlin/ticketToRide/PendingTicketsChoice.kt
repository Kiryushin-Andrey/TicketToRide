package ticketToRide

import kotlinx.serialization.Serializable

@Serializable
data class PendingTicketsChoice(val tickets: List<Ticket>, val minCountToKeep: Int, val shouldChooseOnNextTurn: Boolean)

fun PendingTicketsChoice?.toState() = when {
    this == null -> PendingTicketsChoiceState.None
    this.shouldChooseOnNextTurn -> PendingTicketsChoiceState.Choosing
    else -> PendingTicketsChoiceState.TookInAdvance
}

enum class PendingTicketsChoiceState { None, TookInAdvance, Choosing }
