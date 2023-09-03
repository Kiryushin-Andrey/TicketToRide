package ticketToRide

import androidx.compose.runtime.Composable

@Composable
fun MainView(gameId: String?) {
    App(BuildKonfig.SERVER_HOST, gameId?.let { GameId(it) }, WindowSizeClass.Large)
}
