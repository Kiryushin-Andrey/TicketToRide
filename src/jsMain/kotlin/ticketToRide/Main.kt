package ticketToRide

import kotlinx.browser.document
import react.dom.render

fun main() {
    render(document.getElementById("app")) {
        child(App::class) {}
    }
}