package ticketToRide

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val colorScheme = lightColorScheme().copy(
    primary = Color(0xFF825500),
    onPrimary = Color(0xFFFFFFFF),
//    primaryContainer = Color(0xFFFFDDB3),
//    onPrimaryContainer = Color(0xFF291800),
    secondary = Color(0xFF705B40),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFBDEBB),
    onSecondaryContainer = Color(0xFF271904),
    tertiary = Color(0xFF51643F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD4EABB),
    onTertiaryContainer = Color(0xFF102003),
//    error = Color(0xFFBA1A1A),
//    onError = Color(0xFFFFFFFF),
//    errorContainer = Color(0xFFFFDAD6),
//    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF817567),
//    background = Color(0xFFFFFBFF),
//    onBackground = Color(0xFF1F1B16),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F1B16),
    surfaceVariant = Color(0xFFF0E0CF),
    onSurfaceVariant = Color(0xFF4F4539),
    inverseSurface = Color(0xFF34302A),
    inverseOnSurface = Color(0xFFF9EFE7),
    inversePrimary = Color(0xFFFFB952),
    surfaceTint = Color(0xFF825500),
    outlineVariant = Color(0xFFD3C4B4),
//    scrim = Color(0xFF000000),
)

val ColorScheme.successContainer get() = Color(0xFF90EE90)

val PlayerColor.color
    get() = Color(this.value + 0xFF000000)

val CardColor.color
    get() = Color(this.value + 0xFF000000)
