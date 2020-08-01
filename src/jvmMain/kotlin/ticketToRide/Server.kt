package ticketToRide

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.respondHtml
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import core.ConnectionOutcome
import core.PingPong
import core.json
import java.lang.management.ManagementFactory
import java.net.InetAddress

val games = mutableMapOf<GameId, Game>()

private val logger = KotlinLogging.logger("Server")
private val processName = ManagementFactory.getRuntimeMXBean().name

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val googleApiKey = environment.config.property("google-api-key").getString()
    val host = environment.config.property("ktor.deployment.host").getString()
    val redis = environment.config.propertyOrNull("redis.host")?.let {
        RedisStorage(
            it.getString(),
            environment.config.property("redis.port").getString().toInt(),
            environment.config.propertyOrNull("redis.password")?.getString()
        )
    }
    val isLoopbackAddress = InetAddress.getByName(host).isLoopbackAddress
    val debug = environment.config.propertyOrNull("debug") != null

    if (debug) {
        install(DefaultHeaders) {
            header(HttpHeaders.Server, processName)
        }
    }

    install(WebSockets)
    install(Compression) {
        gzip {
            matchContentType(ContentType.parse("*/javascript"))
        }
    }
    install(ContentNegotiation) { json(json) }
    install(CachingHeaders) {
        options { outgoingContent ->
            outgoingContent.contentType?.withoutParameters()?.let {
                if (it == ContentType.Application.JavaScript || it.contentType == ContentType.Image.Any.contentType)
                    CachingOptions(
                        CacheControl.MaxAge(
                            maxAgeSeconds = 24 * 60 * 60 * 30,
                            visibility = CacheControl.Visibility.Public,
                            mustRevalidate = true
                        )
                    )
                else null
            }
        }
    }
    routing {
        static {
            resource("ticket-to-ride.js")
            resource("favicon.ico")
            resource("default.map")
        }
        static("icons") { resources("icons") }
        static("cards") { resources("cards") }
        static("images") { resources("images") }

        get("/") {
            call.respondHtml { indexHtml(googleApiKey, isLoopbackAddress) }
        }
        get("/game/{id}") {
            call.respondHtml { indexHtml(googleApiKey, isLoopbackAddress) }
        }

        route("/internal") {
            get("/games") {
                call.respond(games.entries.associate { it.key.value to it.value.state.players.map { it.name } })
            }
            get("/game/{id}") {
                call.parameters["id"]?.let { id ->
                    games[GameId(id)]?.let { game ->
                        call.respond(game.state)
                    }
                }
            }
        }

        webSocket("game/{id}/ws/play") {
            val gameId = call.parameters["id"]?.let { GameId(it) }
                ?: throw Error("No game id specified for WebSocket connection")

            when (val outcome = connectAsPlayer(gameId, redis)) {

                is ConnectionOutcome.Success -> {
                    val connection = outcome.connection
                    val game = connection.game
                    incoming.consumeAsFlow()
                        .mapNotNull { (it as? Frame.Text)?.readText() }
                        .filter {
                            if (it == PingPong.Ping) send(PingPong.Pong)
                            it != PingPong.Ping
                        }
                        .mapNotNull { req ->
                            kotlin.runCatching { json.parse(Request.serializer(), req) }.also {
                                logger.info { "Received $req from $connection" }
                                it.exceptionOrNull()?.let { e ->
                                    logger.warn(e) { "Failed to deserialize request: $req" }
                                    game.leavePlayer(connection)
                                    close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Bad request"))
                                }
                            }.getOrNull()
                        }
                        .collect { req ->
                            when (req) {
                                is LeaveGameRequest -> {
                                    game.leavePlayer(connection)
                                    close(CloseReason(CloseReason.Codes.NORMAL, "Exit game"))
                                }

                                is Request.ChatMessage -> {
                                    Response.ChatMessage(connection.name, req.message).let { resp ->
                                        game.sendToAllPlayers { resp }
                                    }
                                }

                                is GameRequest ->
                                    game.process(req, connection)
                            }
                        }
                }

                is ConnectionOutcome.CannotJoin -> {
                    close(CloseReason(CloseReason.Codes.NORMAL, "Failed to join game"))
                }
            }
        }

        webSocket("game/{id}/ws/observe") {
            val gameId = call.parameters["id"]?.let { GameId(it) }
                ?: throw Error("No game id specified for WebSocket connection")

            when (val outcome = connectAsObserver(gameId, redis)) {

                is ConnectionOutcome.Success -> {
                    outcome.apply {
                        connection.send(connection.game.state.forObservers(null))
                    }
                    incoming.consumeAsFlow()
                        .mapNotNull { (it as? Frame.Text)?.readText() }
                        .filter { it == PingPong.Ping }
                        .collect { send(PingPong.Pong) }
                }

                is ConnectionOutcome.CannotJoin -> {
                    close(CloseReason(CloseReason.Codes.NORMAL, "Failed to join game"))
                }
            }
        }
    }
}