package ticketToRide.composables.gameScreen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import ticketToRide.*

@Composable
fun PlayersList(players: List<PlayerView>, whoseTurnIx: Int, modifier: Modifier = Modifier) {
    val modifierBase = if (LocalWindowSizeClass.current == WindowSizeClass.Compact) Modifier.fillMaxWidth() else Modifier

    Column(modifier = modifier) {
        players.forEachIndexed { ix, player ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = player.color.color.copy(alpha = 0.3f)
                ),
                modifier = modifierBase.padding(4.dp)
                    .run {
                        if (ix == whoseTurnIx) {
                            border(2.dp, Color.Red, CardDefaults.shape)
                        } else
                            this
                    }
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(player.name.value)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = modifierBase
                    ) {
                        ArtifactOnHand(
                            painterResource(MR.images.railwayCar),
                            player.carsLeft,
                            "Cars on hand"
                        )
                        ArtifactOnHand(
                            painterResource(MR.images.station),
                            player.carsLeft,
                            "Stations on hand"
                        )
                        ArtifactOnHand(
                            painterResource(MR.images.cardsDeck),
                            player.carsLeft,
                            "Cards on hand"
                        )
                        ArtifactOnHand(
                            painterResource(MR.images.ticket),
                            player.carsLeft,
                            "Tickets on hand"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtifactOnHand(painter: Painter, count: Int, name: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(end = 24.dp)) {
        Icon(painter, name, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(8.dp))
        Text(count.toString())
    }
}
