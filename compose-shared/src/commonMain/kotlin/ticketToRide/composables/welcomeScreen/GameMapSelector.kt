package ticketToRide.composables.welcomeScreen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import dev.icerock.moko.resources.compose.painterResource
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import ticketToRide.*
import ticketToRide.platform.FileSaveDialog
import ticketToRide.platform.FileUploadDialog

@Composable
fun GameMapSelector(vm: NewGameVM, serverHost: String) {
    var saveFileDialogVisible by remember { mutableStateOf(false) }
    var uploadFileDialogVisible by remember { mutableStateOf(false) }
    var mapsTree by remember { mutableStateOf<MapsTreeItem.Folder?>(null) }

    LaunchedEffect(Unit) {
        HttpClient().use {
            val response = it.get(URLBuilder(serverHost).apply { path("maps") }.buildString())
            if (!response.status.isSuccess())
                error("Cannot connect to server")

            Json.decodeFromString<MapsTreeItem.Folder>(response.bodyAsText()).let {
                mapsTree = it
            }
        }
    }

    Text("Game map", fontSize = 16.sp)
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            mapsTree?.let {
                ChooseMapPopupMenu(vm.gameMap, it) { selectedMap ->
                    vm.gameMap = ConnectRequest.StartGameMap.BuiltIn(selectedMap)
                }
            }

            if (vm.gameMap is ConnectRequest.StartGameMap.BuiltIn) {
                TextButton(
                    onClick = { saveFileDialogVisible = true },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(painterResource(MR.images.download), "Download")
                }
            }
        }

        TextButton(
            onClick = { uploadFileDialogVisible = true },
        ) {
            Icon(painterResource(MR.images.upload), "Upload my map")
            if (LocalWindowSizeClass.current == WindowSizeClass.Large) {
                Text("Upload my map", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
    Text(
        "You can create and upload your own game map. Download the built-in map file to see a sample",
        style = MaterialTheme.typography.labelSmall
    )

    val builtInGameMap = vm.gameMap as? ConnectRequest.StartGameMap.BuiltIn
    if (saveFileDialogVisible && builtInGameMap != null) {
        FileSaveDialog(
            mapName = builtInGameMap.path.last(),
            content = {
                HttpClient().use { client ->
                    val url = URLBuilder(serverHost)
                        .apply { pathSegments = listOf("maps") + builtInGameMap.path }
                        .buildString() + ".map"
                    val response = client.get(url)
                    if (!response.status.isSuccess())
                        error("Cannot connect to server")
                    response.bodyAsText()
                }
            },
            close = {
                saveFileDialogVisible = false
            }
        )
    }
    if (uploadFileDialogVisible) {
        FileUploadDialog(onMapUploaded = { file ->
            when (val result = GameMap.parse(file.content)) {
                is Try.Success ->
                    vm.gameMap = ConnectRequest.StartGameMap.Custom(file.filename, result.value)
                is Try.Error -> {
                    vm.gameMap = null
                    vm.customMapParseErrors = result.errors
                }
            }
        }) {
            uploadFileDialogVisible = false
        }
    }
}

@Composable
fun ChooseMapPopupMenu(
    gameMap: ConnectRequest.StartGameMap?,
    rootFolder: MapsTreeItem.Folder,
    onChooseMap: (List<String>) -> Unit
) {
    val showMenu = remember { mutableStateOf(false) }
    val currentFolder = remember { mutableStateOf(listOf(rootFolder)) }

    Column {
        TextButton(
            onClick = {
                currentFolder.value = listOf(rootFolder)
                showMenu.value = true
            }
        ) {
            Text(
                when (gameMap) {
                    is ConnectRequest.StartGameMap.BuiltIn -> gameMap.path.last()
                    is ConnectRequest.StartGameMap.Custom -> gameMap.filename
                    null -> "not selected"
                },
                color = if (gameMap == null) MaterialTheme.colorScheme.error else Color.Unspecified
            )
            Icon(
                painter = painterResource(MR.images.folderOpen),
                contentDescription = "Choose another game map",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable {
                        currentFolder.value = listOf(rootFolder)
                        showMenu.value = true
                    }
            )
        }

        DropdownMenu(
            expanded = showMenu.value,
            onDismissRequest = { showMenu.value = false },
            properties = PopupProperties(focusable = true)
        ) {
            Row {
                currentFolder.value.forEachIndexed { ix, folder ->
                    if (LocalWindowSizeClass.current == WindowSizeClass.Large || ix == currentFolder.value.size - 1) {
                        chooseMapMenuColumn(currentFolder, folder, ix) {
                            showMenu.value = false
                            onChooseMap(it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun chooseMapMenuColumn(
    currentFolder: MutableState<List<MapsTreeItem.Folder>>,
    folder: MapsTreeItem.Folder,
    ix: Int,
    onChooseMap: (List<String>) -> Unit
) {
    withVerticalScroller {
        if (LocalWindowSizeClass.current == WindowSizeClass.Compact && currentFolder.value.size > 1) {
            DropdownMenuItem(
                text = { Text("Back") },
                onClick = {
                    currentFolder.value = currentFolder.value.dropLast(1)
                },
                leadingIcon = {
                    Icon(painterResource(MR.images.back), contentDescription = null)
                }
            )
            HorizontalDivider(color = Color.Gray, thickness = 1.dp)
        }

        folder.children.filterIsInstance<MapsTreeItem.Folder>().forEach { folder ->
            DropdownMenuItem(
                text = {
                    Text(
                        folder.name,
                        modifier = Modifier.apply {
                            if (ix < currentFolder.value.size - 1 && currentFolder.value[ix + 1] == folder) {
                                background(Color.Red)
                            }
                        },
                    )
                },
                leadingIcon = {
                    Icon(
                        painterResource(
                            if (ix < currentFolder.value.size - 1 && currentFolder.value[ix + 1] == folder)
                                MR.images.folderOpen
                            else
                                MR.images.folder
                        ),
                        contentDescription = null
                    )
                },
                onClick = {
                    currentFolder.value = currentFolder.value.take(ix + 1) + folder
                }
            )
        }

        folder.children.filterIsInstance<MapsTreeItem.Map>().forEach { map ->
            DropdownMenuItem(
                text = { Text(map.name) },
                leadingIcon = {
                    Icon(painterResource(MR.images.map), contentDescription = null)
                },
                onClick = {
                    onChooseMap(currentFolder.value.drop(1).take(ix + 1).map { it.name } + map.name)
                }
            )
        }
    }
}

@Composable
expect fun withVerticalScroller(block: @Composable () -> Unit)
