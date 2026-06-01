@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.github.narcispurghel.rxcatalog.common.UserRole
import com.github.narcispurghel.rxcatalog.navigation.AppRoutes
import com.github.narcispurghel.rxcatalog.navigation.TopLevelDestination
import com.github.narcispurghel.rxcatalog.navigation.authenticatedDestinations
import com.github.narcispurghel.rxcatalog.ui.screens.*
import com.github.narcispurghel.rxcatalog.ui.session.DemoSessionState
import com.github.narcispurghel.rxcatalog.ui.theme.RxCatalogTheme

@Composable
fun RxCatalogApp() {
    RxCatalogTheme {
        val navController = rememberNavController()
        val snackbarHostState = remember { SnackbarHostState() }
        val sessionState = rememberDemoSessionState()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { outerPadding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(outerPadding),
            ) {
                if (sessionState.isAuthenticated) {
                    NavigationSuiteScaffold(
                        navigationSuiteItems = {
                            authenticatedDestinations
                                .filter { it.isVisibleFor(sessionState.role) }
                                .forEach { destination ->
                                    item(
                                        icon = {
                                            Icon(
                                                imageVector = destination.icon,
                                                contentDescription = destination.label,
                                            )
                                        },
                                        label = { Text(destination.label) },
                                        selected =
                                            currentRoute.isTopLevelRouteSelected(
                                                destination.route,
                                            ),
                                        onClick = {
                                            navController.navigateTopLevel(destination)
                                        },
                                    )
                                }
                        },
                    ) {
                        AppNavHost(
                            navController = navController,
                            snackbarHostState = snackbarHostState,
                            sessionState = sessionState,
                        )
                    }
                } else {
                    AppNavHost(
                        navController = navController,
                        snackbarHostState = snackbarHostState,
                        sessionState = sessionState,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    sessionState: DemoSessionState,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.SPLASH,
        modifier = modifier.fillMaxSize(),
    ) {
        composable(AppRoutes.SPLASH) {
            SplashScreen(
                sessionState = sessionState,
                onNavigate = { route -> navController.navigateSingleTop(route) },
            )
        }

        composable(AppRoutes.LOGIN) {
            EnsureUnauthenticated(navController, snackbarHostState, sessionState) {
                AuthScreen(
                    title = "Welcome back",
                    subtitle = "Route shell with splash, guards, and adaptive navigation.",
                    primaryAction = "Sign in as user",
                    secondaryAction = "Sign in as doctor",
                    tertiaryAction = "Sign in as pharmacist",
                    icon = Icons.AutoMirrored.Filled.Login,
                    onPrimary = {
                        sessionState.signIn(UserRole.USER)
                        navController.navigateSingleTop(AppRoutes.HOME)
                    },
                    onSecondary = {
                        sessionState.signIn(UserRole.DOCTOR)
                        navController.navigateSingleTop(AppRoutes.HOME)
                    },
                    onTertiary = {
                        sessionState.signIn(UserRole.PHARMACIST)
                        navController.navigateSingleTop(AppRoutes.HOME)
                    },
                    onSwitch = { navController.navigateSingleTop(AppRoutes.REGISTER) },
                    switchLabel = "Switch to register",
                )
            }
        }

        composable(AppRoutes.REGISTER) {
            EnsureUnauthenticated(navController, snackbarHostState, sessionState) {
                AuthScreen(
                    title = "Create account",
                    subtitle = "Create a new account to continue.",
                    primaryAction = "Create user account",
                    secondaryAction = "Create doctor account",
                    tertiaryAction = "Create pharmacist account",
                    icon = Icons.Filled.PersonAdd,
                    onPrimary = {
                        sessionState.signIn(UserRole.USER)
                        navController.navigateSingleTop(AppRoutes.HOME)
                    },
                    onSecondary = {
                        sessionState.signIn(UserRole.DOCTOR)
                        navController.navigateSingleTop(AppRoutes.HOME)
                    },
                    onTertiary = {
                        sessionState.signIn(UserRole.PHARMACIST)
                        navController.navigateSingleTop(AppRoutes.HOME)
                    },
                    onSwitch = { navController.popBackStack() },
                    switchLabel = "Back to login",
                )
            }
        }

        composable(AppRoutes.HOME) {
            EnsureAuthenticated(navController, snackbarHostState, sessionState) {
                HomeScreen(
                    sessionState = sessionState,
                    onSearch = {
                        navController.navigateSingleTop(
                            AppRoutes.searchRoute("aspirin"),
                        )
                    },
                    onSubmit = { navController.navigateSingleTop(AppRoutes.submitRoute()) },
                    onApprovals = { navController.navigateSingleTop(AppRoutes.PENDING_APPROVALS) },
                    onProfile = { navController.navigateSingleTop(AppRoutes.PROFILE) },
                    onMedicine = { medicineId ->
                        navController.navigateSingleTop(AppRoutes.medicineRoute(medicineId))
                    },
                )
            }
        }

        composable(
            route = AppRoutes.SEARCH,
            arguments =
                listOf(
                    navArgument("query") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
        ) { entry ->
            EnsureAuthenticated(navController, snackbarHostState, sessionState) {
                SearchScreen(
                    query = entry.stringArgument("query"),
                    onMedicine = { medicineId ->
                        navController.navigateSingleTop(AppRoutes.medicineRoute(medicineId))
                    },
                    onSubmit = { navController.navigateSingleTop(AppRoutes.submitRoute()) },
                )
            }
        }

        composable(
            route = AppRoutes.MEDICINE,
            arguments =
                listOf(
                    navArgument("medicineId") { type = NavType.StringType },
                ),
            deepLinks =
                listOf(
                    navDeepLink { uriPattern = AppRoutes.MEDICINE_DEEP_LINK },
                    navDeepLink { uriPattern = AppRoutes.WEB_MEDICINE_DEEP_LINK },
                ),
        ) { entry ->
            EnsureAuthenticated(navController, snackbarHostState, sessionState) {
                MedicineDetailsScreen(
                    medicineId = entry.requireStringArgument("medicineId"),
                    onOpenLeaflet = { leafletId ->
                        navController.navigateSingleTop(AppRoutes.leafletRoute(leafletId))
                    },
                    onSubmit = { medicineId ->
                        navController.navigateSingleTop(
                            AppRoutes.submitRoute(medicineId = medicineId),
                        )
                    },
                )
            }
        }

        composable(
            route = AppRoutes.LEAFLET,
            arguments =
                listOf(
                    navArgument("leafletId") { type = NavType.StringType },
                ),
            deepLinks =
                listOf(
                    navDeepLink { uriPattern = AppRoutes.LEAFLET_DEEP_LINK },
                ),
        ) { entry ->
            EnsureAuthenticated(navController, snackbarHostState, sessionState) {
                LeafletDetailsScreen(leafletId = entry.requireStringArgument("leafletId"))
            }
        }

        composable(
            route = AppRoutes.SUBMIT,
            arguments =
                listOf(
                    navArgument("submissionId") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("medicineId") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            deepLinks =
                listOf(
                    navDeepLink { uriPattern = AppRoutes.SUBMISSION_DEEP_LINK },
                ),
        ) { entry ->
            EnsureAuthenticated(navController, snackbarHostState, sessionState) {
                SubmitLeafletScreen(
                    submissionId = entry.stringArgument("submissionId"),
                    medicineId = entry.stringArgument("medicineId"),
                )
            }
        }

        composable(AppRoutes.MY_SUBMISSIONS) {
            EnsureAuthenticated(navController, snackbarHostState, sessionState) {
                MySubmissionsScreen(
                    onEdit = { submissionId ->
                        navController.navigateSingleTop(
                            AppRoutes.submitRoute(submissionId = submissionId),
                        )
                    },
                )
            }
        }

        composable(AppRoutes.PENDING_APPROVALS) {
            EnsureReviewerAccess(navController, snackbarHostState, sessionState) {
                PendingApprovalsScreen(
                    onReview = { submissionId ->
                        navController.navigateSingleTop(AppRoutes.reviewRoute(submissionId))
                    },
                )
            }
        }

        composable(
            route = AppRoutes.REVIEW,
            arguments =
                listOf(
                    navArgument("submissionId") { type = NavType.StringType },
                ),
            deepLinks =
                listOf(
                    navDeepLink { uriPattern = AppRoutes.SUBMISSION_DEEP_LINK },
                ),
        ) { entry ->
            EnsureReviewerAccess(navController, snackbarHostState, sessionState) {
                ReviewSubmissionScreen(submissionId = entry.requireStringArgument("submissionId"))
            }
        }

        composable(AppRoutes.PROFILE) {
            EnsureAuthenticated(navController, snackbarHostState, sessionState) {
                ProfileScreen(
                    sessionState = sessionState,
                    onLogout = {
                        sessionState.signOut()
                        navController.navigateSingleTop(AppRoutes.LOGIN) {
                            popUpTo(AppRoutes.SPLASH) { inclusive = false }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun EnsureUnauthenticated(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    sessionState: DemoSessionState,
    content: @Composable () -> Unit,
) {
    androidx.compose.runtime.LaunchedEffect(sessionState.isAuthenticated) {
        if (sessionState.isAuthenticated) {
            snackbarHostState.showSnackbar("You are already signed in.")
            navController.navigateSingleTop(AppRoutes.HOME)
        }
    }

    content()
}

@Composable
private fun EnsureAuthenticated(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    sessionState: DemoSessionState,
    content: @Composable () -> Unit,
) {
    androidx.compose.runtime.LaunchedEffect(sessionState.isAuthenticated) {
        if (!sessionState.isAuthenticated) {
            snackbarHostState.showSnackbar("Sign in to continue.")
            navController.navigateSingleTop(AppRoutes.LOGIN)
        }
    }

    content()
}

@Composable
private fun EnsureReviewerAccess(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    sessionState: DemoSessionState,
    content: @Composable () -> Unit,
) {
    androidx.compose.runtime.LaunchedEffect(sessionState.isAuthenticated, sessionState.role) {
        if (!sessionState.isAuthenticated) {
            snackbarHostState.showSnackbar("Sign in to continue.")
            navController.navigateSingleTop(AppRoutes.LOGIN)
            return@LaunchedEffect
        }

        if (sessionState.role !in setOf(UserRole.DOCTOR, UserRole.PHARMACIST)) {
            snackbarHostState.showSnackbar("This route is restricted to doctors and pharmacists.")
            navController.navigateSingleTop(AppRoutes.HOME)
        }
    }

    content()
}

private fun NavController.navigateSingleTop(
    route: String,
    builder: androidx.navigation.NavOptionsBuilder.() -> Unit = {},
) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        builder()
    }
}

private fun NavController.navigateTopLevel(destination: TopLevelDestination) {
    navigateSingleTop(
        route =
            when (destination.route) {
                AppRoutes.SEARCH -> AppRoutes.searchRoute()
                else -> destination.route
            },
    )
}

private fun NavBackStackEntry.stringArgument(name: String): String? =
    arguments?.getString(name)?.takeIf {
        it.isNotBlank()
    }

private fun NavBackStackEntry.requireStringArgument(name: String): String =
    requireNotNull(stringArgument(name)) {
        "Missing route argument: $name"
    }

private fun String?.isTopLevelRouteSelected(destinationRoute: String): Boolean {
    if (this == null) return false
    val normalized = destinationRoute.substringBefore("?")
    return this == destinationRoute || this.startsWith(normalized)
}

@Composable
private fun rememberDemoSessionState(): DemoSessionState =
    rememberSaveable(saver = DemoSessionState.Saver) {
        DemoSessionState(
            initialAuthenticated = false,
            initialRole = null,
        )
    }
