package ticketToRide.composables.gameScreen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun TooltipArea(
    text: String,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    content()
}