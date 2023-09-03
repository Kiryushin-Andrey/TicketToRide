package ticketToRide.composables.welcomeScreen

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import ticketToRide.PlayerColor
import ticketToRide.color

@Composable
fun PlayerColorSelector(
    value: PlayerColor?,
    availableColors: List<PlayerColor>,
    onValueChange: (PlayerColor) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Text("Your color", fontSize = 16.sp)
        availableColors.forEach {
            RadioButton(
                selected = value == it,
                onClick = { onValueChange(it) },
                colors = RadioButtonDefaults.colors(
                    selectedColor = it.color,
                    unselectedColor = Color(it.value + 0x99000000)
                )
            )
        }
    }
}