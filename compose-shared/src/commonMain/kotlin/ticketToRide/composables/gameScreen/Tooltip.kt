package ticketToRide.composables.gameScreen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun TooltipArea(text: String, modifier: Modifier = Modifier, content: @Composable () -> Unit)

