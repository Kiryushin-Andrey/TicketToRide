@file:JsModule("fscreen")
@file:JsNonModule
package fscreen

import web.html.HTMLElement

external interface FScreen {
    val fullscreenEnabled: Boolean
    val fullscreenElement: HTMLElement?
    fun requestFullscreen(element: HTMLElement)
    fun exitFullscreen()
}

@JsName("default")
external val fScreen: FScreen
