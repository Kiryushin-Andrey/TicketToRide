package ticketToRide

import kotlin.properties.ReadOnlyProperty

abstract class LocalizedStrings(val getLocale: () -> Locale) {

    fun <T> loc(vararg items: Pair<Locale, T>) = with(mapOf(*items)) {
        ReadOnlyProperty<Any, T> { _, _ ->
            getLocale().let { locale ->
                get(locale) ?: get(Locale.En) ?: throw Error("Resource not found")
            }
        }
    }

    fun <T> locWithParam(vararg items: Pair<Locale, (T) -> String>) = loc(*items)
}