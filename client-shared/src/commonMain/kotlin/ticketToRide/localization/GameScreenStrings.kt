package ticketToRide.localization

import ticketToRide.Locale
import ticketToRide.LocalizedStrings

class GameScreenStrings(val locale: Locale) : LocalizedStrings({ locale }) {

    val yourTurn by loc(
        Locale.En to "Your turn",
        Locale.Ru to "Ваш ход"
    )

    val lastRound by loc(
        Locale.En to "Last round",
        Locale.Ru to "Последний круг"
    )

    val playerXmoves by locWithParam<String>(
        Locale.En to { name -> "$name moves" },
        Locale.Ru to { name -> "Ходит $name" }
    )
}