package ticketToRide.composables.welcomeScreen

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun ShowGameIdDialog(url: String, onStartGame: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(url) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url)
            putExtra(Intent.EXTRA_TITLE, "Join me for Ticket to Ride game!")
            type = "text/plain"
        }
        context.startActivity(intent)
        onStartGame()
    }
}
