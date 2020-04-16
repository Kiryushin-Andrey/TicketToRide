package ticketToRide

import react.dom.render
import kotlin.browser.document

fun main() {
    render(document.body) {
        child(App::class) {}
    }
}