package ticketToRide.composables.gameScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ticketToRide.*

@Composable
fun TicketsOnHand(gameState: GameStateView, modifier: Modifier = Modifier) {
    val ticketsChoiceVM = remember(gameState.myPendingTicketsChoice) {
        gameState.myPendingTicketsChoice?.let { TicketsChoiceVM(it) }
    }
    val fulfilledTickets = remember(gameState.me, gameState.myTicketsOnHand, gameState.players) {
        gameState.me.getFulfilledTickets(gameState.myTicketsOnHand, gameState.players)
    }

    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier.width(IntrinsicSize.Max).defaultMinSize(minWidth = 300.dp)
    ) {
        gameState.myTicketsOnHand.forEach { ticket ->
            TicketCard(ticket, isFulfilled = fulfilledTickets.contains(ticket))
        }

        ticketsChoiceVM?.let { choice ->
            choice.items.forEach { (ticket, keep) ->
                TicketCard(ticket, isFulfilled = false, keep = keep)
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (choice.isValid) {
                ExtendedFloatingActionButton(
                    onClick = {
                        sendToServer(ConfirmTicketsChoiceRequest(choice.ticketsToKeep))
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text("Take chosen tickets")
                }
            } else {
                TooltipArea("You need to keep at least ${choice.minCountToKeep} tickets") {
                    ExtendedFloatingActionButton(
                        onClick = {},
                        containerColor = Color.LightGray,
                        contentColor = Color.Gray
                    ) {
                        Text("Take chosen tickets")
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketCard(ticket: Ticket, isFulfilled: Boolean, keep: MutableState<Boolean>? = null, modifier: Modifier = Modifier) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isFulfilled) colorScheme.successContainer else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp).run {
            if (keep != null) clickable { keep.value = !keep.value } else this
        }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp).fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (keep != null) {
                    Checkbox(keep.value, onCheckedChange = { keep.value = it }, modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text("${ticket.from.value} - ${ticket.to.value}")
            }
            Spacer(modifier = Modifier.width(24.dp))
            Surface(
                shape = ShapeDefaults.ExtraSmall,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                shadowElevation = 2.dp,
                modifier = Modifier.size(32.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        ticket.points.toString(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

private class TicketsChoiceVM(choice: PendingTicketsChoice) {
    val items = choice.tickets.map { it to mutableStateOf(false) }
    val minCountToKeep = choice.minCountToKeep

    val ticketsToKeep get() =
        items.filter { it.second.value }.map { it.first }

    val isValid get() =
        items.filter { (_, keep) -> keep.value }.size >= minCountToKeep
}
