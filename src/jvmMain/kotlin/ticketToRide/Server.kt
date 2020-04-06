package ticketToRide

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.*
import io.ktor.jackson.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*

const val GoogleMapsKey = "AIzaSyCdpAiP1sFvTVh7uPsCKoFuKsE1BYsY-Q0"
val requestsQueue = Channel<ApiRequest>()
val subscriptions = mutableListOf<(Game) -> Unit>()

fun main(args: Array<String>) = runBlocking {
    launch {
        runEngine(requestsQueue.consumeAsFlow()).collect { state ->
            for (notify in subscriptions) notify(state)
        }
    }
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        jackson {}
    }
    install(WebSockets)
    routing {
        static {
            resource("ticket-to-ride.js")
            resources("icons")
        }
        get("/") {
            call.respondHtml { indexHtml() }
        }
        webSocket("ws") {
            subscriptions.add {
                // serialize state to JSON
                // send(it)
                TODO()
            }
            incoming.consumeAsFlow()
                .mapNotNull { (it as? Frame.Text)?.readText() }
                .collect {
                    // deserialize request from JSON
                    // requestsQueue.offer()
                    TODO()
                }
        }
    }
}