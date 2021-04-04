package ticketToRide

enum class CardColor(val rgb: String) {
    RED("#FF0000"),
    GREEN("#00FF00"),
    BLUE("#0000FF"),
    BLACK("#000000"),
    WHITE("#FFFFFF"),
    YELLOW("#FFFF00"),
    ORANGE("#FF8800"),
    MAGENTO("#FF00FF");
}

fun toSegmentRgb(segmentColor: CardColor?) = segmentColor?.rgb ?: "#AAAAAA"