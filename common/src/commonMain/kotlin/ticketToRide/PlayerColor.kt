package ticketToRide

enum class PlayerColor(val value: Int) {
    RED(0xFF0000),
    BLUE(0x0000FF),
    BLACK(0),
    ORANGE(0xFF8800),
    MAGENTO(0xFF00FF);

    val rgb = "#" + value.toString(16).padStart(6, '0')
}