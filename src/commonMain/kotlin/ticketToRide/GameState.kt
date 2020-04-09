package ticketToRide

import kotlinx.serialization.*

@Serializable data class GameId(val value: String)
@Serializable data class CityName(val value: String)
@Serializable data class PlayerName(val value: String)
@Serializable data class Car(val value: Color)

val Car.isLoko: Boolean
    get() = this.value == Color.NONE

@Serializable
data class Ticket(val from: CityName, val to: CityName, val points: Int)

@Serializable
data class TicketsChoice(val tickets: List<Ticket>, val shouldChooseOnNextTurn: Boolean)

@Serializable
data class Player(
    val name: PlayerName,
    val color: Color,
    val away: Boolean = false,
    val cars: Map<Car, Int> = emptyMap(),
    val ticketsOnHand: List<Ticket> = emptyList(),
    val ticketsForChoice: TicketsChoice? = null
)

@Serializable
data class GameState(val players: List<Player>, val openCoaches: List<Car>)