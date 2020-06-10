@file:JsQualifier("google.maps.event")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")
package google.maps.event

import google.maps.*
import org.w3c.dom.events.Event

external fun addDomListener(instance: Any?, eventName: String, handler: (event: Event) -> Unit, capture: Boolean = definedExternally): MapsEventListener

external fun addDomListenerOnce(instance: Any?, eventName: String, handler: (event: Event) -> Unit, capture: Boolean = definedExternally): MapsEventListener

external fun addListener(instance: Any?, eventName: String, handler: (args: Array<Any>) -> Unit): MapsEventListener

external fun addListenerOnce(instance: Any?, eventName: String, handler: (args: Array<Any>) -> Unit): MapsEventListener

external fun clearInstanceListeners(instance: Any?)

external fun clearListeners(instance: Any?, eventName: String)

external fun removeListener(listener: MapsEventListener)

external fun trigger(instance: Any, eventName: String, vararg args: Any)