package ticketToRide

import io.ktor.http.ContentType
import kotlinx.css.*
import kotlinx.html.*
import java.util.*

val appStartTimestamp = Calendar.getInstance().toInstant().toString()

fun HTML.indexHtml(googleApiKey: String, isLoopbackAddress: Boolean) {
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
            +"Loading..."
        }
        script(src = "//maps.googleapis.com/maps/api/js?key=${googleApiKey}") {}
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