package ticketToRide.localization

import ticketToRide.Locale
import ticketToRide.LocalizedStrings

class AppStrings(locale: Locale) : LocalizedStrings({ locale }) {

    val disconnected by locWithParam<Pair<String?, Int>>(
        Locale.En to { (reason, secsToReconnect) ->
            val additionalInfo = reason?.takeIf { it.isNotBlank() }?.let { ": $it" } ?: ""
            "Disconnected from server$additionalInfo. Trying to reconnect in $secsToReconnect seconds..."
        },
        Locale.Ru to { (reason, secsToReconnect) ->
            val additionalInfo = reason?.takeIf { it.isNotBlank() }?.let { ": $it" } ?: ""
            "Потеряно соединение$additionalInfo. Попытка переподключения через $secsToReconnect секунд..."
        }
    )

    val reconnecting by loc(
        Locale.En to "Trying to reconnect...",
        Locale.Ru to "Устанавливаю соединение..."
    )

    val yourTurn by loc(
        Locale.En to "It's your turn to make a move!",
        Locale.Ru to "Ваш ход!"
    )

    val gameIdTaken by loc(
        Locale.En to "Could not start game (could not generate unique game id)",
        Locale.Ru to "Не удалось запустить игру (сгенерировать уникальный id)"
    )

    val noSuchGame by loc(
        Locale.En to "Game with this id does not exist",
        Locale.Ru to "Игра по ссылке не найдена"
    )

    val playerNameTaken by loc(
        Locale.En to "This name is taken by another player",
        Locale.Ru to "Имя уже занято другим игроком"
    )

    val playerColorTaken by loc(
        Locale.En to "This color is taken by another player",
        Locale.Ru to "Цвет занят другим игроком"
    )

    val cannotConnect by loc(
        Locale.En to "Cannot establish WebSocket connection with the server",
        Locale.Ru to "Не удалось установить WebSocket-соединение с сервером"
    )

    val retryConnect by loc(
        Locale.En to "Retry",
        Locale.Ru to "Попытаться снова"
    )
}