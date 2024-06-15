package ticketToRide

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.net.URLDecoder
import java.nio.file.*
import kotlin.io.path.name


val games = mutableMapOf<GameId, Game>()

private val rootScope = CoroutineScope(Dispatchers.Default + Job())

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val redis = environment.config.propertyOrNull("redis.host")?.let {
        RedisStorage(
            it.getString(),
            environment.config.property("redis.port").getString().toInt(),
            environment.config.propertyOrNull("redis.password")?.getString()
        )
    }
    val useProtobuf = environment.config.propertyOrNull("use-protobuf") != null
    val formatter = if (useProtobuf) ProtobufFormatter() else JsonFormatter()

    install(Compression) {
        gzip {
            matchContentType(ContentType.parse("*/javascript"))
        }
    }
    install(ContentNegotiation) {
        json(ticketToRide.serialization.json)
    }
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
            resource("client.js")
            resource("favicon.ico")
        }
        static("icons") { resources("icons") }
        static("cards") { resources("cards") }
        static("images") { resources("images") }
        static(".well-known") { resources("wellknown") }

        get("/") {
            call.respondHtml { indexHtml() }
        }

        get("/game/{id}") {
            call.respondHtml { indexHtml() }
        }

        get("/maps/{...}") {
            val mapPath = URLDecoder.decode(call.request.path().removePrefix("/maps").trimStart('/'), Charsets.UTF_8)
            if (mapPath.isBlank()) {
                mapsTree?.let { call.respond(it) }
                    ?: call.respond(HttpStatusCode.NotFound, "Not found")
            } else {
                Game::class.java.classLoader.getResourceAsStream("maps/$mapPath")
                    ?.let {
                        call.respondBytes(ContentType.parse("application/text")) {
                            it.readAllBytes()
                        }
                    }
                    ?: call.respond(HttpStatusCode.NotFound, "Not found")
            }
        }

        route("/internal") {
            get("/games") {
                call.respond(games.entries.associate { it.key.value to it.value.currentState.players.map { it.name } })
            }
            get("/game/{id}") {
                call.parameters["id"]?.let { id ->
                    games[GameId(id)]?.let { game ->
                        call.respond(game.currentState)
                    }
                }
            }
        }
    }
    webSocketGameTransport("game/{id}/ws", rootScope, formatter, redis)
}

val mapsTree by lazy {
    val resourceFolder = "maps"
    val resourceUri = Game::class.java.classLoader.getResource(resourceFolder)?.toURI()
        ?: return@lazy null

    if (resourceUri.scheme == "jar") {
        FileSystems.newFileSystem(resourceUri, emptyMap<String, Any>()).use { fileSystem ->
            val resourcePath = fileSystem.getPath(resourceFolder)
            buildMapsFolder("root", resourcePath)
        }
    } else {
        val resourcePath = Paths.get(resourceUri)
        buildMapsFolder("root", resourcePath)
    }
}

fun buildMapsFolder(name: String, path: Path): MapsTreeItem.Folder {
    return MapsTreeItem.Folder(name, buildList {
        Files.newDirectoryStream(path).use { stream ->
            for (entry in stream) {
                if (Files.isDirectory(entry))
                    add(buildMapsFolder(entry.name, entry))
                else
                    add(MapsTreeItem.Map(entry.name.removeSuffix(".map")))
            }
        }
    }.sortedBy {
        when (it) {
            is MapsTreeItem.Folder -> it.name
            is MapsTreeItem.Map -> it.name
        }
    })
}

fun gameExists(id: GameId, redis: RedisStorage?) = redis?.hasGame(id) ?: games.containsKey(id)

suspend fun loadGame(
    id: GameId,
    redis: RedisStorage?,
    process: suspend (Game) -> ConnectionOutcome
): ConnectionOutcome {
    val game = games.getOrElse(id) {
        redis?.loadGame(id)?.let { (state, map) ->
            Game.start(rootScope, state, map, redis).also { game ->
                games[id] = game
            }
        }
    }
    return if (game != null) process(game)
    else ConnectionOutcome.Failure(ConnectResponse.Failure.NoSuchGame)
}
