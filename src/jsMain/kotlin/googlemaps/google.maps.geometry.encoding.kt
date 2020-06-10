@file:JsQualifier("google.maps.geometry.encoding")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")
package google.maps.geometry.encoding

import google.maps.LatLng
import google.maps.MVCArray

external fun decodePath(encodedPath: String): Array<LatLng>

external fun encodePath(path: Array<LatLng>): String

external fun encodePath(path: MVCArray<LatLng>): String