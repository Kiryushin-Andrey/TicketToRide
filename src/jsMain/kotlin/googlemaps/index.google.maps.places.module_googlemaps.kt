@file:JsQualifier("google.maps.places")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")
package google.maps.places

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

external open class Autocomplete(inputField: HTMLInputElement, opts: AutocompleteOptions = definedExternally) : google.maps.MVCObject {
    open fun getBounds(): google.maps.LatLngBounds
    open fun getPlace(): PlaceResult
    open fun setBounds(bounds: google.maps.LatLngBounds)
    open fun setBounds(bounds: google.maps.LatLngBoundsLiteral)
    open fun setComponentRestrictions(restrictions: ComponentRestrictions)
    open fun setFields(fields: Array<String>?)
    open fun setOptions(options: AutocompleteOptions)
    open fun setTypes(types: Array<String>)
}

external interface AutocompleteOptions {
    var bounds: dynamic /* LatLngBounds | LatLngBoundsLiteral */
        get() = definedExternally
        set(value) = definedExternally
    var componentRestrictions: ComponentRestrictions?
        get() = definedExternally
        set(value) = definedExternally
    var placeIdOnly: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var strictBounds: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var types: Array<String>?
        get() = definedExternally
        set(value) = definedExternally
    var type: String?
        get() = definedExternally
        set(value) = definedExternally
    var fields: Array<String>?
        get() = definedExternally
        set(value) = definedExternally
}

external interface AutocompletePrediction {
    var description: String
    var id: String
    var matched_substrings: Array<PredictionSubstring>
    var place_id: String
    var reference: String
    var structured_formatting: AutocompleteStructuredFormatting
    var terms: Array<PredictionTerm>
    var types: Array<String>
}

external interface AutocompleteStructuredFormatting {
    var main_text: String
    var main_text_matched_substrings: Array<PredictionSubstring>
    var secondary_text: String
    var secondary_text_matched_substrings: Array<PredictionSubstring>?
        get() = definedExternally
        set(value) = definedExternally
}

external interface OpeningHours {
    var open_now: Boolean
    var periods: Array<OpeningPeriod>
    var weekday_text: Array<String>
    fun isOpen(date: Date = definedExternally): Boolean
}

external interface OpeningPeriod {
    var open: OpeningHoursTime
    var close: OpeningHoursTime?
        get() = definedExternally
        set(value) = definedExternally
}

external interface OpeningHoursTime {
    var day: Number
    var hours: Number
    var minutes: Number
    var nextDate: Number
    var time: String
}

external interface PredictionTerm {
    var offset: Number
    var value: String
}

external interface PredictionSubstring {
    var length: Number
    var offset: Number
}

external open class AutocompleteService {
    open fun getPlacePredictions(request: AutocompletionRequest, callback: (result: Array<AutocompletePrediction>, status: PlacesServiceStatus) -> Unit)
    open fun getQueryPredictions(request: QueryAutocompletionRequest, callback: (result: Array<QueryAutocompletePrediction>, status: PlacesServiceStatus) -> Unit)
}

external open class AutocompleteSessionToken

external interface AutocompletionRequest {
    var bounds: dynamic /* LatLngBounds | LatLngBoundsLiteral */
        get() = definedExternally
        set(value) = definedExternally
    var componentRestrictions: ComponentRestrictions?
        get() = definedExternally
        set(value) = definedExternally
    var input: String
    var location: Any?
        get() = definedExternally
        set(value) = definedExternally
    var offset: Number?
        get() = definedExternally
        set(value) = definedExternally
    var radius: Number?
        get() = definedExternally
        set(value) = definedExternally
    var sessionToken: AutocompleteSessionToken?
        get() = definedExternally
        set(value) = definedExternally
    var types: Array<String>?
        get() = definedExternally
        set(value) = definedExternally
}

external interface ComponentRestrictions {
    var country: dynamic /* String | Array<String> */
        get() = definedExternally
        set(value) = definedExternally
}

external interface PlaceAspectRating {
    var rating: Number
    var type: String
}

external interface PlacePlusCode {
    var compound_code: String?
        get() = definedExternally
        set(value) = definedExternally
    var global_code: String
}

external interface PlaceDetailsRequest {
    var placeId: String
    var fields: Array<String>?
        get() = definedExternally
        set(value) = definedExternally
    var sessionToken: AutocompleteSessionToken?
        get() = definedExternally
        set(value) = definedExternally
}

external interface PlaceGeometry {
    var location: Any
    var viewport: Any
}

external interface PlacePhoto {
    var height: Number
    var html_attributions: Array<String>
    var width: Number
    fun getUrl(opts: PhotoOptions): String
}

external interface PhotoOptions {
    var maxHeight: Number?
        get() = definedExternally
        set(value) = definedExternally
    var maxWidth: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface PlaceResult {
    var address_components: Array<GeocoderAddressComponent>?
        get() = definedExternally
        set(value) = definedExternally
    var adr_address: String?
        get() = definedExternally
        set(value) = definedExternally
    var aspects: Array<PlaceAspectRating>?
        get() = definedExternally
        set(value) = definedExternally
    var formatted_address: String?
        get() = definedExternally
        set(value) = definedExternally
    var formatted_phone_number: String?
        get() = definedExternally
        set(value) = definedExternally
    var geometry: PlaceGeometry?
        get() = definedExternally
        set(value) = definedExternally
    var html_attributions: Array<String>?
        get() = definedExternally
        set(value) = definedExternally
    var icon: String?
        get() = definedExternally
        set(value) = definedExternally
    var id: String?
        get() = definedExternally
        set(value) = definedExternally
    var international_phone_number: String?
        get() = definedExternally
        set(value) = definedExternally
    var name: String
    var opening_hours: OpeningHours?
        get() = definedExternally
        set(value) = definedExternally
    var permanently_closed: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var photos: Array<PlacePhoto>?
        get() = definedExternally
        set(value) = definedExternally
    var place_id: String?
        get() = definedExternally
        set(value) = definedExternally
    var plus_code: PlacePlusCode?
        get() = definedExternally
        set(value) = definedExternally
    var price_level: Number?
        get() = definedExternally
        set(value) = definedExternally
    var rating: Number?
        get() = definedExternally
        set(value) = definedExternally
    var reviews: Array<PlaceReview>?
        get() = definedExternally
        set(value) = definedExternally
    var types: Array<String>?
        get() = definedExternally
        set(value) = definedExternally
    var url: String?
        get() = definedExternally
        set(value) = definedExternally
    var user_ratings_total: Number?
        get() = definedExternally
        set(value) = definedExternally
    var utc_offset: Number?
        get() = definedExternally
        set(value) = definedExternally
    var utc_offset_minutes: Number?
        get() = definedExternally
        set(value) = definedExternally
    var vicinity: String?
        get() = definedExternally
        set(value) = definedExternally
    var website: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface PlaceReview {
    var aspects: Array<PlaceAspectRating>
    var author_name: String
    var author_url: String
    var language: String
    var text: String
}

external interface PlaceSearchPagination {
    fun nextPage()
    var hasNextPage: Boolean
}

external interface PlaceSearchRequest {
    var bounds: dynamic /* LatLngBounds | LatLngBoundsLiteral */
        get() = definedExternally
        set(value) = definedExternally
    var keyword: String?
        get() = definedExternally
        set(value) = definedExternally
    var location: dynamic /* LatLng | LatLngLiteral */
        get() = definedExternally
        set(value) = definedExternally
    var maxPriceLevel: Number?
        get() = definedExternally
        set(value) = definedExternally
    var minPriceLevel: Number?
        get() = definedExternally
        set(value) = definedExternally
    var name: String?
        get() = definedExternally
        set(value) = definedExternally
    var openNow: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var radius: Number?
        get() = definedExternally
        set(value) = definedExternally
    var rankBy: RankBy?
        get() = definedExternally
        set(value) = definedExternally
    var types: Array<String>?
        get() = definedExternally
        set(value) = definedExternally
    var type: String?
        get() = definedExternally
        set(value) = definedExternally
}

external open class PlacesService {
    constructor(attrContainer: HTMLDivElement)
    constructor(attrContainer: google.maps.Map<Element>)
    open fun findPlaceFromPhoneNumber(request: FindPlaceFromPhoneNumberRequest, callback: (results: Array<PlaceResult>, status: PlacesServiceStatus) -> Unit)
    open fun findPlaceFromQuery(request: FindPlaceFromQueryRequest, callback: (results: Array<PlaceResult>, status: PlacesServiceStatus) -> Unit)
    open fun getDetails(request: PlaceDetailsRequest, callback: (result: PlaceResult, status: PlacesServiceStatus) -> Unit)
    open fun nearbySearch(request: PlaceSearchRequest, callback: (results: Array<PlaceResult>, status: PlacesServiceStatus, pagination: PlaceSearchPagination) -> Unit)
    open fun radarSearch(request: RadarSearchRequest, callback: (results: Array<PlaceResult>, status: PlacesServiceStatus) -> Unit)
    open fun textSearch(request: TextSearchRequest, callback: (results: Array<PlaceResult>, status: PlacesServiceStatus, pagination: PlaceSearchPagination) -> Unit)
}

external enum class PlacesServiceStatus {
    INVALID_REQUEST /* = 'INVALID_REQUEST' */,
    NOT_FOUND /* = 'NOT_FOUND' */,
    OK /* = 'OK' */,
    OVER_QUERY_LIMIT /* = 'OVER_QUERY_LIMIT' */,
    REQUEST_DENIED /* = 'REQUEST_DENIED' */,
    UNKNOWN_ERROR /* = 'UNKNOWN_ERROR' */,
    ZERO_RESULTS /* = 'ZERO_RESULTS' */
}

external interface QueryAutocompletePrediction {
    var description: String
    var id: String?
        get() = definedExternally
        set(value) = definedExternally
    var matched_substrings: Array<PredictionSubstring>
    var place_id: String
    var terms: Array<PredictionTerm>
}

external interface QueryAutocompletionRequest {
    var bounds: dynamic /* LatLngBounds | LatLngBoundsLiteral */
        get() = definedExternally
        set(value) = definedExternally
    var input: String?
        get() = definedExternally
        set(value) = definedExternally
    var location: Any?
        get() = definedExternally
        set(value) = definedExternally
    var offset: Number?
        get() = definedExternally
        set(value) = definedExternally
    var radius: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface RadarSearchRequest {
    var bounds: dynamic /* LatLngBounds | LatLngBoundsLiteral */
        get() = definedExternally
        set(value) = definedExternally
    var keyword: String?
        get() = definedExternally
        set(value) = definedExternally
    var location: dynamic /* LatLng | LatLngLiteral */
        get() = definedExternally
        set(value) = definedExternally
    var name: String?
        get() = definedExternally
        set(value) = definedExternally
    var radius: Number?
        get() = definedExternally
        set(value) = definedExternally
    var types: Array<String>?
        get() = definedExternally
        set(value) = definedExternally
    var type: String?
        get() = definedExternally
        set(value) = definedExternally
}

external enum class RankBy {
    PROMINENCE /* = 0 */,
    DISTANCE /* = 1 */
}

external open class SearchBox(inputField: HTMLInputElement, opts: SearchBoxOptions = definedExternally) : google.maps.MVCObject {
    open fun getBounds(): google.maps.LatLngBounds
    open fun getPlaces(): Array<PlaceResult>
    open fun setBounds(bounds: google.maps.LatLngBounds)
    open fun setBounds(bounds: google.maps.LatLngBoundsLiteral)
}

external interface SearchBoxOptions {
    var bounds: dynamic /* LatLngBounds | LatLngBoundsLiteral */
        get() = definedExternally
        set(value) = definedExternally
}

external interface TextSearchRequest {
    var bounds: dynamic /* LatLngBounds | LatLngBoundsLiteral */
        get() = definedExternally
        set(value) = definedExternally
    var location: dynamic /* LatLng | LatLngLiteral */
        get() = definedExternally
        set(value) = definedExternally
    var query: String
    var radius: Number?
        get() = definedExternally
        set(value) = definedExternally
    var types: Array<String>?
        get() = definedExternally
        set(value) = definedExternally
    var type: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface FindPlaceFromQueryRequest {
    var fields: Array<String>
    var locationBias: dynamic /* LatLng | LatLngLiteral | LatLngBounds | LatLngBoundsLiteral | Circle | CircleLiteral | String */
        get() = definedExternally
        set(value) = definedExternally
    var query: String
}

external interface FindPlaceFromPhoneNumberRequest {
    var fields: Array<String>
    var locationBias: dynamic /* LatLng | LatLngLiteral | LatLngBounds | LatLngBoundsLiteral | Circle | CircleLiteral | String */
        get() = definedExternally
        set(value) = definedExternally
    var phoneNumber: String
}