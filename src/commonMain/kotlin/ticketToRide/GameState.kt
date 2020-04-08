package ticketToRide

import kotlinx.serialization.*

@Serializable class GameId(val value: String)
@Serializable class CityName(val value: String)
@Serializable class PlayerId(val value: String)
@Serializable class PlayerName(val value: String)
@Serializable class Car(val value: Color)

val Car.isLoko: Boolean
    get() = this.value == Color.NONE

@Serializable
data class Ticket(val from: CityName, val to: CityName, val points: Int)

@Serializable
data class TicketsChoice(val tickets: List<Ticket>, val shouldChooseOnNextTurn: Boolean)

@Serializable
data class Player(
    val id: PlayerId,
    val name: PlayerName,
    val color: Color,
    val away: Boolean = false,
    val cars: Map<Car, Int> = emptyMap(),
    val ticketsOnHand: List<Ticket> = emptyList(),
    val ticketsForChoice: TicketsChoice? = null
)

@Serializable
data class GameState(val players: List<Player>, val openCoaches: List<Car>)