@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")
package google.maps

import google.maps.Data.Feature
import google.maps.Data.StyleOptions

typealias StylingFunction = (feature: Feature) -> StyleOptions

typealias MVCEventHandler<T, A> = (self: T, args: A) -> Unit

typealias MarkerShapePolyCoords = Array<Number>