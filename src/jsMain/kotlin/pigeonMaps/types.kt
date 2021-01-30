@file:JsModule("pigeon-maps")
@file:JsNonModule
package pigeonMaps

import react.*

external interface Bounds {
    var ne: Array<Double> /* JsTuple<Number, Number> */
    var sw: Array<Double> /* JsTuple<Number, Number> */
}

external interface PigeonProps: RProps {
    var anchor: Array<Double>? /* JsTuple<Number, Number> */
        get() = definedExternally
        set(value) = definedExternally
    var offset: Array<Double>? /* JsTuple<Number, Number> */
        get() = definedExternally
        set(value) = definedExternally
    var left: Number?
        get() = definedExternally
        set(value) = definedExternally
    var top: Number?
        get() = definedExternally
        set(value) = definedExternally
    abstract var latLngToPixel: ((latLng: Array<Double> /* JsTuple<Number, Number> */) -> Array<Double>)
    abstract var pixelToLatLng: ((pixel: Array<Double> /* JsTuple<Number, Number> */) -> Array<Double>)
}