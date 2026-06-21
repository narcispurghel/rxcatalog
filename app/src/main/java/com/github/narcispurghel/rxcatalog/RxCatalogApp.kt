@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.github.narcispurghel.rxcatalog.auth.SessionState
import com.github.narcispurghel.rxcatalog.auth.SessionViewModel
import com.github.narcispurghel.rxcatalog.navigation.AppNavHost
import com.github.narcispurghel.rxcatalog.navigation.authenticatedDestinations
import com.github.narcispurghel.rxcatalog.navigation.isTopLevelRouteSelected
import com.github.narcispurghel.rxcatalog.navigation.navigateTopLevel
import com.github.narcispurghel.rxcatalog.ui.theme.RxCatalogTheme
import com.github.narcispurghel.rxcatalog.ui.theme.rxCatalogNavigationSuiteColors
import com.github.narcispurghel.rxcatalog.ui.theme.rxCatalogNavigationSuiteItemColors

@Preview(showSystemUi = true)
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
		val selectedTopLevelRoute =
			authenticatedDestinations
				.firstOrNull { currentRoute.isTopLevelRouteSelected(it.route) }
				?.route
		val navigationSuiteColors = rxCatalogNavigationSuiteColors()
		val navigationItemColors = rxCatalogNavigationSuiteItemColors()

		Scaffold(
			containerColor = MaterialTheme.colorScheme.background,
			snackbarHost = { SnackbarHost(snackbarHostState) },
		) { outerPadding ->
			Box(
				modifier =
					Modifier
						.fillMaxSize()
						.padding(outerPadding),
			) {
				if (sessionState is SessionState.Authenticated) {
					NavigationSuiteScaffold(
						navigationSuiteColors = navigationSuiteColors,
						navigationSuiteItems = {
							authenticatedDestinations
								.filter { it.isVisibleFor(currentUser?.role) }
								.forEach { destination ->
									val isSelected = selectedTopLevelRoute == destination.route
									item(
										icon = {
											Icon(
												imageVector = destination.icon,
												contentDescription = null,
											)
										},
										label = { Text(destination.label) },
										selected = isSelected,
										colors = navigationItemColors,
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
