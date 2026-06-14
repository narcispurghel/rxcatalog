package com.github.narcispurghel.rxcatalog.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFF60A5FA),
        onPrimary = RxBackgroundDark,
        primaryContainer = RxBlueDark,
        onPrimaryContainer = RxTextPrimaryDark,
        inversePrimary = RxBlue,
        secondary = Color(0xFF67E8F9),
        onSecondary = RxBackgroundDark,
        secondaryContainer = Color(0xFF0F3B4A),
        onSecondaryContainer = Color(0xFFD6F6FF),
        tertiary = RxSuccess,
        onTertiary = RxBackgroundDark,
        tertiaryContainer = RxSuccessContainerDark,
        onTertiaryContainer = Color(0xFFDCFCE7),
        error = Color(0xFFF87171),
        onError = RxBackgroundDark,
        errorContainer = RxErrorContainerDark,
        onErrorContainer = Color(0xFFFEE2E2),
        background = RxBackgroundDark,
        onBackground = RxTextPrimaryDark,
        surface = RxSurfaceDark,
        onSurface = RxTextPrimaryDark,
        surfaceVariant = RxSurfaceMutedDark,
        onSurfaceVariant = RxTextSecondaryDark,
        outline = RxBorderDark,
        outlineVariant = Color(0xFF1E293B),
        inverseSurface = RxSurfaceLight,
        inverseOnSurface = RxTextPrimaryLight,
        surfaceTint = Color(0xFF60A5FA),
        scrim = Color.Black,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = RxBlue,
        onPrimary = Color.White,
        primaryContainer = RxBlueSoft,
        onPrimaryContainer = RxBlueDark,
        inversePrimary = Color(0xFF60A5FA),
        secondary = RxCyan,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFCFFAFE),
        onSecondaryContainer = Color(0xFF0F3B4A),
        tertiary = RxSuccess,
        onTertiary = Color.White,
        tertiaryContainer = RxSuccessContainerLight,
        onTertiaryContainer = Color(0xFF14532D),
        error = RxError,
        onError = Color.White,
        errorContainer = RxErrorContainerLight,
        onErrorContainer = Color(0xFF7F1D1D),
        background = RxBackgroundLight,
        onBackground = RxTextPrimaryLight,
        surface = RxSurfaceLight,
        onSurface = RxTextPrimaryLight,
        surfaceVariant = RxSurfaceMutedLight,
        onSurfaceVariant = RxTextSecondaryLight,
        outline = RxBorderLight,
        outlineVariant = Color(0xFFCBD5E1),
        inverseSurface = RxSurfaceDark,
        inverseOnSurface = RxTextPrimaryDark,
        surfaceTint = RxBlue,
        scrim = Color.Black,
    )

@Composable
fun RxCatalogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme

            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes(),
        content = content,
    )
}
