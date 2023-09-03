package ticketToRide

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.net.URLDecoder
import java.nio.file.*
import kotlin.io.path.name

import ticketToRide.serialization.json

val games = mutableMapOf<GameId, Game>()

private val rootScope = CoroutineScope(Dispatchers.Default + Job())

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    val redis = environment.config.propertyOrNull("redis.host")?.let {
        RedisStorage(
            it.getString(),
            environment.config.property("redis.port").getString().toInt(),
            environment.config.propertyOrNull("redis.password")?.getString()
        )
    }

    install(Compression) {
        gzip {
            matchContentType(ContentType.parse("*/javascript"))
        }
    }
    install(ContentNegotiation) {
        json(json)
    }
    install(CachingHeaders) {
        options { _, content ->
            content.contentType?.withoutParameters()?.let {
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
        staticResources("/icons", "icons")
        staticResources("/cards", "cards")
        staticResources("/images", "images")
        staticResources("/.well-known", "wellknown")

        get("/") {
            call.respondHtml { indexHtml() }
        }

        get("/game/{id}") {
            call.respondHtml { indexHtml() }
        }
        get("/game-exists/{id}") {
            val gameId = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "id is required")
                return@get
            }
            val game = games[GameId(gameId)] ?: run {
                call.respond(HttpStatusCode.NotFound, "Game not found")
                return@get
            }
            call.respond(HttpStatusCode.OK, game.currentState.startedBy)
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
    webSocketGameTransport("game/{id}/ws", rootScope, redis)
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
