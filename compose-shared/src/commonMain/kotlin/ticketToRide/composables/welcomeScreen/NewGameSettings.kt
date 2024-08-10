package ticketToRide.composables.welcomeScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NewGameSettings(vm: NewGameVM) {
    Column {
        OutlinedTextField(
            value = vm.carsNumber.toString(),
            onValueChange = { it: String ->
                it.toIntOrNull()?.takeIf { it in 4..99 }?.let { vm.carsNumber = it }
            },
            singleLine = true,
            label = { Text("Initial number of cars on hand") }
        )
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { vm.calculateScoreInProgress = !vm.calculateScoreInProgress }
        ) {
            Checkbox(
                checked = vm.calculateScoreInProgress,
                onCheckedChange = { vm.calculateScoreInProgress = it },
                )
            Text("Calculate scores during the game")
        }
    }
}
