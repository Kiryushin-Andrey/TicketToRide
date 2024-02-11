package ticketToRide.composables.gameScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.icerock.moko.resources.compose.painterResource
import ticketToRide.Card
import ticketToRide.CardColor
import ticketToRide.color
import ticketToRide.MR

@Composable
fun CardsOnHand(cards: List<Card>, modifier: Modifier = Modifier) {
    val locoBrush = remember {
        Brush.verticalGradient(
            listOf(
                CardColor.ORANGE.color,
                Color.Yellow,
                Color.Green,
                Color.Magenta,
                Color.Cyan,
                Color.Blue,
                Color(0xFFEE82EE)
            ).map { it.copy(alpha = 0.3f) }
        )
    }

    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
    ) {
        for ((card, count) in cards.groupingBy { it }.eachCount()) {
            TooltipArea(
                when (card) {
                    is Card.Car -> card.color.name.lowercase().replaceFirstChar { it.uppercase() }
                    is Card.Loco -> "Loco (can be used in place of any color)"
                }
            ) {
                val cardsInStack = minOf(count, 6) - 1
                (1..cardsInStack).forEach { ix ->
                    MyCard(
                        card,
                        count,
                        locoBrush,
                        modifier = Modifier
                            .offset(x = ix.dp, y = ix.dp)
                            .zIndex(100 - ix.toFloat())
                    )
                }

                val topCardIx = cardsInStack + 1
                MyCard(
                    card,
                    count,
                    locoBrush,
                    modifier = Modifier
                        .offset(x = topCardIx.dp, y = topCardIx.dp)
                        .zIndex(100 - topCardIx.toFloat())
                )
            }
        }
    }
}

@Composable
private fun MyCard(card: Card, count: Int, locoBrush: Brush, modifier: Modifier = Modifier) {
    Card(
        shape = CardDefaults.outlinedShape,
        border = BorderStroke(1.dp, Color.Black),
        modifier = modifier
            .size(width = 64.dp, height = 96.dp)
            .padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
                .run {
                    when (card) {
                        is Card.Car -> background(card.backgroundColor)
                        is Card.Loco -> background(brush = locoBrush)
                    }
                }
                .padding(8.dp)
        ) {
            when (card) {
                is Card.Car ->
                    Image(
                        painter = painterResource(card.image),
                        contentDescription = card.color.name.lowercase(),
                        modifier = Modifier.size(48.dp)
                    )

                is Card.Loco ->
                    Image(
                        painter = painterResource(MR.images.cardLoco),
                        contentDescription = "Loco (can be used as a card of any color)",
                        modifier = Modifier.size(48.dp)
                    )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(count.toString(), fontWeight = FontWeight.Bold)
        }
    }
}

private val Card.Car.backgroundColor
    get() = when (color) {
        CardColor.BLACK -> color.color.copy(alpha = 0.2f)
        CardColor.WHITE -> color.color.copy(alpha = 0.8f)
        CardColor.BLUE -> Color(0xBB84A0FF)
        else -> color.color.copy(alpha = 0.3f)
    }

private val Card.Car.image get() = when (color) {
    CardColor.BLACK -> MR.images.cardBlack
    CardColor.RED -> MR.images.cardRed
    CardColor.GREEN -> MR.images.cardGreen
    CardColor.BLUE -> MR.images.cardBlue
    CardColor.WHITE -> MR.images.cardWhite
    CardColor.YELLOW -> MR.images.cardYellow
    CardColor.ORANGE -> MR.images.cardOrange
    CardColor.MAGENTO -> MR.images.cardMagento
}