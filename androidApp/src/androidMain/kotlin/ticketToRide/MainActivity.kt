package org.akir.ticketToRide

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import ticketToRide.BuildKonfig
import ticketToRide.MainView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val gameId = intent?.data
            ?.takeIf {
                intent?.action == Intent.ACTION_VIEW
                        && it.host == BuildKonfig.SERVER_HOST
                        && it.pathSegments.firstOrNull() == "game" && it.pathSegments.size > 1
            }
            ?.pathSegments
            ?.get(1)

        setContent {
            MainView(gameId)
        }
    }
}
