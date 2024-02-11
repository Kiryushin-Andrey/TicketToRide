package ticketToRide.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.awt.ComposeWindow
import java.awt.FileDialog
import java.nio.file.Files
import java.nio.file.Path

@Composable
actual fun FileUploadDialog(onMapUploaded: (String) -> Unit, close: () -> Unit) {
    LaunchedEffect(onMapUploaded, close) {
        FileDialog(ComposeWindow(), "Upload game map", FileDialog.LOAD).apply {
            isVisible = true
            if (file != null) {
                onMapUploaded(Files.readString(Path.of(directory, file)))
            }
            close()
        }
    }
}

@Composable
actual fun FileSaveDialog(content: String, close: () -> Unit) {
    LaunchedEffect(close) {
        FileDialog(ComposeWindow(), "Download game map", FileDialog.SAVE).apply {
            file = "default.map"
            isVisible = true
            if (file != null) {
                Files.writeString(Path.of(directory, file), content)
            }
            close()
        }
    }
}