package ticketToRide.components

import styled.StyledProps

fun StyledProps.withClasses(vararg pairs: Pair<String, String>) {
    val classes = object{}.asDynamic()
    pairs.forEach { (k, v) -> classes[k] = v }
    asDynamic().classes = classes
}