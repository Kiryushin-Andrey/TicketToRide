package ticketToRide.composables.welcomeScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ticketToRide.LocalAppActions
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
actual fun ShowGameIdDialog(url: String, onStartGame: () -> Unit) {
    val appActions = LocalAppActions.current
    LaunchedEffect(url, appActions) {
        val content = StringSelection(url)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(content, content)
        appActions.onEnter(onStartGame)
    }
    val link = remember(url) {
        buildAnnotatedString {
            append(url)
            addStyle(
                style = SpanStyle(
                    color = Color.Blue,
                    textDecoration = TextDecoration.Underline
                ),
                start = 0,
                end = url.length
            )
            addStringAnnotation(tag = "URL", annotation = url, start = 0, end = url.length)
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Text("Send this link to other players (it is already in your clipboard)")
        Text(link)
        Button(
            modifier = Modifier.align(Alignment.End),
            onClick = onStartGame
        ) {
            Text("OK")
        }
    }
}
