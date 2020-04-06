@file:JsQualifier("google.maps.adsense")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")
package google.maps.adsense

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
import google.maps.ControlPosition

external open class AdUnit(container: Element, opts: AdUnitOptions) : google.maps.MVCObject {
    open fun getBackgroundColor(): String
    open fun getBorderColor(): String
    open fun getChannelNumber(): String
    open fun getContainer(): Element
    open fun getFormat(): AdFormat
    open fun getMap(): google.maps.Map<Element>
    open fun getPosition(): ControlPosition
    open fun getPublisherId(): String
    open fun getTextColor(): String
    open fun getTitleColor(): String
    open fun getUrlColor(): String
    open fun setBackgroundColor(backgroundColor: String)
    open fun setBorderColor(borderColor: String)
    open fun setChannelNumber(channelNumber: String)
    open fun setFormat(format: AdFormat)
    open fun setMap(map: google.maps.Map<Element>?)
    open fun setPosition(position: ControlPosition)
    open fun setTextColor(textColor: String)
    open fun setTitleColor(titleColor: String)
    open fun setUrlColor(urlColor: String)
}

external interface AdUnitOptions {
    var backgroundColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var borderColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var channelNumber: String?
        get() = definedExternally
        set(value) = definedExternally
    var format: AdFormat?
        get() = definedExternally
        set(value) = definedExternally
    var map: Any?
        get() = definedExternally
        set(value) = definedExternally
    var position: ControlPosition?
        get() = definedExternally
        set(value) = definedExternally
    var publisherId: String?
        get() = definedExternally
        set(value) = definedExternally
    var textColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var titleColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var urlColor: String?
        get() = definedExternally
        set(value) = definedExternally
}

external enum class AdFormat {
    BANNER /* = '468x60_as' */,
    BUTTON /* = '125x125_as' */,
    HALF_BANNER /* = '234x60_as' */,
    LARGE_HORIZONTAL_LINK_UNIT /* = '728x15_0ads_al' */,
    LARGE_RECTANGLE /* = '336x280_as' */,
    LARGE_VERTICAL_LINK_UNIT /* = '180x90_0ads_al' */,
    LEADERBOARD /* = '728x90_as' */,
    MEDIUM_RECTANGLE /* = '300x250_as' */,
    MEDIUM_VERTICAL_LINK_UNIT /* = '160x90_0ads_al' */,
    SKYSCRAPER /* = '120x600_as' */,
    SMALL_HORIZONTAL_LINK_UNIT /* = '468x15_0ads_al' */,
    SMALL_RECTANGLE /* = '180x150_as' */,
    SMALL_SQUARE /* = '200x200_as' */,
    SMALL_VERTICAL_LINK_UNIT /* = '120x90_0ads_al' */,
    SQUARE /* = '250x250_as' */,
    VERTICAL_BANNER /* = '120x240_as' */,
    WIDE_SKYSCRAPER /* = '160x600_as' */,
    X_LARGE_VERTICAL_LINK_UNIT /* = '200x90_0ads_al' */
}