@file:JsModule("pigeon-maps")
@file:JsNonModule
package pigeonMaps

import org.w3c.dom.events.MouseEvent
import react.*

external interface MoveEvent {
    var timestamp: Number
    var coords: Array<Double>? /* JsTuple<Number, Number> */
        get() = definedExternally
        set(value) = definedExternally
}

external interface MapClickEventArgs {
    var event: MouseEvent
    var latLng: Array<Double>? /* JsTuple<Number, Number> */
        get() = definedExternally
        set(value) = definedExternally
    var pixel: Array<Double>? /* JsTuple<Number, Number> */
        get() = definedExternally
        set(value) = definedExternally
}

external interface BoundsChangedEventArgs {
    var center: Array<Double>? /* JsTuple<Number, Number> */
        get() = definedExternally
        set(value) = definedExternally
    var bounds: Bounds
    var zoom: Number
    var initial: Boolean
}

external interface MapProps : RProps {
    var center: Array<Double>? /* JsTuple<Number, Number> */
        get() = definedExternally
        set(value) = definedExternally
    var defaultCenter: Array<Double>? /* JsTuple<Number, Number> */
        get() = definedExternally
        set(value) = definedExternally
    var zoom: Number?
        get() = definedExternally
        set(value) = definedExternally
    var defaultZoom: Number?
        get() = definedExternally
        set(value) = definedExternally
    var width: Number?
        get() = definedExternally
        set(value) = definedExternally
    var defaultWidth: Number?
        get() = definedExternally
        set(value) = definedExternally
    var height: Number?
        get() = definedExternally
        set(value) = definedExternally
    var defaultHeight: Number?
        get() = definedExternally
        set(value) = definedExternally
    var provider: ((x: Number, y: Number, z: Number, dpr: Number) -> String)?
        get() = definedExternally
        set(value) = definedExternally
    var dprs: Array<Number>?
        get() = definedExternally
        set(value) = definedExternally
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var animate: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var animateMaxScreens: Number?
        get() = definedExternally
        set(value) = definedExternally
    var minZoom: Number?
        get() = definedExternally
        set(value) = definedExternally
    var maxZoom: Number?
        get() = definedExternally
        set(value) = definedExternally
    var metaWheelZoom: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var metaWheelZoomWarning: String?
        get() = definedExternally
        set(value) = definedExternally
    var twoFingerDrag: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var twoFingerDragWarning: String?
        get() = definedExternally
        set(value) = definedExternally
    var warningZIndex: Number?
        get() = definedExternally
        set(value) = definedExternally
    var attribution: dynamic /* JSX.Element? | Boolean? */
        get() = definedExternally
        set(value) = definedExternally
    var attributionPrefix: dynamic /* JSX.Element? | Boolean? */
        get() = definedExternally
        set(value) = definedExternally
    var zoomSnap: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var mouseEvents: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var touchEvents: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var onClick: ((args: MapClickEventArgs) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onBoundsChanged: ((args: BoundsChangedEventArgs) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onAnimationStart: (() -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onAnimationStop: (() -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var limitBounds: String? /* "center" | "edge" */
        get() = definedExternally
        set(value) = definedExternally
    var boxClassname: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface TileValues {
    var tileMinX: Number
    var tileMaxX: Number
    var tileMinY: Number
    var tileMaxY: Number
    var tileCenterX: Number
    var tileCenterY: Number
    var roundedZoom: Number
    var zoomDelta: Number
    var scaleWidth: Number
    var scaleHeight: Number
    var scale: Number
}

external interface MapState: RState {
    var zoom: Number
    var center: Array<Double>? /* JsTuple<Number, Number> */
        get() = definedExternally
        set(value) = definedExternally
    var width: Number
    var height: Number
    var zoomDelta: Number
    var pixelDelta: Array<Double>? /* JsTuple<Number, Number> */
        get() = definedExternally
        set(value) = definedExternally
    var oldTiles: Array<TileValues>
    var showWarning: Boolean
    var warningType: String? /* "fingers" | "wheel" */
        get() = definedExternally
        set(value) = definedExternally
}

@JsName("default")
external open class Map(props: MapProps) : Component<MapProps, MapState> {
    override fun render(): ReactElement?
}
