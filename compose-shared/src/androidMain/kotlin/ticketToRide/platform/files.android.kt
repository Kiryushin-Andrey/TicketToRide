package ticketToRide.platform

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

@Composable
actual fun FileUploadDialog(onMapUploaded: (UploadedGameMap) -> Unit, close: () -> Unit) {
    val contentResolver = LocalContext.current.contentResolver
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { result ->
        if (result != null) {
            contentResolver.openInputStream(result)?.use { inputStream ->
                onMapUploaded(
                    UploadedGameMap(
                        result.pathSegments.last(),
                        inputStream.readBytes().toString(StandardCharsets.UTF_8)
                    )
                )
            }
        }
        close()
    }

    LaunchedEffect(launcher) {
        launcher.launch(arrayOf("*/*"))
    }
}

@Composable
actual fun FileSaveDialog(mapName: String, content: suspend () -> String, close: () -> Unit) {
    val contentResolver = LocalContext.current.contentResolver
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument("*/*")) { uri ->
        scope.launch {
            if (uri != null) {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(content().toByteArray(Charsets.UTF_8))
                }
            }
            close()
        }
    }
    
    LaunchedEffect(launcher) {
        launcher.launch("${mapName}_map")
    }
}
