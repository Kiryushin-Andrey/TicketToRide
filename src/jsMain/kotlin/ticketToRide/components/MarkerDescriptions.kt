package ticketToRide.components

data class Size(val width: Int, val height: Int)
data class Origin(val x: Float, val y: Float)

object MarkerDescription {
    val size = Size(30, 32)
    val origin = Origin(24f / 61, 63f / 65)
    val imageUrl = "icons/map_icon_flag_orange.svg"
}