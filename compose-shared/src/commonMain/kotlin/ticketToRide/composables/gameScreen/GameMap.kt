package ticketToRide.composables.gameScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSource
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.scale
import ovh.plrapps.mapcompose.ui.MapUI
import ovh.plrapps.mapcompose.ui.state.MapState
import ticketToRide.MapTilesProvider

@Composable
fun GameMap(size: IntSize, modifier: Modifier = Modifier) {
    val tilesHttpClient = remember { HttpClient() }

    val mapState = remember(size) {
        MapState(5, size.width, size.height).apply {
            val tileStreamProvider = { row: Int, col: Int, zoomLvl: Int ->
                runBlocking {
                    tilesHttpClient.request(MapTilesProvider.Watermark.provider(col, row, zoomLvl)) {
                        headers.append("Authorization", "Stadia-Auth $API_KEY")
                    }.readBytes().inputStream().asSource()
                }
            }
            addLayer(tileStreamProvider)
            scale = 4.0f
        }
    }

    MapUI(
        modifier.fillMaxSize(),
        state = mapState
    )
}

private const val API_KEY = "..."
