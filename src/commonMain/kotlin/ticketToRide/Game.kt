package ticketToRide

inline class CityName(val value: String)
inline class PlayerId(val value: String)
inline class PlayerName(val value: String)
inline class Car(val value: Color)

val Car.isLoko: Boolean
    get() = this.value == Color.NONE

data class WannaBePlayer(val id: PlayerId, val name: PlayerName, val color: Color)
data class Ticket(val from: CityName, val to: CityName, val points: Int)
data class TicketsChoice(val tickets: List<Ticket>, val shouldChooseOnNextTurn: Boolean)
data class Player(
    val id: PlayerId,
    val name: PlayerName,
    val color: Color,
    val away: Boolean = false,
    val cars: Map<Car, Int> = emptyMap(),
    val ticketsOnHand: List<Ticket> = emptyList(),
    val ticketsForChoice: TicketsChoice? = null
)

sealed class Game
object Welcome : Game()
data class StartingGame(val players: List<WannaBePlayer>, val targetCount: Int) : Game()
data class GameInProgress(val players: List<Player>, val openCoaches: List<Car>) : Game()