package ticketToRide

enum class CardColor(val value: Int) {
    RED(0xFF0000),
    GREEN(0x00FF00),
    BLUE(0x0000FF),
    BLACK(0x000000),
    WHITE(0xFFFFFF),
    YELLOW(0xFFFF00),
    ORANGE(0xFF8800),
    MAGENTO(0xAA00FF);

    val rgb = "#" + value.toString(16).padStart(6, '0')
}

fun toSegmentRgb(segmentColor: CardColor?) = segmentColor?.rgb ?: "#AAAAAA"