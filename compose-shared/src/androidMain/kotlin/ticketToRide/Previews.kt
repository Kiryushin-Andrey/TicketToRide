package ticketToRide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import ticketToRide.screens.GameInProgressScreen

@Preview
@Composable
fun GameInProgressScreenCompactPreview() {
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
                Ticket(CityId(ST_PETERSBURG), CityId(ARKHANGELSK), 10),
                Ticket(CityId(EKATERINGBURG), CityId(VORONEZH), 15)
            )
        )
    }

    val cities = listOf(
        City(CityId(ST_PETERSBURG), mapOf(Locale.En to ST_PETERSBURG), LatLong(59.938732, 30.316229)),
        City(CityId(EKATERINGBURG), mapOf(Locale.En to EKATERINGBURG), LatLong(56.839104, 60.60825)),
        City(CityId(ARKHANGELSK), mapOf(Locale.En to ARKHANGELSK), LatLong(64.543022, 40.537121)),
        City(CityId(VORONEZH), mapOf(Locale.En to VORONEZH), LatLong(51.6605982, 39.2005858)),
    )

    val gameMap = GameMap(
        cities = cities,
        segments = cities.flatMap { from -> cities.map { to -> Segment(from.id, to.id, length = 3) } },
        mapCenter = LatLong(57.6012967, 40.4744424),
        mapZoom = 4
    )

    val screen = remember {
        Screen.GameInProgress(
            gameId = GameId("abc"),
            gameMap = gameMap,
            gameState = gameStateView,
            playerState = PlayerState.initial(gameMap, gameStateView)
        )
    }
    val appState = remember { AppStateVM("localhost") }

    GameInProgressScreen(screen, appState, WindowSizeClass.Compact)
}

private const val ST_PETERSBURG = "Санкт-Петербург"
private const val EKATERINGBURG = "Екатеринбург"
private const val ARKHANGELSK = "Архангельск"
private const val VORONEZH = "Воронеж"
