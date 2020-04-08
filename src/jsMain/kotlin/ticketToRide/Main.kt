package ticketToRide

import react.dom.render
import ticketToRide.components.App
import kotlin.browser.document

fun main() {
    render(document.getElementById("root")) {
        child(App::class) {}
    }
}