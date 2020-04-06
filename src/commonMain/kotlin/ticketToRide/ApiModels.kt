package ticketToRide

enum class GameState { Welcome, StartingGame, GameInProgress }

sealed class ApiRequest(val playerId: PlayerId)
class StartGame(playerId: PlayerId, val playerName: PlayerName, val color: Color, val targetPlayersCount: Int) : ApiRequest(playerId)
class JoinGame(playerId: PlayerId, val playerName: PlayerName, val color: Color) : ApiRequest(playerId)
class StartGameNow(playerId: PlayerId) : ApiRequest(playerId)
class WentAway(playerId: PlayerId) : ApiRequest(playerId)
class CameBack(playerId: PlayerId) : ApiRequest(playerId)