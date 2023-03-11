package ticketToRide

import react.create
import react.dom.client.createRoot
import web.dom.document

fun main() {
    val rootDiv = document.getElementById("app")!!
    rootDiv.classList.remove("loading")
    rootDiv.classList.add("welcome")

    val root = createRoot(rootDiv)
    root.render(App.create {
        onGameStarted = {
            rootDiv.classList.remove("welcome")
        }
    })
}
