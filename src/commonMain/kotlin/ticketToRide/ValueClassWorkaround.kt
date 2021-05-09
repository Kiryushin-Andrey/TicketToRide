package ticketToRide

// value classes aren't allowed in external declarations - https://youtrack.jetbrains.com/issue/KT-43224
// this interface is a workaround for using them in React props
@Suppress("UNCHECKED_CAST")
interface ValueClass<V, T> {
    val value: V
    val unboxed get() = this as T
}

interface IGameId : ValueClass<String, GameId>
interface IPlayerName : ValueClass<String, PlayerName>
interface ICityName : ValueClass<String, CityName>