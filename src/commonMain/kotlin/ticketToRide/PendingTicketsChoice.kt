package ticketToRide

import kotlinx.serialization.Serializable

@Serializable
data class PendingTicketsChoice(
    val tickets: List<Ticket> = emptyList(),
    val minCountToKeep: Int,
    val shouldChooseOnNextTurn: Boolean
)

enum class PendingTicketsChoiceState { None, TookInAdvance, Choosing }
