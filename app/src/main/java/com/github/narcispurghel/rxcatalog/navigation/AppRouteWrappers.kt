package com.github.narcispurghel.rxcatalog.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import com.github.narcispurghel.rxcatalog.navigation.TopLevelDestination
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
import com.github.narcispurghel.rxcatalog.ui.viewmodels.LeafletDetailsViewModel
import com.github.narcispurghel.rxcatalog.ui.viewmodels.MedicineDetailsViewModel
import com.github.narcispurghel.rxcatalog.ui.viewmodels.MySubmissionsViewModel
import com.github.narcispurghel.rxcatalog.ui.viewmodels.PendingApprovalsViewModel
import com.github.narcispurghel.rxcatalog.ui.viewmodels.SearchViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import androidx.compose.ui.Modifier

@Composable
fun AppNavHost(
    navController: NavHostController,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
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
            SplashRoute(sessionState = sessionState, navController = navController)
        }
        composable(AppRoutes.LOGIN) {
            LoginRoute(navController, snackbarHostState, sessionState)
        }
        composable(AppRoutes.REGISTER) {
            RegisterRoute(navController, snackbarHostState, sessionState)
        }
        composable(AppRoutes.HOME) {
            HomeRoute(navController, snackbarHostState, sessionState, currentUser)
        }
        composable(
            route = AppRoutes.SEARCH,
            arguments = listOf(navArgument("query") { type = NavType.StringType; defaultValue = "" }),
        ) {
            SearchRoute(navController, snackbarHostState, sessionState)
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
            MedicineDetailsRoute(navController, snackbarHostState, sessionState, entry)
        }
        composable(
            route = AppRoutes.LEAFLET,
            arguments = listOf(navArgument("leafletId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = AppRoutes.LEAFLET_DEEP_LINK }),
        ) { entry ->
            LeafletDetailsRoute(navController, snackbarHostState, sessionState, entry)
        }
        composable(
            route = AppRoutes.SUBMIT,
            arguments =
                listOf(
                    navArgument("submissionId") { type = NavType.StringType; defaultValue = "" },
                    navArgument("medicineId") { type = NavType.StringType; defaultValue = "" },
                ),
            deepLinks = listOf(navDeepLink { uriPattern = AppRoutes.SUBMISSION_DEEP_LINK }),
        ) { entry ->
            SubmitLeafletRoute(navController, snackbarHostState, sessionState, entry)
        }
        composable(AppRoutes.MY_SUBMISSIONS) {
            MySubmissionsRoute(navController, snackbarHostState, sessionState)
        }
        composable(AppRoutes.PENDING_APPROVALS) {
            PendingApprovalsRoute(navController, snackbarHostState, sessionState)
        }
        composable(
            route = AppRoutes.REVIEW,
            arguments = listOf(navArgument("submissionId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = AppRoutes.REVIEW_DEEP_LINK }),
        ) { entry ->
            ReviewSubmissionRoute(navController, snackbarHostState, sessionState, entry)
        }
        composable(AppRoutes.PROFILE) {
            ProfileRoute(navController, snackbarHostState, sessionState, sessionViewModel, currentUser)
        }
    }
}

@Composable
private fun SplashRoute(sessionState: SessionState, navController: NavController) {
    SplashScreen(sessionState = sessionState, onNavigate = { route -> navController.navigateSingleTop(route) })
}

@Composable
private fun LoginRoute(
    navController: NavController,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    sessionState: SessionState,
) {
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

@Composable
private fun RegisterRoute(
    navController: NavController,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    sessionState: SessionState,
) {
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

@Composable
private fun HomeRoute(
    navController: NavController,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    sessionState: SessionState,
    currentUser: AuthenticatedUser?,
) {
    EnsureAuthenticated(navController, snackbarHostState, sessionState) {
        HomeScreen(
            sessionState = sessionState,
            currentUser = currentUser,
            onSearch = { navController.navigateSingleTop(AppRoutes.searchRoute("aspirin")) },
            onSubmit = { navController.navigateSingleTop(AppRoutes.submitRoute()) },
            onApprovals = { navController.navigateSingleTop(AppRoutes.PENDING_APPROVALS) },
            onProfile = { navController.navigateSingleTop(AppRoutes.PROFILE) },
            onMedicine = { medicineId -> navController.navigateSingleTop(AppRoutes.medicineRoute(medicineId)) },
        )
    }
}

@Composable
private fun SearchRoute(
    navController: NavController,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    sessionState: SessionState,
) {
    EnsureAuthenticated(navController, snackbarHostState, sessionState) {
        val viewModel: SearchViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        SearchScreen(
            state = uiState,
            onQueryChanged = viewModel::onQueryChanged,
            onMedicine = { medicineId -> navController.navigateSingleTop(AppRoutes.medicineRoute(medicineId)) },
            onSubmit = { navController.navigateSingleTop(AppRoutes.submitRoute()) },
        )
    }
}

@Composable
private fun MedicineDetailsRoute(
    navController: NavController,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    sessionState: SessionState,
    entry: NavBackStackEntry,
) {
    EnsureAuthenticated(navController, snackbarHostState, sessionState) {
        val viewModel: MedicineDetailsViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
                state = uiState,
                onOpenLeaflet = {
                    uiState.medicine?.approvedLeaflet?.let { leaflet ->
                        navController.navigateSingleTop(AppRoutes.leafletRoute(leaflet.leafletId))
                    }
                },
                onSubmit = { navController.navigateSingleTop(AppRoutes.submitRoute(medicineId = medicineId)) },
            )
        }
    }
}

@Composable
private fun LeafletDetailsRoute(
    navController: NavController,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    sessionState: SessionState,
    entry: NavBackStackEntry,
) {
    EnsureAuthenticated(navController, snackbarHostState, sessionState) {
        val viewModel: LeafletDetailsViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val leafletId = entry.stringArgument("leafletId")
        if (leafletId == null) {
            InvalidRouteRedirect(
                navController = navController,
                snackbarHostState = snackbarHostState,
                message = "Unable to open that leaflet.",
                fallbackRoute = AppRoutes.searchRoute(),
            )
        } else {
            LeafletDetailsScreen(state = uiState)
        }
    }
}

@Composable
private fun SubmitLeafletRoute(
    navController: NavController,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    sessionState: SessionState,
    entry: NavBackStackEntry,
) {
    EnsureAuthenticated(navController, snackbarHostState, sessionState) {
        SubmitLeafletScreen(
            submissionId = entry.stringArgument("submissionId"),
            medicineId = entry.stringArgument("medicineId"),
        )
    }
}

@Composable
private fun MySubmissionsRoute(
    navController: NavController,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    sessionState: SessionState,
) {
    EnsureAuthenticated(navController, snackbarHostState, sessionState) {
        val viewModel: MySubmissionsViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        MySubmissionsScreen(
            state = uiState,
            onEdit = { submissionId -> navController.navigateSingleTop(AppRoutes.submitRoute(submissionId = submissionId)) },
        )
    }
}

@Composable
private fun PendingApprovalsRoute(
    navController: NavController,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    sessionState: SessionState,
) {
    EnsureReviewerAccess(navController, snackbarHostState, sessionState) {
        val viewModel: PendingApprovalsViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        PendingApprovalsScreen(
            state = uiState,
            onReview = { submissionId -> navController.navigateSingleTop(AppRoutes.reviewRoute(submissionId)) },
        )
    }
}

@Composable
private fun ReviewSubmissionRoute(
    navController: NavController,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    sessionState: SessionState,
    entry: NavBackStackEntry,
) {
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

@Composable
private fun ProfileRoute(
    navController: NavController,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    sessionState: SessionState,
    sessionViewModel: SessionViewModel,
    currentUser: AuthenticatedUser?,
) {
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

@Composable
private fun InvalidRouteRedirect(
    navController: NavController,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    message: String,
    fallbackRoute: String,
) {
    LaunchedEffect(navController, fallbackRoute, message) {
        navController.navigateSingleTop(fallbackRoute)
        withContext(NonCancellable) {
            snackbarHostState.showSnackbar(message)
        }
    }
}

@Composable
private fun EnsureUnauthenticated(
    navController: NavController,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    sessionState: SessionState,
    content: @Composable () -> Unit,
) {
    val navigateHome = rememberUpdatedState(newValue = { navController.navigateSingleTop(AppRoutes.HOME) })
    val isAuthenticated = sessionState is SessionState.Authenticated

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            navigateHome.value()
            withContext(NonCancellable) {
                snackbarHostState.showSnackbar("You are already signed in.")
            }
        }
    }

    if (!isAuthenticated) {
        content()
    }
}

@Composable
private fun EnsureAuthenticated(
    navController: NavController,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    sessionState: SessionState,
    content: @Composable () -> Unit,
) {
    val navigateLogin = rememberUpdatedState(newValue = { navController.navigateSingleTop(AppRoutes.LOGIN) })
    val isUnauthenticated = sessionState == SessionState.Unauthenticated

    LaunchedEffect(isUnauthenticated) {
        if (isUnauthenticated) {
            navigateLogin.value()
            withContext(NonCancellable) {
                snackbarHostState.showSnackbar("Sign in to continue.")
            }
        }
    }

    if (sessionState is SessionState.Authenticated) {
        content()
    }
}

@Composable
private fun EnsureReviewerAccess(
    navController: NavController,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    sessionState: SessionState,
    content: @Composable () -> Unit,
) {
    val navigateLogin = rememberUpdatedState(newValue = { navController.navigateSingleTop(AppRoutes.LOGIN) })
    val navigateHome = rememberUpdatedState(newValue = { navController.navigateSingleTop(AppRoutes.HOME) })
    val hasReviewerAccess =
        sessionState is SessionState.Authenticated &&
            sessionState.user.role in setOf(UserRole.DOCTOR, UserRole.PHARMACIST)

    LaunchedEffect(sessionState) {
        when (sessionState) {
            SessionState.Loading -> Unit
            SessionState.Unauthenticated -> {
                navigateLogin.value()
                withContext(NonCancellable) {
                    snackbarHostState.showSnackbar("Sign in to continue.")
                }
            }

            is SessionState.Authenticated -> {
                if (!hasReviewerAccess) {
                    navigateHome.value()
                    withContext(NonCancellable) {
                        snackbarHostState.showSnackbar(
                            "This route is restricted to doctors and pharmacists.",
                        )
                    }
                }
            }
        }
    }

    if (hasReviewerAccess) {
        content()
    } else if (sessionState == SessionState.Loading) {
        Unit
    }
}

private fun NavController.navigateSingleTop(
    route: String,
    builder: androidx.navigation.NavOptionsBuilder.() -> Unit = {},
) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.findStartDestination().id) {
            inclusive = true
            saveState = true
        }
        builder()
    }
}

fun NavController.navigateTopLevel(destination: TopLevelDestination) {
    navigateSingleTop(
        route =
            when (destination.route) {
                AppRoutes.SEARCH -> AppRoutes.searchRoute()
                else -> destination.route
            },
    )
}

fun String?.isTopLevelRouteSelected(destinationRoute: String): Boolean {
    if (this == null) return false
    val normalizedCurrentRoute = substringBefore("?")
    val normalizedDestinationRoute = destinationRoute.substringBefore("?")
    return normalizedCurrentRoute == normalizedDestinationRoute ||
        normalizedCurrentRoute.startsWith(normalizedDestinationRoute)
}

private fun NavBackStackEntry.stringArgument(name: String): String? =
    arguments?.getString(name)?.takeIf { it.isNotBlank() }
