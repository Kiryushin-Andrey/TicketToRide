package ticketToRide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import dev.icerock.moko.resources.FileResource
import org.akir.ticketToRide.R
import ticketToRide.screens.GameInProgressScreen

@Preview
@Composable
fun GameInProgressScreenCompactPreview() {
    val defaultMap = FileResource(R.raw.defaultmap).readText(LocalContext.current)
    val map = remember { (GameMap.parse(defaultMap) as Try.Success).value }
    val gameStateView = remember {
        GameStateView(
            players = listOf(
                PlayerView(
                    name = PlayerName("Andrey"),
                    color = PlayerColor.RED,
                    points = 0,
                    carsLeft = 12,
                    stationsLeft = 3,
                    cardsOnHand = 5,
                    ticketsOnHand = 2,
                    away = false,
                    occupiedSegments = emptyList(),
                    placedStations = emptyList(),
                    pendingTicketsChoice = PendingTicketsChoiceState.None
                ),
                PlayerView(
                    name = PlayerName("Lev"),
                    color = PlayerColor.BLUE,
                    points = 0,
                    carsLeft = 12,
                    stationsLeft = 3,
                    cardsOnHand = 5,
                    ticketsOnHand = 2,
                    away = false,
                    occupiedSegments = emptyList(),
                    placedStations = emptyList(),
                    pendingTicketsChoice = PendingTicketsChoiceState.None
                ),
            ),
            openCards = listOf(
                Card.Loco, Card.Car(CardColor.RED), Card.Loco, Card.Car(CardColor.RED), Card.Car(CardColor.BLUE)
            ),
            turn = 0,
            lastRound = false,
            myName = PlayerName("Andrey"),
            myCards = listOf(Card.Loco, Card.Car(CardColor.RED), Card.Car(CardColor.ORANGE)),
            myTicketsOnHand = listOf(
                Ticket(CityId("Санкт-Петербург"), CityId("Архангельск"), 10),
                Ticket(CityId("Екатеринбург"), CityId("Воронеж"), 15)
            )
        )
    }
    val screen = remember {
        Screen.GameInProgress(GameId("abc"), gameStateView, PlayerState.initial(map, gameStateView))
    }
    val appState = remember { AppStateVM("localhost", defaultMap) }

    GameInProgressScreen(screen, appState, WindowSizeClass.Compact)
}
