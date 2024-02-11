package ticketToRide.composables.welcomeScreen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.readTextAsState
import ticketToRide.*
import ticketToRide.platform.*

@Composable
fun NewGameSettings(vm: NewGameVM) {
    var saveFileDialogVisible by remember { mutableStateOf(false) }
    var uploadFileDialogVisible by remember { mutableStateOf(false) }
    val defaultMap by ticketToRide.common.MR.files.defaultMap.readTextAsState()

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
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { vm.customMap = null }
                ) {
                    RadioButton(
                        selected = vm.customMap == null,
                        onClick = { vm.customMap = null }
                    )
                    Text("Built-in map of Russia")
                }
//                if (LocalWindowSizeClass.current == WindowSizeClass.Large) {
                    Icon(
                        painter = painterResource(MR.images.download),
                        contentDescription = "Download",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable { saveFileDialogVisible = true }
                    )
//                }
            }

            TextButton(
                onClick = { uploadFileDialogVisible = true },
            ) {
                Icon(
                    painter = painterResource(MR.images.upload),
                    contentDescription = "Upload my map"
                )
                if (LocalWindowSizeClass.current == WindowSizeClass.Large) {
                    Text("Upload my map", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        Text(
            "You can create and upload your own game map. Download the built-in map file to see a sample",
            style = MaterialTheme.typography.labelSmall
        )
    }

    if (saveFileDialogVisible && defaultMap != null) {
        FileSaveDialog(defaultMap!!) {
            saveFileDialogVisible = false
        }
    }
    if (uploadFileDialogVisible) {
        FileUploadDialog(onMapUploaded = { content ->
            when (val result = GameMap.parse(content)) {
                is Try.Success ->
                    vm.customMap = result.value
                is Try.Error ->
                    vm.customMapParseErrors = result.errors
            }
        }) {
            uploadFileDialogVisible = false
        }
    }
}
