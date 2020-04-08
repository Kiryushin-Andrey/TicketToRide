package ticketToRide

enum class Color(val rgb: String) {
    NONE("#AAAAAA"),
    RED("#FF0000"),
    GREEN("#00FF00"),
    BLUE("#0000FF"),
    BLACK("#000000"),
    WHITE("#FFFFFF"),
    YELLOW("#FFFF00"),
    ORANGE("#FF8800"),
    MAGENTO("#FF00FF");

    companion object {
        fun random(gameState: GameState? = null) =
            values()
                .filter { color -> gameState == null || gameState.players.map { it.color }.contains(color) }
                .toList().shuffled().first()
    }
}