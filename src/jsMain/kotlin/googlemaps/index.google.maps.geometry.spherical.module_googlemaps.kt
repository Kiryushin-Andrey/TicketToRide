@file:JsQualifier("google.maps.geometry.spherical")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")
package google.maps.geometry.spherical

import kotlin.js.*
import kotlin.js.Json
import org.khronos.webgl.*
import org.w3c.dom.*
import org.w3c.dom.events.*
import org.w3c.dom.parsing.*
import org.w3c.dom.svg.*
import org.w3c.dom.url.*
import org.w3c.fetch.*
import org.w3c.files.*
import org.w3c.notifications.*
import org.w3c.performance.*
import org.w3c.workers.*
import org.w3c.xhr.*
import google.maps.*

external fun computeArea(path: Array<LatLng>, radius: Number = definedExternally): Number

external fun computeArea(path: google.maps.MVCArray<LatLng>, radius: Number = definedExternally): Number

external fun computeDistanceBetween(from: LatLng, to: LatLng, radius: Number = definedExternally): Number

external fun computeHeading(from: LatLng, to: LatLng): Number

external fun computeLength(path: Array<LatLng>, radius: Number = definedExternally): Number

external fun computeLength(path: google.maps.MVCArray<LatLng>, radius: Number = definedExternally): Number

external fun computeOffset(from: LatLng, distance: Number, heading: Number, radius: Number = definedExternally): LatLng

external fun computeOffsetOrigin(to: LatLng, distance: Number, heading: Number, radius: Number = definedExternally): LatLng

external fun computeSignedArea(loop: Array<LatLng>, radius: Number = definedExternally): Number

external fun computeSignedArea(loop: google.maps.MVCArray<LatLng>, radius: Number = definedExternally): Number

external fun interpolate(from: LatLng, to: LatLng, fraction: Number): LatLng