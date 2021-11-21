package ticketToRide

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class LocalizedStrings(val getLocale: () -> Locale) {

    fun <T> loc(vararg items: Pair<Locale, T>) = with(mapOf(*items)) {
        object : ReadOnlyProperty<Any, T> {
            override operator fun getValue(thisRef: Any, property: KProperty<*>) = getLocale().let { locale ->
                get(locale) ?: get(Locale.En) ?: throw Error("Resource not found")
            }
        }
    }

    fun <T> locWithParam(vararg items: Pair<Locale, (T) -> String>) = loc(*items)
}