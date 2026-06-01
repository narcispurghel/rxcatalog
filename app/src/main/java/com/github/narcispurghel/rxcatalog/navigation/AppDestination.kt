package com.github.narcispurghel.rxcatalog.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.narcispurghel.rxcatalog.common.UserRole

data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val allowedRoles: Set<UserRole> = emptySet(),
) {
    fun isVisibleFor(role: UserRole?): Boolean = allowedRoles.isEmpty() || role in allowedRoles
}

val authenticatedDestinations =
    listOf(
        TopLevelDestination(
            route = AppRoutes.HOME,
            label = "Home",
            icon = Icons.Default.Home,
        ),
        TopLevelDestination(
            route = AppRoutes.SEARCH,
            label = "Search",
            icon = Icons.Default.Search,
        ),
        TopLevelDestination(
            route = AppRoutes.MY_SUBMISSIONS,
            label = "Submissions",
            icon = Icons.AutoMirrored.Filled.Assignment,
        ),
        TopLevelDestination(
            route = AppRoutes.PENDING_APPROVALS,
            label = "Approvals",
            icon = Icons.AutoMirrored.Filled.FactCheck,
            allowedRoles = setOf(UserRole.DOCTOR, UserRole.PHARMACIST),
        ),
        TopLevelDestination(
            route = AppRoutes.PROFILE,
            label = "Profile",
            icon = Icons.Default.AccountCircle,
        ),
    )
