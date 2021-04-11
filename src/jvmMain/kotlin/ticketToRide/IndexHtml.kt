package ticketToRide

import io.ktor.http.ContentType
import kotlinx.css.*
import kotlinx.html.*
import java.util.*

val appStartTimestamp = Calendar.getInstance().toInstant().toString()

fun HTML.indexHtml(isLoopbackAddress: Boolean) {
    val queryString =
        if (isLoopbackAddress) "ver=${BuildKonfig.version}&ts=${appStartTimestamp}"
        else "ver=${BuildKonfig.version}"

    head {
        title("Ticket to Ride!")
        styleBlock {
            rule("#app") {
                width = 100.pct
                height = 100.pct
            }
            rule(".loading") {
                background = "no-repeat center url('/images/loader.gif'), url('/images/background.jpg') top left / cover"
            }
            rule(".welcome") {
                background = "url('/images/background.jpg') top left / cover"
            }
            rule("html, body") {
                height = 100.pct
                margin = "0"
                padding = "0"
            }
        }
        link("//fonts.googleapis.com/icon?family=Material+Icons", "stylesheet") {
            media = "print"
            onLoad = "this.media = 'all'"
        }
    }
    body {
        div {
            id = "app"
            classes = setOf("loading")
        }
        script(src = "/ticket-to-ride.js?$queryString") {}
    }
}

fun FlowOrMetaDataContent.styleBlock(builder: CSSBuilder.() -> Unit) {
    style(type = ContentType.Text.CSS.toString()) {
        unsafe {
            raw(CSSBuilder().apply(builder).toString())
        }
    }
}