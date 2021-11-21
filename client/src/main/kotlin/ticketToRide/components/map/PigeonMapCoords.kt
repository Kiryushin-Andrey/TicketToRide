package ticketToRide.components.map

import ticketToRide.LatLong

typealias PigeonMapCoords = Array<Double>

val PigeonMapCoords.x: Double get() = this[0]
val PigeonMapCoords.y: Double get() = this[1]

fun LatLong.toPigeonMapCoords(): PigeonMapCoords = arrayOf(lat, lng)