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
        fun randomForPlayer(gameState: GameState? = null) =
            values()
                .filter { color -> color != Color.NONE && (gameState == null || !gameState.players.map { it.color }.contains(color)) }
                .toList().shuffled().first()

        fun randomForCar() = values().toList().shuffled().first()
    }
}