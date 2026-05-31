package com.github.narcispurghel.rxcatalog.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = MedicalTeal80,
        secondary = MedicalBlue80,
        tertiary = MedicalAmber80,
        background = Color(0xFF111318),
        surface = Color(0xFF111318),
        onPrimary = Color(0xFF003733),
        onSecondary = Color(0xFF0C1A45),
        onTertiary = Color(0xFF3F2F00),
    )

private val LightColorScheme =
    lightColorScheme(
        primary = MedicalTeal40,
        secondary = MedicalBlue40,
        tertiary = MedicalAmber40,
        background = Color(0xFFF8FBFA),
        surface = Color(0xFFF8FBFA),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
    )

@Composable
fun RxCatalogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> {
                DarkColorScheme
            }

            else -> {
                LightColorScheme
            }
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
