package ticketToRide

import androidx.compose.runtime.Composable

@Composable
fun MainView(gameId: String?) {
    App(BuildKonfig.SERVER_HOST.replace("localhost", "10.0.2.2"), gameId?.let { GameId(it) }, WindowSizeClass.Compact)
}
