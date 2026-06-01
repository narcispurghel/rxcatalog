package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.github.narcispurghel.rxcatalog.navigation.AppRoutes
import com.github.narcispurghel.rxcatalog.ui.components.splash.SplashHeroCard
import com.github.narcispurghel.rxcatalog.ui.session.DemoSessionState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    sessionState: DemoSessionState,
    onNavigate: (String) -> Unit,
) {
    LaunchedEffect(sessionState.isAuthenticated, sessionState.role) {
        delay(650.milliseconds)
        onNavigate(if (sessionState.isAuthenticated) AppRoutes.HOME else AppRoutes.LOGIN)
    }

    SplashHeroCard(
        title = "RxCatalog",
        subtitle = "Splash screen, route guards, and adaptive nav shell.",
        icon = Icons.AutoMirrored.Filled.Login,
    )
}
