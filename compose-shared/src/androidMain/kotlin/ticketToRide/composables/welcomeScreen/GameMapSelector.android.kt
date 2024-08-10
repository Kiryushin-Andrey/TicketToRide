package ticketToRide.composables.welcomeScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun withVerticalScroller(block: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        block()
    }
}