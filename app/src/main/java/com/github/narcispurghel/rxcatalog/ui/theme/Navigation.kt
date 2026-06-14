package com.github.narcispurghel.rxcatalog.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.runtime.Composable

@Composable
fun rxCatalogNavigationSuiteColors(): NavigationSuiteColors =
    NavigationSuiteDefaults.colors(
        navigationBarContainerColor = MaterialTheme.colorScheme.surface,
        navigationBarContentColor = MaterialTheme.colorScheme.onSurface,
        navigationRailContainerColor = MaterialTheme.colorScheme.surface,
        navigationRailContentColor = MaterialTheme.colorScheme.onSurface,
        navigationDrawerContainerColor = MaterialTheme.colorScheme.surface,
        navigationDrawerContentColor = MaterialTheme.colorScheme.onSurface,
    )

@Composable
fun rxCatalogNavigationSuiteItemColors(): NavigationSuiteItemColors =
    NavigationSuiteDefaults.itemColors(
        navigationBarItemColors =
            NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        navigationRailItemColors =
            NavigationRailItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        navigationDrawerItemColors =
            NavigationDrawerItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
    )
