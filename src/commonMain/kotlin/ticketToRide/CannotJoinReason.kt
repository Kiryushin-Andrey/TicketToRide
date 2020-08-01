package ticketToRide

import kotlinx.serialization.Serializable

@Serializable
enum class CannotJoinReason { NoSuchGame, NoSuchPlayer, GameIdTaken, PlayerNameTaken, PlayerColorTaken }