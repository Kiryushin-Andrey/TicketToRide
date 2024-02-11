package ticketToRide.composables.gameScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun TooltipArea(text: String, modifier: Modifier, content: @Composable () -> Unit) {
    androidx.compose.foundation.TooltipArea(
        delayMillis = 1000,
        modifier = modifier,
        tooltip = {
            Card(
                shape = ShapeDefaults.Small,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface),
                modifier = Modifier.zIndex(0f)
            ) {
                Text(
                    text,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 12.sp
                )
            }
        },
        content = content
    )
}
