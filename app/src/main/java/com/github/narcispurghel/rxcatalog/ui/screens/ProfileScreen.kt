@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.auth.AuthenticatedUser
import com.github.narcispurghel.rxcatalog.auth.SessionState
import com.github.narcispurghel.rxcatalog.common.UserRole
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChip
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChipTone

@Composable
fun ProfileScreen(
    sessionState: SessionState,
    currentUser: AuthenticatedUser?,
    isLoggingOut: Boolean,
    logoutError: String?,
    onDismissLogoutError: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Profile") })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ProfileHeroCard(
                    currentUser = currentUser,
                    sessionState = sessionState,
                )
            }
            item {
                AccountDetailsCard(
                    currentUser = currentUser,
                    sessionState = sessionState,
                )
            }
            item {
                AccessCard(role = currentUser?.role)
            }
            if (logoutError != null) {
                item {
                    LogoutErrorCard(
                        message = logoutError,
                        onDismiss = onDismissLogoutError,
                    )
                }
            }
            item {
                SessionActionsCard(
                    isLoggingOut = isLoggingOut,
                    onLogout = onLogout,
                )
            }
        }
    }
}

@Composable
private fun ProfileHeroCard(
    currentUser: AuthenticatedUser?,
    sessionState: SessionState,
) {
    val isAuthenticated = sessionState is SessionState.Authenticated
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = currentUser?.displayName.toInitials(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = currentUser?.displayName ?: "Guest profile",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = currentUser?.email ?: "No account email available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatusChip(
                        label = if (isAuthenticated) "Signed in" else "Not signed in",
                        tone =
                            if (isAuthenticated) {
                                StatusChipTone.APPROVED
                            } else {
                                StatusChipTone.DRAFT
                            },
                    )
                    StatusChip(
                        label = currentUser?.role?.toProfileRoleLabel() ?: "No role",
                        tone = currentUser?.role.toProfileTone(),
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountDetailsCard(
    currentUser: AuthenticatedUser?,
    sessionState: SessionState,
) {
    ProfileSectionCard(
        title = "Account details",
        subtitle = "Identity and session information currently active on this device.",
    ) {
        ProfileInfoRow(
            icon = Icons.Filled.AccountCircle,
            label = "Display name",
            value = currentUser?.displayName ?: "Unavailable",
        )
        ProfileInfoRow(
            icon = Icons.Outlined.Email,
            label = "Email",
            value = currentUser?.email ?: "Unavailable",
        )
        ProfileInfoRow(
            icon = Icons.Filled.Security,
            label = "Session status",
            value = sessionState.toStatusLabel(),
        )
    }
}

@Composable
private fun AccessCard(role: UserRole?) {
    val accessSummary =
        when (role) {
            UserRole.DOCTOR -> "Reviewer tools are enabled for medical approval workflows."
            UserRole.PHARMACIST -> "Reviewer tools are enabled for medicine leaflet validation."
            UserRole.USER -> "Catalog browsing and leaflet submissions are enabled."
            null -> "Sign in to unlock catalog browsing and submission workflows."
        }
    ProfileSectionCard(
        title = "Access level",
        subtitle = accessSummary,
    ) {
        AccessCapabilityRow(
            icon = Icons.Filled.Search,
            label = "Catalog search",
            enabled = role != null,
        )
        AccessCapabilityRow(
            icon = Icons.Filled.MedicalServices,
            label = "Leaflet submissions",
            enabled = role != null,
        )
        AccessCapabilityRow(
            icon = Icons.Filled.VerifiedUser,
            label = "Review approvals",
            enabled = role in setOf(UserRole.DOCTOR, UserRole.PHARMACIST),
        )
    }
}

@Composable
private fun SessionActionsCard(
    isLoggingOut: Boolean,
    onLogout: () -> Unit,
) {
    ProfileSectionCard(
        title = "Session",
        subtitle = "End this device session when you are finished.",
    ) {
        Button(
            onClick = onLogout,
            enabled = !isLoggingOut,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        ) {
            if (isLoggingOut) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(if (isLoggingOut) "Signing out..." else "Sign out")
        }
    }
}

@Composable
private fun LogoutErrorCard(
    message: String,
    onDismiss: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors =
            CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            TextButton(
                onClick = onDismiss,
                colors =
                    ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
            ) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(icon = icon)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AccessCapabilityRow(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(
            icon = icon,
            containerColor =
                if (enabled) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            contentColor =
                if (enabled) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        StatusChip(
            label = if (enabled) "Enabled" else "Locked",
            tone = if (enabled) StatusChipTone.APPROVED else StatusChipTone.DRAFT,
        )
    }
}

@Composable
private fun IconBadge(
    icon: ImageVector,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
            )
        }
    }
}

private fun SessionState.toStatusLabel(): String =
    when (this) {
        is SessionState.Authenticated -> "Authenticated"
        SessionState.Loading -> "Loading"
        SessionState.Unauthenticated -> "Unauthenticated"
    }

private fun String?.toInitials(): String {
    val parts =
        this
            ?.trim()
            ?.split(Regex("\\s+"))
            ?.filter { it.isNotBlank() }
            .orEmpty()
    return parts
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .joinToString("")
        .ifBlank { "RX" }
}

private fun UserRole.toProfileRoleLabel(): String =
    when (this) {
        UserRole.USER -> "User access"
        UserRole.DOCTOR -> "Doctor reviewer"
        UserRole.PHARMACIST -> "Pharmacist reviewer"
    }

private fun UserRole?.toProfileTone(): StatusChipTone =
    when (this) {
        UserRole.DOCTOR,
        UserRole.PHARMACIST,
        -> StatusChipTone.REVIEWER

        UserRole.USER -> StatusChipTone.DRAFT
        null -> StatusChipTone.DRAFT
    }
