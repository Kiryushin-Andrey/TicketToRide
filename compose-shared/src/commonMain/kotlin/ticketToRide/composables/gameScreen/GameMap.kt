package ticketToRide.composables.gameScreen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.enableRotation
import ovh.plrapps.mapcompose.api.setTileStreamProvider
import ovh.plrapps.mapcompose.ui.MapUI
import ovh.plrapps.mapcompose.ui.state.MapState
import ovh.plrapps.mapcompose.ui.state.rememberMapState
import java.io.File
import java.io.FileInputStream

@Composable
fun GameMap(modifier: Modifier = Modifier) {
    val tileStreamProvider = { row: Int, col: Int, zoomLvl: Int ->
        FileInputStream(File("path/{$zoomLvl}/{$row}/{$col}.jpg")).asSource()
    }
    val mapState = rememberMapState(4, 4096, 4096).apply {
        setTileStreamProvider(tileStreamProvider)
        enableRotation()
    }

    MapUI(modifier, state = mapState)
}
