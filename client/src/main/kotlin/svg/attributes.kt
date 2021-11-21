package svg

import kotlinx.html.HTMLTag
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal inline fun <reified T> enumAttribute(name: String) where T : Enum<T> = object : ReadWriteProperty<HTMLTag, T?> {
    override operator fun getValue(thisRef: HTMLTag, property: KProperty<*>): T? {
        return thisRef.attributes[name]?.let { value ->
            enumValues<T>().firstOrNull { it.name == value }
        }
    }

    override fun setValue(thisRef: HTMLTag, property: KProperty<*>, value: T?) {
        value?.let { thisRef.attributes[name] = it.name } ?: thisRef.attributes.remove(name)
    }
}

internal fun stringAttribute(name: String) = object : ReadWriteProperty<HTMLTag, String?> {
    override operator fun getValue(thisRef: HTMLTag, property: KProperty<*>): String? {
        return thisRef.attributes[name]
    }

    override fun setValue(thisRef: HTMLTag, property: KProperty<*>, value: String?) {
        value?.let { thisRef.attributes[name] = it } ?: thisRef.attributes.remove(name)
    }
}

internal fun intAttribute(name: String) = object : ReadWriteProperty<HTMLTag, Int?> {
    override operator fun getValue(thisRef: HTMLTag, property: KProperty<*>): Int? {
        return thisRef.attributes[name]?.toIntOrNull()
    }

    override fun setValue(thisRef: HTMLTag, property: KProperty<*>, value: Int?) {
        thisRef.attributes[name] = value.toString()
    }
}
