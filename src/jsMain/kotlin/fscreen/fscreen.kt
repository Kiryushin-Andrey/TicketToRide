@file:JsModule("fscreen")
@file:JsNonModule
package fscreen

import org.w3c.dom.Element

external interface FScreen {
    val fullscreenEnabled: Boolean
    val fullscreenElement: Element?
    fun requestFullscreen(element: Element)
    fun exitFullscreen()
}

@JsName("default")
external val fScreen: FScreen
