package ticketToRide.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ticketToRide.AppState
import ticketToRide.PlayerState
import ticketToRide.Screen
import ticketToRide.WindowSizeClass
import ticketToRide.composables.gameScreen.*
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

@Composable
private fun GameInProgressScreenCompact(vm: GameInProgressVM, modifier: Modifier = Modifier) {
}

@Composable
private fun GameStatusMessage(message: String, color: Color) {
    Box(modifier = Modifier.background(color).fillMaxWidth()) {
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
