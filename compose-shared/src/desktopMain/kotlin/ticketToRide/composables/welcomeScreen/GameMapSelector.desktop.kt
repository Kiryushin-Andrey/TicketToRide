package ticketToRide.composables.welcomeScreen

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ticketToRide.LocalWindowSizeClass
import ticketToRide.WindowSizeClass

@Composable
actual fun withVerticalScroller(block: @Composable () -> Unit) {
    val scrollState = remember { ScrollState(initial = 0) }

    Box(
        modifier = Modifier.run {
            if (LocalWindowSizeClass.current == WindowSizeClass.Compact)
                fillMaxSize()
            else
                width(300.dp).height(600.dp)
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().run {
                if (LocalWindowSizeClass.current == WindowSizeClass.Large)
                    verticalScroll(scrollState)
                else
                    this
            }
        ) {
            block()
        }

        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(8.dp) // Width of the scrollbar
                .padding(vertical = 8.dp)
        )
    }
}