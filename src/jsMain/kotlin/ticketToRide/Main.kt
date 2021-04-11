package ticketToRide

import kotlinx.browser.document
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import react.dom.render

fun main() {
    val rootDiv = document.getElementById("app")!!
    rootDiv.removeClass("loading")
    rootDiv.addClass("welcome")

    render(rootDiv) {
        child(App::class) {
            attrs {
                onGameStarted = {
                    rootDiv.removeClass("welcome")
                }
            }
        }
    }
}