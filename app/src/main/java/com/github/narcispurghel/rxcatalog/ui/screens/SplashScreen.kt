package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.github.narcispurghel.rxcatalog.auth.SessionState
import com.github.narcispurghel.rxcatalog.navigation.AppRoutes
import com.github.narcispurghel.rxcatalog.ui.components.splash.SplashHeroCard
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    sessionState: SessionState,
    onNavigate: (String) -> Unit,
) {
    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.Loading) {
            return@LaunchedEffect
        }

        delay(650.milliseconds)
        onNavigate(
            if (sessionState is SessionState.Authenticated) {
                AppRoutes.HOME
            } else {
                AppRoutes.LOGIN
            },
        )
    }

    SplashHeroCard(
        title = "RxCatalog",
        subtitle = "Checking your session and routing you to the right entry point.",
        icon = Icons.AutoMirrored.Filled.Login,
    )
}
