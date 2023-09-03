package ticketToRide.composables.welcomeScreen

import androidx.compose.runtime.Composable

@Composable
expect fun ShowGameIdDialog(url: String, onStartGame: () -> Unit)
