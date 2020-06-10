@file:JsQualifier("google.maps")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")
package google.maps

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import kotlin.js.Date

external interface MapTypeStyler {
    var color: String?
        get() = definedExternally
        set(value) = definedExternally
    var gamma: Number?
        get() = definedExternally
        set(value) = definedExternally
    var hue: String?
        get() = definedExternally
        set(value) = definedExternally
    var invert_lightness: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var lightness: Number?
        get() = definedExternally
        set(value) = definedExternally
    var saturation: Number?
        get() = definedExternally
        set(value) = definedExternally
    var visibility: String?
        get() = definedExternally
        set(value) = definedExternally
    var weight: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface MapHandlerMap {
    var bounds_changed: dynamic /* JsTuple<> */
        get() = definedExternally
        set(value) = definedExternally
    var center_changed: dynamic /* JsTuple<> */
        get() = definedExternally
        set(value) = definedExternally
    var click: dynamic /* JsTuple<dynamic> */
        get() = definedExternally
        set(value) = definedExternally
    var dblclick: dynamic /* JsTuple<MouseEvent> */
        get() = definedExternally
        set(value) = definedExternally
    var drag: dynamic /* JsTuple<> */
        get() = definedExternally
        set(value) = definedExternally
    var dragend: dynamic /* JsTuple<> */
        get() = definedExternally
        set(value) = definedExternally
    var dragstart: dynamic /* JsTuple<> */
        get() = definedExternally
        set(value) = definedExternally
    var heading_changed: dynamic /* JsTuple<> */
        get() = definedExternally
        set(value) = definedExternally
    var idle: dynamic /* JsTuple<> */
        get() = definedExternally
        set(value) = definedExternally
    var maptypeid_changed: dynamic /* JsTuple<> */
        get() = definedExternally
        set(value) = definedExternally
    var mousemove: dynamic /* JsTuple<MouseEvent> */
        get() = definedExternally
        set(value) = definedExternally
    var mouseout: dynamic /* JsTuple<MouseEvent> */
        get() = definedExternally
        set(value) = definedExternally
    var mouseover: dynamic /* JsTuple<MouseEvent> */
        get() = definedExternally
        set(value) = definedExternally
    var projection_changed: dynamic /* JsTuple<> */
        get() = definedExternally
        set(value) = definedExternally
    var rightclick: dynamic /* JsTuple<MouseEvent> */
        get() = definedExternally
        set(value) = definedExternally
    var tilesloaded: dynamic /* JsTuple<> */
        get() = definedExternally
        set(value) = definedExternally
    var tilt_changed: dynamic /* JsTuple<> */
        get() = definedExternally
        set(value) = definedExternally
    var zoom_changed: dynamic /* JsTuple<> */
        get() = definedExternally
        set(value) = definedExternally
}

external open class Map<E : Element>(mapDiv: E, opts: MapOptions = definedExternally) : MVCObject {
    open fun <N : Any> addListener(eventName: N, handler: MVCEventHandler<Map<E> /* this */, Any>): MapsEventListener
    override fun addListener(eventName: String, handler: MVCEventHandler<MVCObject, Array<Any>>): MapsEventListener
    open fun fitBounds(bounds: LatLngBounds, padding: Number = definedExternally)
    open fun fitBounds(bounds: LatLngBounds, padding: Padding = definedExternally)
    open fun fitBounds(bounds: LatLngBoundsLiteral, padding: Number = definedExternally)
    open fun fitBounds(bounds: LatLngBoundsLiteral, padding: Padding = definedExternally)
    open fun getBounds(): LatLngBounds?
    open fun getCenter(): LatLng
    open fun getClickableIcons(): Boolean
    open fun getDiv(): E
    open fun getHeading(): Number
    open fun getMapTypeId(): MapTypeId
    open fun getProjection(): Projection?
    open fun getStreetView(): StreetViewPanorama
    open fun getTilt(): Number
    open fun getZoom(): Number
    open fun panBy(x: Number, y: Number)
    open fun panTo(latLng: LatLng)
    open fun panTo(latLng: LatLngLiteral)
    open fun panToBounds(latLngBounds: LatLngBounds, padding: Number = definedExternally)
    open fun panToBounds(latLngBounds: LatLngBounds, padding: Padding = definedExternally)
    open fun panToBounds(latLngBounds: LatLngBoundsLiteral, padding: Number = definedExternally)
    open fun panToBounds(latLngBounds: LatLngBoundsLiteral, padding: Padding = definedExternally)
    open fun setCenter(latlng: LatLng)
    open fun setCenter(latlng: LatLngLiteral)
    open fun setHeading(heading: Number)
    open fun setMapTypeId(mapTypeId: MapTypeId)
    open fun setMapTypeId(mapTypeId: String)
    open fun setOptions(options: MapOptions)
    open fun setStreetView(panorama: StreetViewPanorama?)
    open fun setTilt(tilt: Number)
    open fun setZoom(zoom: Number)
    open var controls: Array<MVCArray<Node>>
    open var data: Data
    open var mapTypes: MapTypeRegistry
    open var overlayMapTypes: MVCArray<MapType>
    open fun setClickableIcons(clickable: Boolean)
    open fun fitBounds(bounds: LatLngBounds)
    open fun fitBounds(bounds: LatLngBoundsLiteral)
    open fun panToBounds(latLngBounds: LatLngBounds)
    open fun panToBounds(latLngBounds: LatLngBoundsLiteral)
}

external interface MapOptions {
    var backgroundColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var center: dynamic /* LatLng? | LatLngLiteral? */
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
    var fullscreenControlOptions: FullscreenControlOptions?
        get() = definedExternally
        set(value) = definedExternally
    var gestureHandling: String? /* 'cooperative' | 'greedy' | 'none' | 'auto' */
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
    var mapTypeControlOptions: MapTypeControlOptions?
        get() = definedExternally
        set(value) = definedExternally
    var mapTypeId: dynamic /* MapTypeId? | String? */
        get() = definedExternally
        set(value) = definedExternally
    var maxZoom: Number?
        get() = definedExternally
        set(value) = definedExternally
    var minZoom: Number?
        get() = definedExternally
        set(value) = definedExternally
    var noClear: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var panControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var panControlOptions: PanControlOptions?
        get() = definedExternally
        set(value) = definedExternally
    var restriction: MapRestriction?
        get() = definedExternally
        set(value) = definedExternally
    var rotateControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var rotateControlOptions: RotateControlOptions?
        get() = definedExternally
        set(value) = definedExternally
    var scaleControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var scaleControlOptions: ScaleControlOptions?
        get() = definedExternally
        set(value) = definedExternally
    var scrollwheel: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var streetView: StreetViewPanorama?
        get() = definedExternally
        set(value) = definedExternally
    var streetViewControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var streetViewControlOptions: StreetViewControlOptions?
        get() = definedExternally
        set(value) = definedExternally
    var styles: Array<MapTypeStyle>?
        get() = definedExternally
        set(value) = definedExternally
    var tilt: Number?
        get() = definedExternally
        set(value) = definedExternally
    var zoom: Number?
        get() = definedExternally
        set(value) = definedExternally
    var zoomControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var zoomControlOptions: ZoomControlOptions?
        get() = definedExternally
        set(value) = definedExternally
}

external interface MapTypeStyle {
    var elementType: String? /* 'all' | 'geometry' | 'geometry.fill' | 'geometry.stroke' | 'labels' | 'labels.icon' | 'labels.text' | 'labels.text.fill' | 'labels.text.stroke' */
        get() = definedExternally
        set(value) = definedExternally
    var featureType: String? /* 'all' | 'administrative' | 'administrative.country' | 'administrative.land_parcel' | 'administrative.locality' | 'administrative.neighborhood' | 'administrative.province' | 'landscape' | 'landscape.man_made' | 'landscape.natural' | 'landscape.natural.landcover' | 'landscape.natural.terrain' | 'poi' | 'poi.attraction' | 'poi.business' | 'poi.government' | 'poi.medical' | 'poi.park' | 'poi.place_of_worship' | 'poi.school' | 'poi.sports_complex' | 'road' | 'road.arterial' | 'road.highway' | 'road.highway.controlled_access' | 'road.local' | 'transit' | 'transit.line' | 'transit.station' | 'transit.station.airport' | 'transit.station.bus' | 'transit.station.rail' | 'water' */
        get() = definedExternally
        set(value) = definedExternally
    var stylers: Array<MapTypeStyler>?
        get() = definedExternally
        set(value) = definedExternally
}

external interface MouseEvent {
    fun stop()
    var latLng: LatLng
}

external interface IconMouseEvent : MouseEvent {
    var placeId: String
}

external enum class MapTypeId {
    HYBRID /* = 'hybrid' */,
    ROADMAP /* = 'roadmap' */,
    SATELLITE /* = 'satellite' */,
    TERRAIN /* = 'terrain' */
}

external open class MapTypeRegistry : MVCObject {
    open fun set(id: String, mapType: MapType)
    override fun set(key: String, value: Any)
}

external interface MapRestriction {
    var latLngBounds: dynamic /* LatLngBounds | LatLngBoundsLiteral */
        get() = definedExternally
        set(value) = definedExternally
    var strictBounds: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external open class TrafficLayer(opts: TrafficLayerOptions = definedExternally) : MVCObject {
    open fun getMap(): Map<Element>
    open fun setMap(map: Map<Element>?)
    open fun setOptions(options: TrafficLayerOptions)
}

external interface TrafficLayerOptions {
    var autoRefresh: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var map: Map<Element>?
        get() = definedExternally
        set(value) = definedExternally
}

external open class TransitLayer : MVCObject {
    open fun getMap()
    open fun setMap(map: Map<Element>?)
}

external open class BicyclingLayer : MVCObject {
    open fun getMap(): Map<Element>
    open fun setMap(map: Map<Element>?)
}

external open class LatLng(lat: Number, lng: Number, noWrap: Boolean = definedExternally) {
    constructor(literal: LatLngLiteral, noWrap: Boolean)
    open fun equals(other: LatLng): Boolean
    open fun lat(): Number
    open fun lng(): Number
    override fun toString(): String
    open fun toUrlValue(precision: Number = definedExternally): String
    open fun toJSON(): LatLngLiteral
}

external interface LatLngLiteral {
    var lat: Number
    var lng: Number
}

external interface ReadonlyLatLngLiteral {
    var lat: Number
    var lng: Number
}

external open class LatLngBounds {
    constructor(sw: LatLng, ne: LatLng)
    constructor(sw: LatLng, ne: LatLngLiteral)
    constructor(sw: LatLngLiteral, ne: LatLng)
    constructor(sw: LatLngLiteral, ne: LatLngLiteral)
    open fun contains(latLng: LatLng): Boolean
    open fun contains(latLng: LatLngLiteral): Boolean
    open fun equals(other: LatLngBounds): Boolean
    open fun equals(other: LatLngBoundsLiteral): Boolean
    open fun extend(point: LatLng): LatLngBounds
    open fun extend(point: LatLngLiteral): LatLngBounds
    open fun getCenter(): LatLng
    open fun getNorthEast(): LatLng
    open fun getSouthWest(): LatLng
    open fun intersects(other: LatLngBounds): Boolean
    open fun intersects(other: LatLngBoundsLiteral): Boolean
    open fun isEmpty(): Boolean
    open fun toJSON(): LatLngBoundsLiteral
    open fun toSpan(): LatLng
    override fun toString(): String
    open fun toUrlValue(precision: Number = definedExternally): String
    open fun union(other: LatLngBounds): LatLngBounds
    open fun union(other: LatLngBoundsLiteral): LatLngBounds
}

external interface LatLngBoundsLiteral {
    var east: Number
    var north: Number
    var south: Number
    var west: Number
}

external open class Point(x: Number, y: Number) {
    open var x: Number
    open var y: Number
    open fun equals(other: Point): Boolean
    override fun toString(): String
}

external open class Size(width: Number, height: Number, widthUnit: String = definedExternally, heightUnit: String = definedExternally) {
    open var height: Number
    open var width: Number
    open fun equals(other: Size): Boolean
    override fun toString(): String
}

external interface Padding {
    var bottom: Number
    var left: Number
    var right: Number
    var top: Number
}

external interface CircleLiteral : CircleOptions {
    override var center: dynamic /* LatLng? | LatLngLiteral? */
        get() = definedExternally
        set(value) = definedExternally
    override var radius: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface MapsEventListener {
    fun remove()
}

external open class MVCObject {
    open fun addListener(eventName: String, handler: MVCEventHandler<MVCObject /* this */, Array<Any>>): MapsEventListener
    open fun bindTo(key: String, target: MVCObject, targetKey: String = definedExternally, noNotify: Boolean = definedExternally)
    open fun changed(key: String)
    open fun get(key: String): Any
    open fun notify(key: String)
    open fun set(key: String, value: Any)
    open fun setValues(values: Any)
    open fun unbind(key: String)
    open fun unbindAll()
}

external open class MVCArray<T>(array: Array<T> = definedExternally) : MVCObject {
    open fun clear()
    open fun forEach(callback: (elem: T, i: Number) -> Unit)
    open fun getArray(): Array<T>
    open fun getAt(i: Number): T
    open fun getLength(): Number
    open fun insertAt(i: Number, elem: T)
    open fun pop(): T
    open fun push(elem: T): Number
    open fun removeAt(i: Number): T
    open fun setAt(i: Number, elem: T)
}

external interface FullscreenControlOptions {
    var position: ControlPosition?
        get() = definedExternally
        set(value) = definedExternally
}

external interface MapTypeControlOptions {
    var mapTypeIds: Array<dynamic /* MapTypeId | String */>?
        get() = definedExternally
        set(value) = definedExternally
    var position: ControlPosition?
        get() = definedExternally
        set(value) = definedExternally
    var style: MapTypeControlStyle?
        get() = definedExternally
        set(value) = definedExternally
}

external enum class MapTypeControlStyle {
    DEFAULT /* = 0 */,
    HORIZONTAL_BAR /* = 1 */,
    DROPDOWN_MENU /* = 2 */,
    INSET /* = 3 */,
    INSET_LARGE /* = 4 */
}

external interface MotionTrackingControlOptions {
    var position: ControlPosition?
        get() = definedExternally
        set(value) = definedExternally
}

external interface PanControlOptions {
    var position: ControlPosition?
        get() = definedExternally
        set(value) = definedExternally
}

external interface RotateControlOptions {
    var position: ControlPosition?
        get() = definedExternally
        set(value) = definedExternally
}

external interface ScaleControlOptions {
    var style: ScaleControlStyle?
        get() = definedExternally
        set(value) = definedExternally
}

external enum class ScaleControlStyle {
    DEFAULT /* = 0 */
}

external interface StreetViewControlOptions {
    var position: ControlPosition?
        get() = definedExternally
        set(value) = definedExternally
}

external enum class ZoomControlStyle {
    DEFAULT /* = 0 */,
    SMALL /* = 1 */,
    LARGE /* = 2 */
}

external interface ZoomControlOptions {
    var position: ControlPosition?
        get() = definedExternally
        set(value) = definedExternally
    var style: ZoomControlStyle?
        get() = definedExternally
        set(value) = definedExternally
}

external enum class ControlPosition {
    BOTTOM_CENTER /* = 11 */,
    BOTTOM_LEFT /* = 10 */,
    BOTTOM_RIGHT /* = 12 */,
    LEFT_BOTTOM /* = 6 */,
    LEFT_CENTER /* = 4 */,
    LEFT_TOP /* = 5 */,
    RIGHT_BOTTOM /* = 9 */,
    RIGHT_CENTER /* = 8 */,
    RIGHT_TOP /* = 7 */,
    TOP_CENTER /* = 2 */,
    TOP_LEFT /* = 1 */,
    TOP_RIGHT /* = 3 */
}

external open class Marker(opts: ReadonlyMarkerOptions = definedExternally) : MVCObject {
    open fun getAnimation(): Animation?
    open fun getClickable(): Boolean
    open fun getCursor(): String?
    open fun getDraggable(): Boolean?
    open fun getIcon(): dynamic /* String? | ReadonlyIcon? | ReadonlySymbol? */
    open fun getLabel(): ReadonlyMarkerLabel?
    open fun getMap(): dynamic /* Map<Element>? | StreetViewPanorama? */
    open fun getOpacity(): Number?
    open fun getPosition(): LatLng?
    open fun getShape(): dynamic /* MarkerShapeCircle | MarkerShapeRect | MarkerShapePoly */
    open fun getTitle(): String?
    open fun getVisible(): Boolean
    open fun getZIndex(): Number?
    open fun setAnimation(animation: Animation?)
    open fun setClickable(flag: Boolean)
    open fun setCursor(cursor: String?)
    open fun setDraggable(flag: Boolean?)
    open fun setIcon(icon: String?)
    open fun setIcon(icon: ReadonlyIcon?)
    open fun setIcon(icon: ReadonlySymbol?)
    open fun setLabel(label: String?)
    open fun setLabel(label: ReadonlyMarkerLabel?)
    open fun setMap(map: Map<Element>?)
    open fun setMap(map: StreetViewPanorama?)
    open fun setOpacity(opacity: Number?)
    open fun setOptions(options: ReadonlyMarkerOptions)
    open fun setPosition(latlng: LatLng?)
    open fun setPosition(latlng: ReadonlyLatLngLiteral?)
    open fun setShape(shape: MarkerShapeCircle)
    open fun setShape(shape: MarkerShapeRect)
    open fun setShape(shape: MarkerShapePoly)
    open fun setTitle(title: String?)
    open fun setVisible(visible: Boolean)
    open fun setZIndex(zIndex: Number?)
    open fun addListener(eventName: String /* 'animation_changed' | 'clickable_changed' | 'cursor_changed' | 'draggable_changed' | 'flat_changed' | 'icon_changed' | 'position_changed' | 'shape_changed' | 'title_changed' | 'visible_changed' | 'zindex_changed' */, handler: (self: Marker) -> Unit): MapsEventListener
    open fun addListener(eventName: String /* 'click' | 'dblclick' | 'drag' | 'dragend' | 'dragstart' | 'mousedown' | 'mouseout' | 'mouseover' | 'mouseup' | 'rightclick' */, handler: (self: Marker, event: MouseEvent) -> Unit): MapsEventListener
    open fun addListener(eventName: String, handler: (self: Marker, args: Array<Any>) -> Unit): MapsEventListener

    companion object {
        var MAX_ZINDEX: Number
    }
}

external interface MarkerOptions {
    var anchorPoint: Point?
        get() = definedExternally
        set(value) = definedExternally
    var animation: Animation?
        get() = definedExternally
        set(value) = definedExternally
    var clickable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var crossOnDrag: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var cursor: String?
        get() = definedExternally
        set(value) = definedExternally
    var draggable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var icon: dynamic /* String? | Icon? | google.maps.Symbol? */
        get() = definedExternally
        set(value) = definedExternally
    var label: dynamic /* String? | MarkerLabel? */
        get() = definedExternally
        set(value) = definedExternally
    var map: dynamic /* Map<Element>? | StreetViewPanorama? */
        get() = definedExternally
        set(value) = definedExternally
    var opacity: Number?
        get() = definedExternally
        set(value) = definedExternally
    var optimized: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var position: dynamic /* LatLng? | LatLngLiteral? */
        get() = definedExternally
        set(value) = definedExternally
    var shape: dynamic /* MarkerShapeCircle | MarkerShapeRect | MarkerShapePoly */
        get() = definedExternally
        set(value) = definedExternally
    var title: String?
        get() = definedExternally
        set(value) = definedExternally
    var visible: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var zIndex: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface ReadonlyMarkerOptions {
    var anchorPoint: Point?
        get() = definedExternally
        set(value) = definedExternally
    var animation: Animation?
        get() = definedExternally
        set(value) = definedExternally
    var clickable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var crossOnDrag: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var cursor: String?
        get() = definedExternally
        set(value) = definedExternally
    var draggable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var icon: dynamic /* String? | ReadonlyIcon? | ReadonlySymbol? */
        get() = definedExternally
        set(value) = definedExternally
    var label: dynamic /* String? | ReadonlyMarkerLabel? */
        get() = definedExternally
        set(value) = definedExternally
    var map: dynamic /* Map<Element>? | StreetViewPanorama? */
        get() = definedExternally
        set(value) = definedExternally
    var opacity: Number?
        get() = definedExternally
        set(value) = definedExternally
    var optimized: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var place: Place?
        get() = definedExternally
        set(value) = definedExternally
    var position: dynamic /* LatLng? | ReadonlyLatLngLiteral? */
        get() = definedExternally
        set(value) = definedExternally
    var shape: dynamic /* MarkerShapeCircle | MarkerShapeRect | MarkerShapePoly */
        get() = definedExternally
        set(value) = definedExternally
    var title: String?
        get() = definedExternally
        set(value) = definedExternally
    var visible: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var zIndex: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface Icon {
    var anchor: Point?
        get() = definedExternally
        set(value) = definedExternally
    var labelOrigin: Point?
        get() = definedExternally
        set(value) = definedExternally
    var origin: Point?
        get() = definedExternally
        set(value) = definedExternally
    var scaledSize: Size?
        get() = definedExternally
        set(value) = definedExternally
    var size: Size?
        get() = definedExternally
        set(value) = definedExternally
    var url: String
}

external interface ReadonlyIcon {
    var anchor: Point?
        get() = definedExternally
        set(value) = definedExternally
    var labelOrigin: Point?
        get() = definedExternally
        set(value) = definedExternally
    var origin: Point?
        get() = definedExternally
        set(value) = definedExternally
    var scaledSize: Size?
        get() = definedExternally
        set(value) = definedExternally
    var size: Size?
        get() = definedExternally
        set(value) = definedExternally
    var url: String
}

external interface MarkerLabel {
    var color: String?
        get() = definedExternally
        set(value) = definedExternally
    var fontFamily: String?
        get() = definedExternally
        set(value) = definedExternally
    var fontSize: String?
        get() = definedExternally
        set(value) = definedExternally
    var fontWeight: String?
        get() = definedExternally
        set(value) = definedExternally
    var text: String
}

external interface ReadonlyMarkerLabel {
    var color: String?
        get() = definedExternally
        set(value) = definedExternally
    var fontFamily: String?
        get() = definedExternally
        set(value) = definedExternally
    var fontSize: String?
        get() = definedExternally
        set(value) = definedExternally
    var fontWeight: String?
        get() = definedExternally
        set(value) = definedExternally
    var text: String
}

external interface MarkerShapeCircle {
    var type: String /* 'circle' */
    var coords: dynamic /* JsTuple<Number, Number, Number> */
        get() = definedExternally
        set(value) = definedExternally
}

external interface MarkerShapeRect {
    var type: String /* 'rect' */
    var coords: dynamic /* JsTuple<Number, Number, Number, Number> */
        get() = definedExternally
        set(value) = definedExternally
}

external interface MarkerShapePoly {
    var type: String /* 'poly' */
    var coords: MarkerShapePolyCoords
}

external interface Symbol {
    var anchor: Point?
        get() = definedExternally
        set(value) = definedExternally
    var fillColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var fillOpacity: Number?
        get() = definedExternally
        set(value) = definedExternally
    var labelOrigin: Point?
        get() = definedExternally
        set(value) = definedExternally
    var path: dynamic /* SymbolPath | String */
        get() = definedExternally
        set(value) = definedExternally
    var rotation: Number?
        get() = definedExternally
        set(value) = definedExternally
    var scale: Number?
        get() = definedExternally
        set(value) = definedExternally
    var strokeColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var strokeOpacity: Number?
        get() = definedExternally
        set(value) = definedExternally
    var strokeWeight: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface ReadonlySymbol {
    var anchor: Point?
        get() = definedExternally
        set(value) = definedExternally
    var fillColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var fillOpacity: Number?
        get() = definedExternally
        set(value) = definedExternally
    var labelOrigin: Point?
        get() = definedExternally
        set(value) = definedExternally
    var path: dynamic /* SymbolPath | String */
        get() = definedExternally
        set(value) = definedExternally
    var rotation: Number?
        get() = definedExternally
        set(value) = definedExternally
    var scale: Number?
        get() = definedExternally
        set(value) = definedExternally
    var strokeColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var strokeOpacity: Number?
        get() = definedExternally
        set(value) = definedExternally
    var strokeWeight: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external enum class SymbolPath {
    BACKWARD_CLOSED_ARROW /* = 3 */,
    BACKWARD_OPEN_ARROW /* = 4 */,
    CIRCLE /* = 0 */,
    FORWARD_CLOSED_ARROW /* = 1 */,
    FORWARD_OPEN_ARROW /* = 2 */
}

external enum class Animation {
    BOUNCE /* = 1 */,
    DROP /* = 2 */
}

external open class InfoWindow(opts: InfoWindowOptions = definedExternally) : MVCObject {
    open fun close()
    open fun getContent(): dynamic /* String | Element */
    open fun getPosition(): LatLng
    open fun getZIndex(): Number
    open fun open(map: Map<Element> = definedExternally, anchor: MVCObject = definedExternally)
    open fun open(map: StreetViewPanorama = definedExternally, anchor: MVCObject = definedExternally)
    open fun setContent(content: String)
    open fun setContent(content: Node)
    open fun setOptions(options: InfoWindowOptions)
    open fun setPosition(position: LatLng)
    open fun setPosition(position: LatLngLiteral)
    open fun setZIndex(zIndex: Number)
    open fun open()
}

external interface InfoWindowOptions {
    var content: dynamic /* String? | Node? */
        get() = definedExternally
        set(value) = definedExternally
    var disableAutoPan: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var maxWidth: Number?
        get() = definedExternally
        set(value) = definedExternally
    var pixelOffset: Size?
        get() = definedExternally
        set(value) = definedExternally
    var position: dynamic /* LatLng? | LatLngLiteral? */
        get() = definedExternally
        set(value) = definedExternally
    var zIndex: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external open class Polyline(opts: PolylineOptions = definedExternally) : MVCObject {
    open fun getDraggable(): Boolean
    open fun getEditable(): Boolean
    open fun getMap(): Map<Element>
    open fun getPath(): MVCArray<LatLng>
    open fun getVisible(): Boolean
    open fun setDraggable(draggable: Boolean)
    open fun setEditable(editable: Boolean)
    open fun setMap(map: Map<Element>?)
    open fun setOptions(options: PolylineOptions)
    open fun setPath(path: MVCArray<LatLng>)
    open fun setPath(path: Array<LatLng>)
    open fun setPath(path: Array<LatLngLiteral>)
    open fun setVisible(visible: Boolean)
}

external interface PolylineOptions {
    var clickable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var draggable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var editable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var geodesic: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var icons: Array<IconSequence>?
        get() = definedExternally
        set(value) = definedExternally
    var map: Map<Element>?
        get() = definedExternally
        set(value) = definedExternally
    var path: dynamic /* MVCArray<LatLng>? | Array<LatLng>? | Array<LatLngLiteral>? */
        get() = definedExternally
        set(value) = definedExternally
    var strokeColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var strokeOpacity: Number?
        get() = definedExternally
        set(value) = definedExternally
    var strokeWeight: Number?
        get() = definedExternally
        set(value) = definedExternally
    var visible: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var zIndex: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface IconSequence {
    var fixedRotation: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var icon: Symbol?
        get() = definedExternally
        set(value) = definedExternally
    var offset: String?
        get() = definedExternally
        set(value) = definedExternally
    var repeat: String?
        get() = definedExternally
        set(value) = definedExternally
}

external open class Polygon(opts: PolygonOptions = definedExternally) : MVCObject {
    open fun getDraggable(): Boolean
    open fun getEditable(): Boolean
    open fun getMap(): Map<Element>
    open fun getPath(): MVCArray<LatLng>
    open fun getPaths(): MVCArray<MVCArray<LatLng>>
    open fun getVisible(): Boolean
    open fun setDraggable(draggable: Boolean)
    open fun setEditable(editable: Boolean)
    open fun setMap(map: Map<Element>?)
    open fun setOptions(options: PolygonOptions)
    open fun setPath(path: MVCArray<LatLng>)
    open fun setPath(path: Array<LatLng>)
    open fun setPath(path: Array<LatLngLiteral>)
    open fun setPaths(paths: MVCArray<MVCArray<LatLng>>)
    open fun setPaths(paths: MVCArray<LatLng>)
    open fun setPaths(paths: Array<Array<LatLng>>)
    open fun setPaths(paths: Array<Array<LatLngLiteral>>)
    open fun setPaths(paths: Array<LatLng>)
    open fun setPaths(paths: Array<LatLngLiteral>)
    open fun setVisible(visible: Boolean)
}

external interface PolygonOptions {
    var clickable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var draggable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var editable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var fillColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var fillOpacity: Number?
        get() = definedExternally
        set(value) = definedExternally
    var geodesic: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var map: Map<Element>?
        get() = definedExternally
        set(value) = definedExternally
    var paths: dynamic /* MVCArray<MVCArray<LatLng>>? | MVCArray<LatLng>? | Array<Array<LatLng>>? | Array<Array<LatLngLiteral>>? | Array<LatLng>? | Array<LatLngLiteral>? */
        get() = definedExternally
        set(value) = definedExternally
    var strokeColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var strokeOpacity: Number?
        get() = definedExternally
        set(value) = definedExternally
    var strokePosition: StrokePosition?
        get() = definedExternally
        set(value) = definedExternally
    var strokeWeight: Number?
        get() = definedExternally
        set(value) = definedExternally
    var visible: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var zIndex: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface PolyMouseEvent : MouseEvent {
    var edge: Number?
        get() = definedExternally
        set(value) = definedExternally
    var path: Number?
        get() = definedExternally
        set(value) = definedExternally
    var vertex: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external open class Rectangle(opts: RectangleOptions = definedExternally) : MVCObject {
    open fun getBounds(): LatLngBounds
    open fun getDraggable(): Boolean
    open fun getEditable(): Boolean
    open fun getMap(): Map<Element>
    open fun getVisible(): Boolean
    open fun setBounds(bounds: LatLngBounds)
    open fun setBounds(bounds: LatLngBoundsLiteral)
    open fun setDraggable(draggable: Boolean)
    open fun setEditable(editable: Boolean)
    open fun setMap(map: Map<Element>?)
    open fun setOptions(options: RectangleOptions)
    open fun setVisible(visible: Boolean)
}

external interface RectangleOptions {
    var bounds: dynamic /* LatLngBounds? | LatLngBoundsLiteral? */
        get() = definedExternally
        set(value) = definedExternally
    var clickable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var draggable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var editable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var fillColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var fillOpacity: Number?
        get() = definedExternally
        set(value) = definedExternally
    var map: Map<Element>?
        get() = definedExternally
        set(value) = definedExternally
    var strokeColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var strokeOpacity: Number?
        get() = definedExternally
        set(value) = definedExternally
    var strokePosition: StrokePosition?
        get() = definedExternally
        set(value) = definedExternally
    var strokeWeight: Number?
        get() = definedExternally
        set(value) = definedExternally
    var visible: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var zIndex: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external open class Circle(opts: CircleOptions = definedExternally) : MVCObject {
    open fun getBounds(): LatLngBounds
    open fun getCenter(): LatLng
    open fun getDraggable(): Boolean
    open fun getEditable(): Boolean
    open fun getMap(): Map<Element>
    open fun getRadius(): Number
    open fun getVisible(): Boolean
    open fun setCenter(center: LatLng)
    open fun setCenter(center: LatLngLiteral)
    open fun setDraggable(draggable: Boolean)
    open fun setEditable(editable: Boolean)
    open fun setMap(map: Map<Element>?)
    open fun setOptions(options: CircleOptions)
    open fun setRadius(radius: Number)
    open fun setVisible(visible: Boolean)
}

external interface CircleOptions {
    var center: dynamic /* LatLng? | LatLngLiteral? */
        get() = definedExternally
        set(value) = definedExternally
    var clickable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var draggable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var editable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var fillColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var fillOpacity: Number?
        get() = definedExternally
        set(value) = definedExternally
    var map: Map<Element>?
        get() = definedExternally
        set(value) = definedExternally
    var radius: Number?
        get() = definedExternally
        set(value) = definedExternally
    var strokeColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var strokeOpacity: Number?
        get() = definedExternally
        set(value) = definedExternally
    var strokePosition: StrokePosition?
        get() = definedExternally
        set(value) = definedExternally
    var strokeWeight: Number?
        get() = definedExternally
        set(value) = definedExternally
    var visible: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var zIndex: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external enum class StrokePosition {
    CENTER /* = 0 */,
    INSIDE /* = 1 */,
    OUTSIDE /* = 2 */
}

external open class Data(options: Data.DataOptions = definedExternally) : MVCObject {
    open fun add(feature: Feature): Feature
    open fun add(feature: FeatureOptions): Feature
    open fun addGeoJson(geoJson: Any?, options: GeoJsonOptions = definedExternally): Array<Feature>
    open fun contains(feature: Feature): Boolean
    open fun forEach(callback: (feature: Feature) -> Unit)
    open fun getControlPosition(): ControlPosition
    open fun getControls(): Array<String /* 'Point' | 'LineString' | 'Polygon' */>
    open fun getDrawingMode(): String /* 'Point' | 'LineString' | 'Polygon' */
    open fun getFeatureById(id: Number): Feature
    open fun getFeatureById(id: String): Feature
    open fun getMap(): Map<Element>
    open fun getStyle(): dynamic /* Data.StylingFunction | Data.StyleOptions */
    open fun loadGeoJson(url: String, options: GeoJsonOptions = definedExternally, callback: (features: Array<Feature>) -> Unit = definedExternally)
    open fun overrideStyle(feature: Feature, style: StyleOptions)
    open fun remove(feature: Feature)
    open fun revertStyle(feature: Feature = definedExternally)
    open fun setControlPosition(controlPosition: ControlPosition)
    open fun setControls(controls: Array<String /* 'Point' | 'LineString' | 'Polygon' */>?)
    open fun setDrawingMode(drawingMode: String /* 'Point' | 'LineString' | 'Polygon' */)
    open fun setMap(map: Map<Element>?)
    open fun setStyle(style: StylingFunction)
    open fun setStyle(style: StyleOptions)
    open fun toGeoJson(callback: (feature: Any?) -> Unit)
    interface DataOptions {
        var controlPosition: ControlPosition?
            get() = definedExternally
            set(value) = definedExternally
        var controls: Array<String /* 'Point' | 'LineString' | 'Polygon' */>?
            get() = definedExternally
            set(value) = definedExternally
        var drawingMode: String? /* 'Point' | 'LineString' | 'Polygon' */
            get() = definedExternally
            set(value) = definedExternally
        var featureFactory: ((geometry: Geometry) -> Feature)?
            get() = definedExternally
            set(value) = definedExternally
        var map: Map<Element>?
            get() = definedExternally
            set(value) = definedExternally
        var style: dynamic /* StylingFunction? | StyleOptions? */
            get() = definedExternally
            set(value) = definedExternally
    }
    interface GeoJsonOptions {
        var idPropertyName: String?
            get() = definedExternally
            set(value) = definedExternally
    }
    interface StyleOptions {
        var clickable: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var cursor: String?
            get() = definedExternally
            set(value) = definedExternally
        var draggable: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var editable: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var fillColor: String?
            get() = definedExternally
            set(value) = definedExternally
        var fillOpacity: Number?
            get() = definedExternally
            set(value) = definedExternally
        var icon: dynamic /* String? | Icon? | google.maps.Symbol? */
            get() = definedExternally
            set(value) = definedExternally
        var shape: dynamic /* MarkerShapeCircle | MarkerShapeRect | MarkerShapePoly */
            get() = definedExternally
            set(value) = definedExternally
        var strokeColor: String?
            get() = definedExternally
            set(value) = definedExternally
        var strokeOpacity: Number?
            get() = definedExternally
            set(value) = definedExternally
        var strokeWeight: Number?
            get() = definedExternally
            set(value) = definedExternally
        var title: String?
            get() = definedExternally
            set(value) = definedExternally
        var visible: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var zIndex: Number?
            get() = definedExternally
            set(value) = definedExternally
    }
    open class Feature(options: FeatureOptions = definedExternally) {
        open fun forEachProperty(callback: (value: Any, name: String) -> Unit)
        open fun getGeometry(): Geometry
        open fun getId(): dynamic /* Number | String */
        open fun getProperty(name: String): Any
        open fun removeProperty(name: String)
        open fun setGeometry(newGeometry: Geometry)
        open fun setGeometry(newGeometry: LatLng)
        open fun setGeometry(newGeometry: LatLngLiteral)
        open fun setProperty(name: String, newValue: Any)
        open fun toGeoJson(callback: (feature: Any?) -> Unit)
    }
    interface FeatureOptions {
        var geometry: dynamic /* Geometry? | LatLng? | LatLngLiteral? */
            get() = definedExternally
            set(value) = definedExternally
        var id: dynamic /* Number? | String? */
            get() = definedExternally
            set(value) = definedExternally
        var properties: Any?
            get() = definedExternally
            set(value) = definedExternally
    }
    open class Geometry {
        open fun getType(): String
        open fun forEachLatLng(callback: (latLng: LatLng) -> Unit)
    }
    open class Point : Geometry {
        constructor(latLng: LatLng)
        constructor(latLng: LatLngLiteral)
        open fun get(): LatLng
    }
    open class MultiPoint(elements: Array<dynamic /* LatLng | LatLngLiteral */>) : Geometry {
        open fun getArray(): Array<LatLng>
        open fun getAt(n: Number): LatLng
        open fun getLength(): Number
    }
    open class LineString(elements: Array<dynamic /* LatLng | LatLngLiteral */>) : Geometry {
        open fun getArray(): Array<LatLng>
        open fun getAt(n: Number): LatLng
        open fun getLength(): Number
    }
    open class MultiLineString(elements: Array<dynamic /* LineString | Array<dynamic /* LatLng | LatLngLiteral */> */>) : Geometry {
        open fun getArray(): Array<LineString>
        open fun getAt(n: Number): LineString
        open fun getLength(): Number
    }
    open class LinearRing(elements: Array<dynamic /* LatLng | LatLngLiteral */>) : Geometry {
        open fun getArray(): Array<LatLng>
        open fun getAt(n: Number): LatLng
        open fun getLength(): Number
    }
    open class Polygon(elements: Array<dynamic /* LinearRing | Array<dynamic /* LatLng | LatLngLiteral */> */>) : Geometry {
        open fun getArray(): Array<LinearRing>
        open fun getAt(n: Number): LinearRing
        open fun getLength(): Number
    }
    open class MultiPolygon(elements: Array<dynamic /* Polygon | Array<dynamic /* LinearRing | Array<dynamic /* LatLng | LatLngLiteral */> */> */>) : Geometry {
        open fun getArray(): Array<Polygon>
        open fun getAt(n: Number): Polygon
        open fun getLength(): Number
    }
    open class GeometryCollection(elements: Array<dynamic /* Array<Geometry> | Array<LatLng> | LatLngLiteral */>) : Geometry {
        open fun getArray(): Array<Geometry>
        open fun getAt(n: Number): Geometry
        open fun getLength(): Number
    }
    interface AddFeatureEvent {
        var feature: Feature
    }
    interface RemoveFeatureEvent {
        var feature: Feature
    }
    interface SetGeometryEvent {
        var feature: Feature
        var newGeometry: Geometry
        var oldGeometry: Geometry
    }
    interface SetPropertyEvent {
        var feature: Feature
        var name: String
        var newValue: Any
        var oldValue: Any
    }
    interface RemovePropertyEvent {
        var feature: Feature
        var name: String
        var oldValue: Any
    }
}

external open class OverlayView : MVCObject {
    open fun draw()
    open fun getMap(): dynamic /* Map<Element> | StreetViewPanorama */
    open fun getPanes(): MapPanes
    open fun getProjection(): MapCanvasProjection
    open fun onAdd()
    open fun onRemove()
    open fun setMap(map: Map<Element>?)
    open fun setMap(map: StreetViewPanorama?)

    companion object {
        fun preventMapHitsAndGesturesFrom(element: Element)
        fun preventMapHitsFrom(element: Element)
    }
}

external interface MapPanes {
    var floatPane: Element
    var mapPane: Element
    var markerLayer: Element
    var overlayLayer: Element
    var overlayMouseTarget: Element
}

external open class MapCanvasProjection : MVCObject {
    open fun fromContainerPixelToLatLng(pixel: Point, nowrap: Boolean = definedExternally): LatLng
    open fun fromDivPixelToLatLng(pixel: Point, nowrap: Boolean = definedExternally): LatLng
    open fun fromLatLngToContainerPixel(latLng: LatLng): Point
    open fun fromLatLngToDivPixel(latLng: LatLng): Point
    open fun getWorldWidth(): Number
}

external open class KmlLayer(opts: KmlLayerOptions = definedExternally) : MVCObject {
    open fun getDefaultViewport(): LatLngBounds
    open fun getMap(): Map<Element>
    open fun getMetadata(): KmlLayerMetadata
    open fun getStatus(): KmlLayerStatus
    open fun getUrl(): String
    open fun getZIndex(): Number
    open fun setMap(map: Map<Element>?)
    open fun setUrl(url: String)
    open fun setZIndex(zIndex: Number)
    open fun setOptions(options: KmlLayerOptions)
}

external interface KmlLayerOptions {
    var clickable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var map: Map<Element>?
        get() = definedExternally
        set(value) = definedExternally
    var preserveViewport: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var screenOverlays: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var suppressInfoWindows: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var url: String?
        get() = definedExternally
        set(value) = definedExternally
    var zIndex: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface KmlLayerMetadata {
    var author: KmlAuthor
    var description: String
    var hasScreenOverlays: Boolean
    var name: String
    var snippet: String
}

external enum class KmlLayerStatus {
    DOCUMENT_NOT_FOUND /* = 'DOCUMENT_NOT_FOUND' */,
    DOCUMENT_TOO_LARGE /* = 'DOCUMENT_TOO_LARGE' */,
    FETCH_ERROR /* = 'FETCH_ERROR' */,
    INVALID_DOCUMENT /* = 'INVALID_DOCUMENT' */,
    INVALID_REQUEST /* = 'INVALID_REQUEST' */,
    LIMITS_EXCEEDED /* = 'LIMITS_EXCEEDED' */,
    OK /* = 'OK' */,
    TIMED_OUT /* = 'TIMED_OUT' */,
    UNKNOWN /* = 'UNKNOWN' */
}

external interface KmlMouseEvent {
    var featureData: KmlFeatureData
    var latLng: LatLng
    var pixelOffset: Size
}

external interface KmlFeatureData {
    var author: KmlAuthor
    var description: String
    var id: String
    var infoWindowHtml: String
    var name: String
    var snippet: String
}

external interface KmlAuthor {
    var email: String
    var name: String
    var uri: String
}

external interface MapType {
    fun getTile(tileCoord: Point, zoom: Number, ownerDocument: Document): Element
    fun releaseTile(tile: Element)
    var alt: String?
        get() = definedExternally
        set(value) = definedExternally
    var maxZoom: Number?
        get() = definedExternally
        set(value) = definedExternally
    var minZoom: Number?
        get() = definedExternally
        set(value) = definedExternally
    var name: String?
        get() = definedExternally
        set(value) = definedExternally
    var projection: Projection?
        get() = definedExternally
        set(value) = definedExternally
    var radius: Number?
        get() = definedExternally
        set(value) = definedExternally
    var tileSize: Size?
        get() = definedExternally
        set(value) = definedExternally
}

external interface Projection {
    fun fromLatLngToPoint(latLng: LatLng, point: Point = definedExternally): Point
    fun fromPointToLatLng(pixel: Point, noWrap: Boolean = definedExternally): LatLng
}

external open class ImageMapType(opts: ImageMapTypeOptions) : MVCObject, MapType {
    open fun getOpacity(): Number
    override fun getTile(tileCoord: Point, zoom: Number, ownerDocument: Document): Element
    override fun releaseTile(tile: Element)
    open fun setOpacity(opacity: Number)
}

external interface ImageMapTypeOptions {
    var alt: String?
        get() = definedExternally
        set(value) = definedExternally
    fun getTileUrl(tileCoord: Point, zoom: Number): String
    var maxZoom: Number?
        get() = definedExternally
        set(value) = definedExternally
    var minZoom: Number?
        get() = definedExternally
        set(value) = definedExternally
    var name: String?
        get() = definedExternally
        set(value) = definedExternally
    var opacity: Number?
        get() = definedExternally
        set(value) = definedExternally
    var tileSize: Size
}

external open class GroundOverlay : MVCObject {
    constructor(url: String, bounds: LatLngBounds, opts: GroundOverlayOptions)
    constructor(url: String, bounds: LatLngBoundsLiteral, opts: GroundOverlayOptions)
    open fun getBounds(): LatLngBounds
    open fun getMap(): Map<Element>
    open fun getOpacity(): Number
    open fun getUrl(): String
    open fun setMap(map: Map<Element>?)
    open fun setOpacity(opacity: Number)
}

external interface GroundOverlayOptions {
    var clickable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var map: Map<Element>?
        get() = definedExternally
        set(value) = definedExternally
    var opacity: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external open class StyledMapType(styles: Array<MapTypeStyle>, options: StyledMapTypeOptions = definedExternally) : MVCObject, MapType {
    override fun getTile(tileCoord: Point, zoom: Number, ownerDocument: Document): Element
    override fun releaseTile(tile: Element)
}

external interface StyledMapTypeOptions {
    var alt: String?
        get() = definedExternally
        set(value) = definedExternally
    var maxZoom: Number?
        get() = definedExternally
        set(value) = definedExternally
    var minZoom: Number?
        get() = definedExternally
        set(value) = definedExternally
    var name: String?
        get() = definedExternally
        set(value) = definedExternally
}

external open class MaxZoomService {
    open fun getMaxZoomAtLatLng(latlng: LatLng, callback: (result: MaxZoomResult) -> Unit)
    open fun getMaxZoomAtLatLng(latlng: LatLngLiteral, callback: (result: MaxZoomResult) -> Unit)
}

external interface MaxZoomResult {
    var status: MaxZoomStatus
    var zoom: Number
}

external enum class MaxZoomStatus {
    ERROR /* = 'ERROR' */,
    OK /* = 'OK' */
}

external open class StreetViewPanorama(container: Element, opts: StreetViewPanoramaOptions = definedExternally) : MVCObject {
    open var controls: Array<MVCArray<Node>>
    open fun getLinks(): Array<StreetViewLink>
    open fun getLocation(): StreetViewLocation
    open fun getMotionTracking(): Boolean
    open fun getPano(): String
    open fun getPhotographerPov(): StreetViewPov
    open fun getPosition(): LatLng
    open fun getPov(): StreetViewPov
    open fun getStatus(): StreetViewStatus
    open fun getVisible(): Boolean
    open fun getZoom(): Number
    open fun registerPanoProvider(provider: (input: String) -> StreetViewPanoramaData, opts: PanoProviderOptions = definedExternally)
    open fun setLinks(links: Array<StreetViewLink>)
    open fun setMotionTracking(motionTracking: Boolean)
    open fun setOptions(options: StreetViewPanoramaOptions)
    open fun setPano(pano: String)
    open fun setPosition(latLng: LatLng)
    open fun setPosition(latLng: LatLngLiteral)
    open fun setPov(pov: StreetViewPov)
    open fun setVisible(flag: Boolean)
    open fun setZoom(zoom: Number)
}

external interface StreetViewPanoramaOptions {
    var addressControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var addressControlOptions: StreetViewAddressControlOptions?
        get() = definedExternally
        set(value) = definedExternally
    var clickToGo: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var disableDefaultUI: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var disableDoubleClickZoom: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var enableCloseButton: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var fullscreenControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var fullscreenControlOptions: FullscreenControlOptions?
        get() = definedExternally
        set(value) = definedExternally
    var imageDateControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var linksControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var motionTracking: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var motionTrackingControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var motionTrackingControlOptions: MotionTrackingControlOptions?
        get() = definedExternally
        set(value) = definedExternally
    var mode: String? /* 'html4' | 'html5' | 'webgl' */
        get() = definedExternally
        set(value) = definedExternally
    var panControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var panControlOptions: PanControlOptions?
        get() = definedExternally
        set(value) = definedExternally
    var pano: String?
        get() = definedExternally
        set(value) = definedExternally
    var panoProvider: ((input: String) -> StreetViewPanoramaData)?
        get() = definedExternally
        set(value) = definedExternally
    var position: dynamic /* LatLng? | LatLngLiteral? */
        get() = definedExternally
        set(value) = definedExternally
    var pov: StreetViewPov?
        get() = definedExternally
        set(value) = definedExternally
    var scrollwheel: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var visible: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var zoom: Number?
        get() = definedExternally
        set(value) = definedExternally
    var zoomControl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var zoomControlOptions: ZoomControlOptions?
        get() = definedExternally
        set(value) = definedExternally
}

external interface StreetViewAddressControlOptions {
    var position: ControlPosition?
        get() = definedExternally
        set(value) = definedExternally
}

external interface PanoProviderOptions {
    var cors: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface StreetViewTileData {
    fun getTileUrl(pano: String, tileZoom: Number, tileX: Number, tileY: Number): String
    var centerHeading: Number?
        get() = definedExternally
        set(value) = definedExternally
    var tileSize: Size?
        get() = definedExternally
        set(value) = definedExternally
    var worldSize: Size?
        get() = definedExternally
        set(value) = definedExternally
}

external interface StreetViewPov {
    var heading: Number?
        get() = definedExternally
        set(value) = definedExternally
    var pitch: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external open class StreetViewCoverageLayer : MVCObject {
    open fun getMap(): Map<Element>
    open fun setMap(map: Map<Element>?)
}

external open class StreetViewService {
    open fun getPanorama(request: StreetViewLocationRequest, cb: (data: StreetViewPanoramaData?, status: StreetViewStatus) -> Unit)
    open fun getPanorama(request: StreetViewPanoRequest, cb: (data: StreetViewPanoramaData?, status: StreetViewStatus) -> Unit)
    open fun getPanoramaById(pano: String, callback: (streetViewPanoramaData: StreetViewPanoramaData, streetViewStatus: StreetViewStatus) -> Unit)
    open fun getPanoramaByLocation(latlng: LatLng, radius: Number, callback: (streetViewPanoramaData: StreetViewPanoramaData, streetViewStatus: StreetViewStatus) -> Unit)
    open fun getPanoramaByLocation(latlng: LatLngLiteral, radius: Number, callback: (streetViewPanoramaData: StreetViewPanoramaData, streetViewStatus: StreetViewStatus) -> Unit)
}

external enum class StreetViewStatus {
    OK /* = 'OK' */,
    UNKNOWN_ERROR /* = 'UNKNOWN_ERROR' */,
    ZERO_RESULTS /* = 'ZERO_RESULTS' */
}

external interface StreetViewLocationRequest {
    var location: dynamic /* LatLng | LatLngLiteral */
        get() = definedExternally
        set(value) = definedExternally
    var preference: StreetViewPreference?
        get() = definedExternally
        set(value) = definedExternally
    var radius: Number?
        get() = definedExternally
        set(value) = definedExternally
    var source: StreetViewSource?
        get() = definedExternally
        set(value) = definedExternally
}

external interface StreetViewPanoRequest {
    var pano: String
}

external interface StreetViewLocation {
    var description: String?
        get() = definedExternally
        set(value) = definedExternally
    var latLng: LatLng?
        get() = definedExternally
        set(value) = definedExternally
    var pano: String?
        get() = definedExternally
        set(value) = definedExternally
    var shortDescription: String?
        get() = definedExternally
        set(value) = definedExternally
}

external enum class StreetViewPreference {
    BEST /* = 'best' */,
    NEAREST /* = 'nearest' */
}

external enum class StreetViewSource {
    DEFAULT /* = 'default' */,
    OUTDOOR /* = 'outdoor' */
}

external interface StreetViewPanoramaData {
    var copyright: String?
        get() = definedExternally
        set(value) = definedExternally
    var imageDate: String?
        get() = definedExternally
        set(value) = definedExternally
    var links: Array<StreetViewLink>?
        get() = definedExternally
        set(value) = definedExternally
    var location: StreetViewLocation?
        get() = definedExternally
        set(value) = definedExternally
    var tiles: StreetViewTileData?
        get() = definedExternally
        set(value) = definedExternally
}

external interface StreetViewLink {
    var description: String?
        get() = definedExternally
        set(value) = definedExternally
    var heading: Number?
        get() = definedExternally
        set(value) = definedExternally
    var pano: String?
        get() = definedExternally
        set(value) = definedExternally
}

external open class Geocoder {
    open fun geocode(request: GeocoderRequest, callback: (results: Array<GeocoderResult>, status: GeocoderStatus) -> Unit)
}

external interface GeocoderRequest {
    var address: String?
        get() = definedExternally
        set(value) = definedExternally
    var bounds: dynamic /* LatLngBounds? | LatLngBoundsLiteral? */
        get() = definedExternally
        set(value) = definedExternally
    var componentRestrictions: GeocoderComponentRestrictions?
        get() = definedExternally
        set(value) = definedExternally
    var location: dynamic /* LatLng? | LatLngLiteral? */
        get() = definedExternally
        set(value) = definedExternally
    var placeId: String?
        get() = definedExternally
        set(value) = definedExternally
    var region: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface GeocoderComponentRestrictions {
    var administrativeArea: String?
        get() = definedExternally
        set(value) = definedExternally
    var country: dynamic /* String? | Array<String>? */
        get() = definedExternally
        set(value) = definedExternally
    var locality: String?
        get() = definedExternally
        set(value) = definedExternally
    var postalCode: String?
        get() = definedExternally
        set(value) = definedExternally
    var route: String?
        get() = definedExternally
        set(value) = definedExternally
}

external enum class GeocoderStatus {
    ERROR /* = 'ERROR' */,
    INVALID_REQUEST /* = 'INVALID_REQUEST' */,
    OK /* = 'OK' */,
    OVER_QUERY_LIMIT /* = 'OVER_QUERY_LIMIT' */,
    REQUEST_DENIED /* = 'REQUEST_DENIED' */,
    UNKNOWN_ERROR /* = 'UNKNOWN_ERROR' */,
    ZERO_RESULTS /* = 'ZERO_RESULTS' */
}

external interface GeocoderResult {
    var address_components: Array<GeocoderAddressComponent>
    var formatted_address: String
    var geometry: GeocoderGeometry
    var partial_match: Boolean
    var place_id: String
    var postcode_localities: Array<String>
    var types: Array<String>
}

external interface GeocoderAddressComponent {
    var long_name: String
    var short_name: String
    var types: Array<String>
}

external interface GeocoderGeometry {
    var bounds: LatLngBounds
    var location: LatLng
    var location_type: GeocoderLocationType
    var viewport: LatLngBounds
}

external enum class GeocoderLocationType {
    APPROXIMATE /* = 'APPROXIMATE' */,
    GEOMETRIC_CENTER /* = 'GEOMETRIC_CENTER' */,
    RANGE_INTERPOLATED /* = 'RANGE_INTERPOLATED' */,
    ROOFTOP /* = 'ROOFTOP' */
}

external open class DirectionsService {
    open fun route(request: DirectionsRequest, callback: (result: DirectionsResult, status: DirectionsStatus) -> Unit)
}

external interface DirectionsRequest {
    var avoidFerries: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var avoidHighways: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var avoidTolls: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var destination: dynamic /* String? | LatLng? | LatLngLiteral? | Place? */
        get() = definedExternally
        set(value) = definedExternally
    var durationInTraffic: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var drivingOptions: DrivingOptions?
        get() = definedExternally
        set(value) = definedExternally
    var optimizeWaypoints: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var origin: dynamic /* String? | LatLng? | LatLngLiteral? | Place? */
        get() = definedExternally
        set(value) = definedExternally
    var provideRouteAlternatives: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var region: String?
        get() = definedExternally
        set(value) = definedExternally
    var transitOptions: TransitOptions?
        get() = definedExternally
        set(value) = definedExternally
    var travelMode: TravelMode?
        get() = definedExternally
        set(value) = definedExternally
    var unitSystem: UnitSystem?
        get() = definedExternally
        set(value) = definedExternally
    var waypoints: Array<DirectionsWaypoint>?
        get() = definedExternally
        set(value) = definedExternally
}

external enum class DirectionsStatus {
    INVALID_REQUEST /* = 'INVALID_REQUEST' */,
    MAX_WAYPOINTS_EXCEEDED /* = 'MAX_WAYPOINTS_EXCEEDED' */,
    NOT_FOUND /* = 'NOT_FOUND' */,
    OK /* = 'OK' */,
    OVER_QUERY_LIMIT /* = 'OVER_QUERY_LIMIT' */,
    REQUEST_DENIED /* = 'REQUEST_DENIED' */,
    UNKNOWN_ERROR /* = 'UNKNOWN_ERROR' */,
    ZERO_RESULTS /* = 'ZERO_RESULTS' */
}

external interface DirectionsResult {
    var geocoded_waypoints: Array<DirectionsGeocodedWaypoint>
    var routes: Array<DirectionsRoute>
}

external open class DirectionsRenderer(opts: DirectionsRendererOptions = definedExternally) : MVCObject {
    open fun getDirections(): DirectionsResult
    open fun getMap(): Map<Element>
    open fun getPanel(): Element
    open fun getRouteIndex(): Number
    open fun setDirections(directions: DirectionsResult)
    open fun setMap(map: Map<Element>?)
    open fun setOptions(options: DirectionsRendererOptions)
    open fun setPanel(panel: Element)
    open fun setRouteIndex(routeIndex: Number)
}

external interface DirectionsRendererOptions {
    var directions: DirectionsResult?
        get() = definedExternally
        set(value) = definedExternally
    var draggable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var hideRouteList: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var infoWindow: InfoWindow?
        get() = definedExternally
        set(value) = definedExternally
    var map: Map<Element>?
        get() = definedExternally
        set(value) = definedExternally
    var markerOptions: MarkerOptions?
        get() = definedExternally
        set(value) = definedExternally
    var panel: Element?
        get() = definedExternally
        set(value) = definedExternally
    var polylineOptions: PolylineOptions?
        get() = definedExternally
        set(value) = definedExternally
    var preserveViewport: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var routeIndex: Number?
        get() = definedExternally
        set(value) = definedExternally
    var suppressBicyclingLayer: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var suppressInfoWindows: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var suppressMarkers: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var suppressPolylines: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface DirectionsWaypoint {
    var location: dynamic /* String? | LatLng? | Place? */
        get() = definedExternally
        set(value) = definedExternally
    var stopover: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface DirectionsGeocodedWaypoint {
    var partial_match: Boolean
    var place_id: String
    var types: Array<String>
}

external interface DirectionsRoute {
    var bounds: LatLngBounds
    var copyrights: String
    var fare: TransitFare
    var legs: Array<DirectionsLeg>
    var overview_path: Array<LatLng>
    var overview_polyline: String
    var warnings: Array<String>
    var waypoint_order: Array<Number>
}

external interface DirectionsLeg {
    var arrival_time: Time
    var departure_time: Time
    var distance: Distance
    var duration: Duration
    var duration_in_traffic: Duration
    var end_address: String
    var end_location: LatLng
    var start_address: String
    var start_location: LatLng
    var steps: Array<DirectionsStep>
    var via_waypoints: Array<LatLng>
}

external interface BaseDirectionsStep {
    var distance: Distance
    var duration: Duration
    var end_location: LatLng
    var instructions: String
    var path: Array<LatLng>
    var start_location: LatLng
    var transit: TransitDetails
    var travel_mode: TravelMode
}

external interface DirectionsStep : BaseDirectionsStep {
    var steps: Array<BaseDirectionsStep>
}

external interface Place {
    var location: dynamic /* LatLng? | LatLngLiteral? */
        get() = definedExternally
        set(value) = definedExternally
    var placeId: String?
        get() = definedExternally
        set(value) = definedExternally
    var query: String?
        get() = definedExternally
        set(value) = definedExternally
}

external enum class TravelMode {
    BICYCLING /* = 'BICYCLING' */,
    DRIVING /* = 'DRIVING' */,
    TRANSIT /* = 'TRANSIT' */,
    TWO_WHEELER /* = 'TWO_WHEELER' */,
    WALKING /* = 'WALKING' */
}

external interface DrivingOptions {
    var departureTime: Date
    var trafficModel: TrafficModel?
        get() = definedExternally
        set(value) = definedExternally
}

external enum class TrafficModel {
    BEST_GUESS /* = 'bestguess' */,
    OPTIMISTIC /* = 'optimistic' */,
    PESSIMISTIC /* = 'pessimistic' */
}

external interface TransitOptions {
    var arrivalTime: Date?
        get() = definedExternally
        set(value) = definedExternally
    var departureTime: Date?
        get() = definedExternally
        set(value) = definedExternally
    var modes: Array<TransitMode>?
        get() = definedExternally
        set(value) = definedExternally
    var routingPreference: TransitRoutePreference?
        get() = definedExternally
        set(value) = definedExternally
}

external enum class TransitMode {
    BUS /* = 'BUS' */,
    RAIL /* = 'RAIL' */,
    SUBWAY /* = 'SUBWAY' */,
    TRAIN /* = 'TRAIN' */,
    TRAM /* = 'TRAM' */
}

external enum class TransitRoutePreference {
    FEWER_TRANSFERS /* = 'FEWER_TRANSFERS' */,
    LESS_WALKING /* = 'LESS_WALKING' */
}

external interface TransitFare {
    var currency: String
    var value: Number
}

external interface TransitDetails {
    var arrival_stop: TransitStop
    var arrival_time: Time
    var departure_stop: TransitStop
    var departure_time: Time
    var headsign: String
    var headway: Number
    var line: TransitLine
    var num_stops: Number
}

external interface TransitStop {
    var location: LatLng
    var name: String
}

external interface TransitLine {
    var agencies: Array<TransitAgency>
    var color: String
    var icon: String
    var name: String
    var short_name: String
    var text_color: String
    var url: String
    var vehicle: TransitVehicle
}

external interface TransitAgency {
    var name: String
    var phone: String
    var url: String
}

external interface TransitVehicle {
    var icon: String
    var local_icon: String
    var name: String
    var type: VehicleType
}

external enum class VehicleType {
    BUS,
    CABLE_CAR,
    COMMUTER_TRAIN,
    FERRY,
    FUNICULAR,
    GONDOLA_LIFT,
    HEAVY_RAIL,
    HIGH_SPEED_TRAIN,
    INTERCITY_BUS,
    METRO_RAIL,
    MONORAIL,
    OTHER,
    RAIL,
    SHARE_TAXI,
    SUBWAY,
    TRAM,
    TROLLEYBUS
}

external enum class UnitSystem {
    METRIC /* = 0 */,
    IMPERIAL /* = 1 */
}

external interface Distance {
    var text: String
    var value: Number
}

external interface Duration {
    var text: String
    var value: Number
}

external interface Time {
    var text: String
    var time_zone: String
    var value: Date
}

external open class DistanceMatrixService {
    open fun getDistanceMatrix(request: DistanceMatrixRequest, callback: (response: DistanceMatrixResponse, status: DistanceMatrixStatus) -> Unit)
}

external interface DistanceMatrixRequest {
    var avoidFerries: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var avoidHighways: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var avoidTolls: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var destinations: dynamic /* Array<String>? | Array<LatLng>? | Array<LatLngLiteral>? | Array<Place>? */
        get() = definedExternally
        set(value) = definedExternally
    var drivingOptions: DrivingOptions?
        get() = definedExternally
        set(value) = definedExternally
    var durationInTraffic: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var origins: dynamic /* Array<String>? | Array<LatLng>? | Array<LatLngLiteral>? | Array<Place>? */
        get() = definedExternally
        set(value) = definedExternally
    var region: String?
        get() = definedExternally
        set(value) = definedExternally
    var transitOptions: TransitOptions?
        get() = definedExternally
        set(value) = definedExternally
    var travelMode: TravelMode?
        get() = definedExternally
        set(value) = definedExternally
    var unitSystem: UnitSystem?
        get() = definedExternally
        set(value) = definedExternally
}

external interface DistanceMatrixResponse {
    var destinationAddresses: Array<String>
    var originAddresses: Array<String>
    var rows: Array<DistanceMatrixResponseRow>
}

external interface DistanceMatrixResponseRow {
    var elements: Array<DistanceMatrixResponseElement>
}

external interface DistanceMatrixResponseElement {
    var distance: Distance
    var duration: Duration
    var duration_in_traffic: Duration
    var fare: TransitFare
    var status: DistanceMatrixElementStatus
}

external enum class DistanceMatrixStatus {
    INVALID_REQUEST /* = 'INVALID_REQUEST' */,
    MAX_DIMENSIONS_EXCEEDED /* = 'MAX_DIMENSIONS_EXCEEDED' */,
    MAX_ELEMENTS_EXCEEDED /* = 'MAX_ELEMENTS_EXCEEDED' */,
    OK /* = 'OK' */,
    OVER_QUERY_LIMIT /* = 'OVER_QUERY_LIMIT' */,
    REQUEST_DENIED /* = 'REQUEST_DENIED' */,
    UNKNOWN_ERROR /* = 'UNKNOWN_ERROR' */
}

external enum class DistanceMatrixElementStatus {
    NOT_FOUND /* = 'NOT_FOUND' */,
    OK /* = 'OK' */,
    ZERO_RESULTS /* = 'ZERO_RESULTS' */
}

external open class ElevationService {
    open fun getElevationAlongPath(request: PathElevationRequest, callback: (results: Array<ElevationResult>, status: ElevationStatus) -> Unit)
    open fun getElevationForLocations(request: LocationElevationRequest, callback: (results: Array<ElevationResult>, status: ElevationStatus) -> Unit)
}

external interface LocationElevationRequest {
    var locations: Array<LatLng>
}

external interface PathElevationRequest {
    var path: Array<LatLng>?
        get() = definedExternally
        set(value) = definedExternally
    var samples: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface ElevationResult {
    var elevation: Number
    var location: LatLng
    var resolution: Number
}

external enum class ElevationStatus {
    INVALID_REQUEST /* = 'INVALID_REQUEST' */,
    OK /* = 'OK' */,
    OVER_QUERY_LIMIT /* = 'OVER_QUERY_LIMIT' */,
    REQUEST_DENIED /* = 'REQUEST_DENIED' */,
    UNKNOWN_ERROR /* = 'UNKNOWN_ERROR' */
}

external var version: String

external interface Attribution {
    var iosDeepLinkId: String?
        get() = definedExternally
        set(value) = definedExternally
    var source: String?
        get() = definedExternally
        set(value) = definedExternally
    var webUrl: String?
        get() = definedExternally
        set(value) = definedExternally
}

external open class SaveWidget(container: Node, opts: SaveWidgetOptions = definedExternally) {
    open fun getAttribution(): Attribution
    open fun getPlace(): Place
    open fun setAttribution(attribution: Attribution)
    open fun setOptions(opts: SaveWidgetOptions)
    open fun setPlace(place: Place)
}

external interface SaveWidgetOptions {
    var attribution: Attribution?
        get() = definedExternally
        set(value) = definedExternally
    var place: Place?
        get() = definedExternally
        set(value) = definedExternally
}

external open class FusionTablesLayer(options: FusionTablesLayerOptions) : MVCObject {
    open fun getMap(): Map<Element>
    open fun setMap(map: Map<Element>?)
    open fun setOptions(options: FusionTablesLayerOptions)
}

external interface FusionTablesLayerOptions {
    var clickable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var heatmap: FusionTablesHeatmap?
        get() = definedExternally
        set(value) = definedExternally
    var map: Map<Element>?
        get() = definedExternally
        set(value) = definedExternally
    var query: FusionTablesQuery?
        get() = definedExternally
        set(value) = definedExternally
    var styles: Array<FusionTablesStyle>?
        get() = definedExternally
        set(value) = definedExternally
    var suppressInfoWindows: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface FusionTablesQuery {
    var from: String?
        get() = definedExternally
        set(value) = definedExternally
    var limit: Number?
        get() = definedExternally
        set(value) = definedExternally
    var offset: Number?
        get() = definedExternally
        set(value) = definedExternally
    var orderBy: String?
        get() = definedExternally
        set(value) = definedExternally
    var select: String?
        get() = definedExternally
        set(value) = definedExternally
    var where: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface FusionTablesStyle {
    var markerOptions: FusionTablesMarkerOptions?
        get() = definedExternally
        set(value) = definedExternally
    var polygonOptions: FusionTablesPolygonOptions?
        get() = definedExternally
        set(value) = definedExternally
    var polylineOptions: FusionTablesPolylineOptions?
        get() = definedExternally
        set(value) = definedExternally
    var where: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface FusionTablesHeatmap {
    var enabled: Boolean
}

external interface FusionTablesMarkerOptions {
    var iconName: String
}

external interface FusionTablesPolygonOptions {
    var fillColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var fillOpacity: Number?
        get() = definedExternally
        set(value) = definedExternally
    var strokeColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var strokeOpacity: Number?
        get() = definedExternally
        set(value) = definedExternally
    var strokeWeight: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface FusionTablesPolylineOptions {
    var strokeColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var strokeOpacity: Number?
        get() = definedExternally
        set(value) = definedExternally
    var strokeWeight: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface FusionTablesMouseEvent {
    var infoWindowHtml: String?
        get() = definedExternally
        set(value) = definedExternally
    var latLng: LatLng?
        get() = definedExternally
        set(value) = definedExternally
    var pixelOffset: Size?
        get() = definedExternally
        set(value) = definedExternally
    var row: Any?
        get() = definedExternally
        set(value) = definedExternally
}

external interface FusionTablesCell {
    var columnName: String?
        get() = definedExternally
        set(value) = definedExternally
    var value: String?
        get() = definedExternally
        set(value) = definedExternally
}