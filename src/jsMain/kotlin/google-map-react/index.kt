@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")
@file:JsModule("google-map-react")
package google.map.react

import org.w3c.dom.Element
import react.*

external interface MapTypeStyle {
    var elementType: String?
        get() = definedExternally
        set(value) = definedExternally
    var featureType: String?
        get() = definedExternally
        set(value) = definedExternally
    var stylers: Array<Any>
}

external interface ControlOptions {
    var position: Number
}

external interface MapOptions {
    var backgroundColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var clickableIcons: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var controlSize: Number?
        get() = definedExternally
        set(value) = definedExternally
    var disableDefaultUI: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var disableDoubleClickZoom: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var draggable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var draggableCursor: String?
        get() = definedExternally
        set(value) = definedExternally
    var draggingCursor: String?
        get() = definedExternally
        set(value) = definedExternally
    var fullscreenControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var fullscreenControlOptions: ControlOptions ?
        get() = definedExternally
        set(value) = definedExternally
    var gestureHandling: String?
        get() = definedExternally
        set(value) = definedExternally
    var heading: Number?
        get() = definedExternally
        set(value) = definedExternally
    var keyboardShortcuts: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var mapTypeControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var mapTypeControlOptions: Any?
        get() = definedExternally
        set(value) = definedExternally
    var mapTypeId: String?
        get() = definedExternally
        set(value) = definedExternally
    var minZoom: Number?
        get() = definedExternally
        set(value) = definedExternally
    var maxZoom: Number?
        get() = definedExternally
        set(value) = definedExternally
    var noClear: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var options: ((maps: Maps) -> Props)?
        get() = definedExternally
        set(value) = definedExternally
    var panControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var panControlOptions: ControlOptions ?
        get() = definedExternally
        set(value) = definedExternally
    var rotateControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var rotateControlOptions: ControlOptions ?
        get() = definedExternally
        set(value) = definedExternally
    var scaleControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var scaleControlOptions: Any?
        get() = definedExternally
        set(value) = definedExternally
    var scrollwheel: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var streetView: Any?
        get() = definedExternally
        set(value) = definedExternally
    var streetViewControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var streetViewControlOptions: ControlOptions ?
        get() = definedExternally
        set(value) = definedExternally
    var styles: Array<MapTypeStyle>?
        get() = definedExternally
        set(value) = definedExternally
    var tilt: Number?
        get() = definedExternally
        set(value) = definedExternally
    var zoomControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var zoomControlOptions: ControlOptions ?
        get() = definedExternally
        set(value) = definedExternally
    var minZoomOverride: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface Maps {
    var Animation: Any
    var ControlPosition: Any
    var MapTypeControlStyle: Any
    var MapTypeId: Any
    var NavigationControlStyle: Any
    var ScaleControlStyle: Any
    var StrokePosition: Any
    var SymbolPath: Any
    var ZoomControlStyle: Any
    var DirectionsStatus: Any
    var DirectionsTravelMode: Any
    var DirectionsUnitSystem: Any
    var DistanceMatrixStatus: Any
    var DistanceMatrixElementStatus: Any
    var ElevationStatus: Any
    var GeocoderLocationType: Any
    var GeocoderStatus: Any
    var KmlLayerStats: Any
    var MaxZoomStatus: Any
    var StreetViewStatus: Any
    var TransitMode: Any
    var TransitRoutePreference: Any
    var TravelMode: Any
    var UnitSystem: Any
}

external interface Bounds {
    var nw: Coords
    var ne: Coords
    var sw: Coords
    var se: Coords
}

external interface Point {
    var x: Number
    var y: Number
}

external interface Coords {
    var lat: Number
    var lng: Number
}

external interface Size {
    var width: Number
    var height: Number
}

external interface ClickEventValue : Point, Coords {
    var event: Any
}

external interface ChangeEventValue {
    var center: Coords
    var zoom: Number
    var bounds: Bounds
    var marginBounds: Bounds
    var size: Size
}

external interface MapsDict {
    var map: google.maps.Map<Element>
    var maps: Any
    var ref: Element?
        get() = definedExternally
        set(value) = definedExternally
}

external interface Props : react.RProps {
    var bootstrapURLKeys: dynamic /* `T$2` | `T$3` */
        get() = definedExternally
        set(value) = definedExternally
    var defaultCenter: Coords?
        get() = definedExternally
        set(value) = definedExternally
    var center: Coords?
        get() = definedExternally
        set(value) = definedExternally
    var defaultZoom: Number?
        get() = definedExternally
        set(value) = definedExternally
    var zoom: Number?
        get() = definedExternally
        set(value) = definedExternally
    var heatmapLibrary: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var hoverDistance: Number?
        get() = definedExternally
        set(value) = definedExternally
    var options: dynamic /* MapOptions | (maps: Maps) -> MapOptions */
        get() = definedExternally
        set(value) = definedExternally
    var margin: Array<Any>?
        get() = definedExternally
        set(value) = definedExternally
    var debounced: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var draggable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var layerTypes: Array<String>?
        get() = definedExternally
        set(value) = definedExternally
    val onClick: ((value: ClickEventValue) -> Any)?
        get() = definedExternally
    val onChange: ((value: ChangeEventValue) -> Any)?
        get() = definedExternally
    var resetBoundsOnResize: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    val onChildClick: ((hoverKey: Any, childProps: Any) -> Unit)?
        get() = definedExternally
    val onChildMouseEnter: ((hoverKey: Any, childProps: Any) -> Unit)?
        get() = definedExternally
    val onChildMouseLeave: ((hoverKey: Any, childProps: Any) -> Unit)?
        get() = definedExternally
    val onChildMouseDown: ((childKey: Any, childProps: Any, mouse: Any) -> Unit)?
        get() = definedExternally
    val onChildMouseUp: ((childKey: Any, childProps: Any, mouse: Any) -> Unit)?
        get() = definedExternally
    val onChildMouseMove: ((childKey: Any, childProps: Any, mouse: Any) -> Unit)?
        get() = definedExternally
    val onDrag: ((map: Any) -> Unit)?
        get() = definedExternally
    val onDragEnd: ((map: Any) -> Unit)?
        get() = definedExternally
    val onZoomAnimationStart: ((args: Any) -> Unit)?
        get() = definedExternally
    val onZoomAnimationEnd: ((args: Any) -> Unit)?
        get() = definedExternally
    val onMapTypeIdChange: ((args: Any) -> Unit)?
        get() = definedExternally
    val distanceToMouse: ((pt: Point, mousePos: Point, markerProps: Any?) -> Number)?
        get() = definedExternally
    var googleMapLoader: ((bootstrapURLKeys: Any) -> Any)?
        get() = definedExternally
        set(value) = definedExternally
    var onGoogleApiLoaded: ((maps: MapsDict) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    val onTilesLoaded: (() -> Unit)?
        get() = definedExternally
    var yesIWantToUseGoogleMapApiInternals: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var style: Any?
        get() = definedExternally
        set(value) = definedExternally
    var shouldUnregisterMapOnUnmount: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

@JsName("default")
external open class GoogleMapReact : Component<Props, RState> {
    override fun render(): ReactElement?
}
