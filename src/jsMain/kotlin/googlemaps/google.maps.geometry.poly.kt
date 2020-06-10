@file:JsQualifier("google.maps.geometry.poly")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")
package google.maps.geometry.poly

import google.maps.*
import kotlin.js.*

external fun containsLocation(point: LatLng, polygon: Polygon): Boolean

external fun isLocationOnEdge(point: LatLng, poly: Polygon, tolerance: Number = definedExternally): Boolean

external fun isLocationOnEdge(point: LatLng, poly: Polyline, tolerance: Number = definedExternally): Boolean