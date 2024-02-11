package ticketToRide.platform

import androidx.compose.runtime.Composable

@Composable
expect fun FileUploadDialog(onMapUploaded: (String) -> Unit, close: () -> Unit)

@Composable
expect fun FileSaveDialog(content: String, close: () -> Unit)
