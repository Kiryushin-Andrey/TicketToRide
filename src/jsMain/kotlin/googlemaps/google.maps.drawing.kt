@file:JsQualifier("google.maps.drawing")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")
package google.maps.drawing

import google.maps.*
import org.w3c.dom.Element

external open class DrawingManager(options: DrawingManagerOptions = definedExternally) : google.maps.MVCObject {
    open fun getDrawingMode(): OverlayType
    open fun getMap(): Map<Element>
    open fun setDrawingMode(drawingMode: OverlayType?)
    open fun setMap(map: Map<Element>?)
    open fun setOptions(options: DrawingManagerOptions)
}

external interface DrawingManagerOptions {
    var circleOptions: Any?
        get() = definedExternally
        set(value) = definedExternally
    var drawingControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var drawingControlOptions: DrawingControlOptions?
        get() = definedExternally
        set(value) = definedExternally
    var drawingMode: OverlayType?
        get() = definedExternally
        set(value) = definedExternally
    var map: Any?
        get() = definedExternally
        set(value) = definedExternally
    var markerOptions: Any?
        get() = definedExternally
        set(value) = definedExternally
    var polygonOptions: Any?
        get() = definedExternally
        set(value) = definedExternally
    var polylineOptions: Any?
        get() = definedExternally
        set(value) = definedExternally
    var rectangleOptions: Any?
        get() = definedExternally
        set(value) = definedExternally
}

external interface DrawingControlOptions {
    var drawingModes: Array<OverlayType>?
        get() = definedExternally
        set(value) = definedExternally
    var position: ControlPosition?
        get() = definedExternally
        set(value) = definedExternally
}

external interface OverlayCompleteEvent {
    var overlay: dynamic /* Marker | Polygon | Polyline | Rectangle | Circle */
        get() = definedExternally
        set(value) = definedExternally
    var type: OverlayType
}

external enum class OverlayType {
    CIRCLE /* = 'circle' */,
    MARKER /* = 'marker' */,
    POLYGON /* = 'polygon' */,
    POLYLINE /* = 'polyline' */,
    RECTANGLE /* = 'rectangle' */
}