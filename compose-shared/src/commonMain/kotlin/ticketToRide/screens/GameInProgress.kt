package ticketToRide.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.painterResource
import ticketToRide.AppState
import ticketToRide.MR
import ticketToRide.Screen
import ticketToRide.WindowSizeClass
import ticketToRide.composables.gameScreen.CardsDeck
import ticketToRide.composables.gameScreen.CardsOnHand
import ticketToRide.composables.gameScreen.GameMap
import ticketToRide.composables.gameScreen.PlayersList
import ticketToRide.composables.gameScreen.TicketsOnHand
import ticketToRide.localization.GameScreenStrings

@Composable
fun GameInProgressScreen(
    screenState: Screen.GameInProgress,
    appState: AppState,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val vm = remember(screenState, appState) { GameInProgressVM(screenState, appState) }
    when (windowSizeClass) {
        WindowSizeClass.Large ->
            GameInProgressScreenLarge(vm, modifier)
        WindowSizeClass.Compact ->
            GameInProgressScreenCompact(vm, modifier)
    }
}

@Composable
private fun GameInProgressScreenLarge(vm: GameInProgressVM, modifier: Modifier = Modifier) {
    val str = remember(vm.locale) { GameScreenStrings(vm.locale) }
    Column(modifier = modifier.fillMaxSize()) {
        when {
            vm.gameState.myTurn ->
                GameStatusMessage(str.yourTurn, Color(0x90, 0xEE, 0x90))
            vm.gameState.lastRound ->
                GameStatusMessage(str.lastRound, Color(0xE9, 0x96, 0x7A))
            else ->
                GameStatusMessage(str.playerXmoves(vm.gameState.players[vm.gameState.turn].name.value), Color.White)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            GameMap(modifier = Modifier.fillMaxSize())
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                PlayersList(vm.gameState.players, vm.gameState.turn, modifier = Modifier.padding(start = 8.dp, top = 8.dp))

//                GameActionsLog()
//                ChatSendMessageTextBox()
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                CardsOnHand(vm.gameState.myCards, modifier = Modifier.padding(end = 16.dp, top = 8.dp))
                Spacer(Modifier.height(16.dp))
                TicketsOnHand(vm.gameState, modifier = Modifier.padding(end = 16.dp))
                Spacer(Modifier.height(16.dp))
                
//                when (val state = vm.playerState) {
//                    is PlayerState.MyTurn.PickedCity -> {
//                        PickedCityMoveAction(state)
//                        Spacer(Modifier.height(16.dp))
//                    }
//                    is PlayerState.MyTurn.BuildingStation -> {
//                        BuildStationMoveAction(state)
//                        Spacer(Modifier.height(16.dp))
//                    }
//                    is PlayerState.MyTurn.BuildingSegment -> {
//                        BuildSegmentMoveAction(state)
//                        Spacer(Modifier.height(16.dp))
//                    }
//                    else -> {}
//                }
//                CitySearchTextBox()
            }
            CardsDeck(modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameInProgressScreenCompact(vm: GameInProgressVM, modifier: Modifier = Modifier) {
    val str = remember(vm.locale) { GameScreenStrings(vm.locale) }
    val selectedNavItem = remember { mutableStateOf<SelectedNavItem?>(null) }

    Box(modifier = modifier.background(Color.White).fillMaxSize()) {
        GameMap(modifier = Modifier.fillMaxSize())
        NavigationBar(modifier = Modifier.align(Alignment.BottomCenter)) {
            navBarItem(SelectedNavItem.Players, selectedNavItem)
            navBarItem(SelectedNavItem.MyHand, selectedNavItem)
        }
    }

    if (selectedNavItem.value != null) {
        val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        ModalBottomSheet(
            onDismissRequest = {
                selectedNavItem.value = null
            }
        ) {
            Column(modifier = Modifier.padding(bottom = bottomPadding)) {
                when (selectedNavItem.value) {
                    SelectedNavItem.Players ->
                        PlayersList(
                            vm.gameState.players,
                            vm.gameState.turn,
                            modifier = Modifier.padding(8.dp).fillMaxWidth()
                        )

                    SelectedNavItem.MyHand ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            TicketsOnHand(vm.gameState, modifier = Modifier.fillMaxWidth())
                            CardsOnHand(
                                vm.gameState.myCards,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun RowScope.navBarItem(item: SelectedNavItem, selectedNavItem: MutableState<SelectedNavItem?>) {
    NavigationBarItem(
        selected = selectedNavItem.value == item,
        onClick = {
            selectedNavItem.value = item.takeUnless { selectedNavItem.value == item }
        },
        icon = {
            Icon(
                painterResource(item.icon),
                contentDescription = item.contentDescription,
                modifier = Modifier.size(32.dp)
            )
        },
        alwaysShowLabel = true,
        label = { Text(item.title) },
    )
}

private enum class SelectedNavItem {
    Players {
        override val title = "Players"
        override val contentDescription = "Player Rankings"
        override val icon = MR.images.ranking
    },
    MyHand {
        override val title = "My Hand"
        override val contentDescription = "My Hand"
        override val icon = MR.images.myHand
    };

    abstract val title: String
    abstract val contentDescription: String
    abstract val icon: ImageResource
}

@Composable
private fun GameStatusMessage(message: String, color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(color).fillMaxWidth()) {
        Text(
            message,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp).align(Alignment.Center)
        )
    }
}

class GameInProgressVM(
    private val screenState: Screen.GameInProgress,
    private val appState: AppState
) {
    val locale get() = appState.locale
    val gameState get() = screenState.gameState
    val playerState get() = screenState.playerState
}
