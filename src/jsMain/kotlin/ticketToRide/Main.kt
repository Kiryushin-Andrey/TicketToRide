package ticketToRide

import react.dom.render
import kotlin.browser.document

fun main() {
    render(document.getElementById("app")) {
        child(App::class) {}
    }
}