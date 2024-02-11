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
actual fun FileUploadDialog(onMapUploaded: (String) -> Unit, close: () -> Unit) {
    val contentResolver = LocalContext.current.contentResolver
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { result ->
        if (result != null) {
            val inputStream = contentResolver.openInputStream(result)
            if (inputStream != null) {
                try {
                    onMapUploaded(inputStream.readBytes().toString(StandardCharsets.UTF_8))
                } finally {
                    inputStream.close()
                }
            }
        }
        close()
    }

    LaunchedEffect(launcher) {
        launcher.launch(arrayOf("*/*"))
    }
}

@Composable
actual fun FileSaveDialog(content: String, close: () -> Unit) {
    val contentResolver = LocalContext.current.contentResolver
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument("*/*")) { uri ->
        scope.launch {
            if (uri != null) {
                val outputStream = contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    try {
                        outputStream.write(content.toByteArray(Charsets.UTF_8))
                    } finally {
                        outputStream.close()
                    }
                }
            }
            close()
        }
    }
    
    LaunchedEffect(launcher) {
        launcher.launch("default.map")
    }
}
