@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.narcispurghel.rxcatalog.auth.AuthNavigationEvent
import com.github.narcispurghel.rxcatalog.auth.AuthenticatedUser
import com.github.narcispurghel.rxcatalog.auth.LoginViewModel
import com.github.narcispurghel.rxcatalog.auth.RegisterViewModel
import com.github.narcispurghel.rxcatalog.auth.SessionState
import com.github.narcispurghel.rxcatalog.auth.SessionViewModel
import com.github.narcispurghel.rxcatalog.common.UserRole
import com.github.narcispurghel.rxcatalog.navigation.AppRoutes
import com.github.narcispurghel.rxcatalog.navigation.TopLevelDestination
import com.github.narcispurghel.rxcatalog.navigation.authenticatedDestinations
import com.github.narcispurghel.rxcatalog.ui.screens.AuthScreen
import com.github.narcispurghel.rxcatalog.ui.screens.HomeScreen
import com.github.narcispurghel.rxcatalog.ui.screens.LeafletDetailsScreen
import com.github.narcispurghel.rxcatalog.ui.screens.MedicineDetailsScreen
import com.github.narcispurghel.rxcatalog.ui.screens.MySubmissionsScreen
import com.github.narcispurghel.rxcatalog.ui.screens.PendingApprovalsScreen
import com.github.narcispurghel.rxcatalog.ui.screens.ProfileScreen
import com.github.narcispurghel.rxcatalog.ui.screens.ReviewSubmissionScreen
import com.github.narcispurghel.rxcatalog.ui.screens.SearchScreen
import com.github.narcispurghel.rxcatalog.ui.screens.SplashScreen
import com.github.narcispurghel.rxcatalog.ui.screens.SubmitLeafletScreen
import com.github.narcispurghel.rxcatalog.ui.viewmodels.MySubmissionsViewModel
import com.github.narcispurghel.rxcatalog.ui.viewmodels.PendingApprovalsViewModel
import com.github.narcispurghel.rxcatalog.ui.viewmodels.SearchViewModel
import com.github.narcispurghel.rxcatalog.ui.theme.RxCatalogTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RxCatalogApp() {
    RxCatalogTheme {
        val navController = rememberNavController()
        val snackbarHostState = remember { SnackbarHostState() }
        val sessionViewModel: SessionViewModel = hiltViewModel()
        val sessionUiState = sessionViewModel.uiState
        val sessionState = sessionUiState.sessionState
        val currentUser = sessionUiState.currentUser
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { outerPadding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(outerPadding),
            ) {
                if (sessionState is SessionState.Authenticated) {
                    NavigationSuiteScaffold(
                        navigationSuiteItems = {
                            authenticatedDestinations
                                .filter { it.isVisibleFor(currentUser?.role) }
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
                                        onClick = { navController.navigateTopLevel(destination) },
                                    )
                                }
                        },
                    ) {
                        AppNavHost(
                            navController = navController,
                            snackbarHostState = snackbarHostState,
                            sessionState = sessionState,
                            currentUser = currentUser,
                            sessionViewModel = sessionViewModel,
                        )
                    }
                } else {
                    AppNavHost(
                        navController = navController,
                        snackbarHostState = snackbarHostState,
                        sessionState = sessionState,
                        currentUser = currentUser,
                        sessionViewModel = sessionViewModel,
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
    sessionState: SessionState,
    currentUser: AuthenticatedUser?,
    sessionViewModel: SessionViewModel,
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
                val viewModel: LoginViewModel = hiltViewModel()
                LaunchedEffect(viewModel) {
                    viewModel.events.collectLatest { event ->
                        if (event is AuthNavigationEvent.NavigateHome) {
                            navController.navigateSingleTop(AppRoutes.HOME)
                        }
                    }
                }
                AuthScreen(
                    state = viewModel.uiState,
                    onDisplayNameChanged = viewModel::onDisplayNameChanged,
                    onEmailChanged = viewModel::onEmailChanged,
                    onPasswordChanged = viewModel::onPasswordChanged,
                    onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
                    onRoleSelected = viewModel::onRoleSelected,
                    onSubmit = viewModel::submit,
                    onSwitch = { navController.navigateSingleTop(AppRoutes.REGISTER) },
                    onDismissError = viewModel::clearSubmitError,
                )
            }
        }

        composable(AppRoutes.REGISTER) {
            EnsureUnauthenticated(navController, snackbarHostState, sessionState) {
                val viewModel: RegisterViewModel = hiltViewModel()
                LaunchedEffect(viewModel) {
                    viewModel.events.collectLatest { event ->
                        if (event is AuthNavigationEvent.NavigateHome) {
                            navController.navigateSingleTop(AppRoutes.HOME)
                        }
                    }
                }
                AuthScreen(
                    state = viewModel.uiState,
                    onDisplayNameChanged = viewModel::onDisplayNameChanged,
                    onEmailChanged = viewModel::onEmailChanged,
                    onPasswordChanged = viewModel::onPasswordChanged,
                    onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
                    onRoleSelected = viewModel::onRoleSelected,
                    onSubmit = viewModel::submit,
                    onSwitch = { navController.navigateSingleTop(AppRoutes.LOGIN) },
                    onDismissError = viewModel::clearSubmitError,
                )
            }
        }

        composable(AppRoutes.HOME) {
            EnsureAuthenticated(navController, snackbarHostState, sessionState) {
                HomeScreen(
                    sessionState = sessionState,
                    currentUser = currentUser,
                    onSearch = { navController.navigateSingleTop(AppRoutes.searchRoute("aspirin")) },
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
        ) {
            EnsureAuthenticated(navController, snackbarHostState, sessionState) {
                val viewModel: SearchViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                SearchScreen(
                    state = uiState,
                    onQueryChanged = viewModel::onQueryChanged,
                    onMedicine = { medicineId ->
                        navController.navigateSingleTop(AppRoutes.medicineRoute(medicineId))
                    },
                    onSubmit = { navController.navigateSingleTop(AppRoutes.submitRoute()) },
                )
            }
        }

        composable(
            route = AppRoutes.MEDICINE,
            arguments = listOf(navArgument("medicineId") { type = NavType.StringType }),
            deepLinks =
                listOf(
                    navDeepLink { uriPattern = AppRoutes.MEDICINE_DEEP_LINK },
                    navDeepLink { uriPattern = AppRoutes.WEB_MEDICINE_DEEP_LINK },
                ),
        ) { entry ->
            EnsureAuthenticated(navController, snackbarHostState, sessionState) {
                val medicineId = entry.stringArgument("medicineId")
                if (medicineId == null) {
                    InvalidRouteRedirect(
                        navController = navController,
                        snackbarHostState = snackbarHostState,
                        message = "Unable to open that medicine.",
                        fallbackRoute = AppRoutes.searchRoute(),
                    )
                } else {
                    MedicineDetailsScreen(
                        medicineId = medicineId,
                        onOpenLeaflet = { leafletId ->
                            navController.navigateSingleTop(AppRoutes.leafletRoute(leafletId))
                        },
                        onSubmit = { selectedMedicineId ->
                            navController.navigateSingleTop(
                                AppRoutes.submitRoute(medicineId = selectedMedicineId),
                            )
                        },
                    )
                }
            }
        }

        composable(
            route = AppRoutes.LEAFLET,
            arguments = listOf(navArgument("leafletId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = AppRoutes.LEAFLET_DEEP_LINK }),
        ) { entry ->
            EnsureAuthenticated(navController, snackbarHostState, sessionState) {
                val leafletId = entry.stringArgument("leafletId")
                if (leafletId == null) {
                    InvalidRouteRedirect(
                        navController = navController,
                        snackbarHostState = snackbarHostState,
                        message = "Unable to open that leaflet.",
                        fallbackRoute = AppRoutes.searchRoute(),
                    )
                } else {
                    LeafletDetailsScreen(leafletId = leafletId)
                }
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
            deepLinks = listOf(navDeepLink { uriPattern = AppRoutes.SUBMISSION_DEEP_LINK }),
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
                val viewModel: MySubmissionsViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                MySubmissionsScreen(
                    state = uiState,
                    onEdit = { submissionId ->
                        navController.navigateSingleTop(AppRoutes.submitRoute(submissionId = submissionId))
                    },
                )
            }
        }

        composable(AppRoutes.PENDING_APPROVALS) {
            EnsureReviewerAccess(navController, snackbarHostState, sessionState) {
                val viewModel: PendingApprovalsViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                PendingApprovalsScreen(
                    state = uiState,
                    onReview = { submissionId ->
                        navController.navigateSingleTop(AppRoutes.reviewRoute(submissionId))
                    },
                )
            }
        }

        composable(
            route = AppRoutes.REVIEW,
            arguments = listOf(navArgument("submissionId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = AppRoutes.REVIEW_DEEP_LINK }),
        ) { entry ->
            EnsureReviewerAccess(navController, snackbarHostState, sessionState) {
                val submissionId = entry.stringArgument("submissionId")
                if (submissionId == null) {
                    InvalidRouteRedirect(
                        navController = navController,
                        snackbarHostState = snackbarHostState,
                        message = "Unable to open that review.",
                        fallbackRoute = AppRoutes.PENDING_APPROVALS,
                    )
                } else {
                    ReviewSubmissionScreen(submissionId = submissionId)
                }
            }
        }

        composable(AppRoutes.PROFILE) {
            EnsureAuthenticated(navController, snackbarHostState, sessionState) {
                ProfileScreen(
                    sessionState = sessionState,
                    currentUser = currentUser,
                    isLoggingOut = sessionViewModel.uiState.isLoggingOut,
                    logoutError = sessionViewModel.uiState.logoutError,
                    onDismissLogoutError = sessionViewModel::clearLogoutError,
                    onLogout = sessionViewModel::logout,
                )
            }
        }
    }
}

@Composable
private fun InvalidRouteRedirect(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    message: String,
    fallbackRoute: String,
) {
    LaunchedEffect(navController, fallbackRoute, message) {
        snackbarHostState.showSnackbar(message)
        navController.navigateSingleTop(fallbackRoute)
    }
}

@Composable
private fun EnsureUnauthenticated(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    sessionState: SessionState,
    content: @Composable () -> Unit,
) {
    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.Authenticated) {
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
    sessionState: SessionState,
    content: @Composable () -> Unit,
) {
    LaunchedEffect(sessionState) {
        if (sessionState == SessionState.Unauthenticated) {
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
    sessionState: SessionState,
    content: @Composable () -> Unit,
) {
    LaunchedEffect(sessionState) {
        when (sessionState) {
            SessionState.Loading -> Unit
            SessionState.Unauthenticated -> {
                snackbarHostState.showSnackbar("Sign in to continue.")
                navController.navigateSingleTop(AppRoutes.LOGIN)
            }

            is SessionState.Authenticated -> {
                if (sessionState.user.role !in setOf(UserRole.DOCTOR, UserRole.PHARMACIST)) {
                    snackbarHostState.showSnackbar(
                        "This route is restricted to doctors and pharmacists.",
                    )
                    navController.navigateSingleTop(AppRoutes.HOME)
                }
            }
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
    arguments?.getString(name)?.takeIf { it.isNotBlank() }

private fun String?.isTopLevelRouteSelected(destinationRoute: String): Boolean {
    if (this == null) return false
    val normalized = destinationRoute.substringBefore("?")
    return this == destinationRoute || this.startsWith(normalized)
}
