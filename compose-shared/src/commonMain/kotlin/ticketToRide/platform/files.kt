package ticketToRide.platform

import androidx.compose.runtime.Composable

data class UploadedGameMap(val filename: String, val content: String)

@Composable
expect fun FileUploadDialog(onMapUploaded: (UploadedGameMap) -> Unit, close: () -> Unit)

@Composable
expect fun FileSaveDialog(mapName: String, content: suspend () -> String, close: () -> Unit)
