@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.auth.AuthenticatedUser
import com.github.narcispurghel.rxcatalog.auth.SessionState
import com.github.narcispurghel.rxcatalog.common.UserRole
import com.github.narcispurghel.rxcatalog.catalog.CatalogSeedIds
import com.github.narcispurghel.rxcatalog.ui.components.common.MetadataRow
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChip
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChipTone

@Composable
fun HomeScreen(
    sessionState: SessionState,
    currentUser: AuthenticatedUser?,
    onSearch: () -> Unit,
    onSubmit: () -> Unit,
    onApprovals: () -> Unit,
    onProfile: () -> Unit,
    onMedicine: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("RxCatalog") })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Medicine catalog",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = "Search medicines, submit leaflet updates, and review verified information from one calm workspace.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            StatusChip(
                                label =
                                    if (sessionState is SessionState.Authenticated) {
                                        "Signed in"
                                    } else {
                                        "Signed out"
                                    },
                                tone =
                                    if (sessionState is SessionState.Authenticated) {
                                        StatusChipTone.APPROVED
                                    } else {
                                        StatusChipTone.DRAFT
                                    },
                            )
                            StatusChip(
                                label = currentUser?.role?.toRoleLabel() ?: "No role",
                                tone =
                                    currentUser?.role?.toStatusChipTone()
                                        ?: StatusChipTone.DRAFT,
                            )
                        }
                        MetadataRow(
                            label = "Display name",
                            value = currentUser?.displayName ?: "Unavailable",
                        )
                        MetadataRow(
                            label = "Account",
                            value = currentUser?.email ?: "No active session",
                        )
                    }
                }
            }
            item {
                HomeActionCard(
                    title = "Search medicines",
                    subtitle = "Browse catalog records by name, brand, ingredient, or ATC code.",
                    label = "Open catalog",
                    icon = Icons.Filled.Search,
                    onClick = onSearch,
                )
            }
            item {
                HomeActionCard(
                    title = "Submit leaflet",
                    subtitle = "Prepare a leaflet update for reviewer verification.",
                    label = "Open submission form",
                    icon = Icons.Filled.MedicalServices,
                    onClick = onSubmit,
                )
            }
            if (currentUser?.role in setOf(UserRole.DOCTOR, UserRole.PHARMACIST)) {
                item {
                    HomeActionCard(
                        title = "Review queue",
                        subtitle = "Review pending submissions and confirm verified information.",
                        label = "View pending approvals",
                        icon = Icons.Filled.PendingActions,
                        onClick = onApprovals,
                        badge = "Reviewer access",
                    )
                }
            }
            item {
                HomeActionCard(
                    title = "Profile",
                    subtitle = "Check session details and manage access.",
                    label = "Open profile",
                    icon = Icons.Filled.AccountCircle,
                    onClick = onProfile,
                )
            }
            item {
                HomeActionCard(
                    title = "Sample medicine",
                    subtitle = "Jump into a seeded medicine record for UI inspection.",
                    label = "Open record",
                    icon = Icons.Filled.Info,
                    onClick = { onMedicine(CatalogSeedIds.ASPIRIN_MEDICINE_ID) },
                    secondary = true,
                )
            }
        }
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    subtitle: String,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    badge: String? = null,
    secondary: Boolean = false,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    badge?.let {
                        StatusChip(
                            label = it,
                            tone = StatusChipTone.REVIEWER,
                        )
                    }
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (secondary) {
                OutlinedButton(onClick = onClick) {
                    Text(label)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                    )
                }
            } else {
                Button(onClick = onClick) {
                    Text(label)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

private fun UserRole.toRoleLabel(): String =
    when (this) {
        UserRole.USER -> "User access"
        UserRole.DOCTOR -> "Doctor reviewer"
        UserRole.PHARMACIST -> "Pharmacist reviewer"
    }

private fun UserRole.toStatusChipTone(): StatusChipTone =
    when (this) {
        UserRole.USER -> StatusChipTone.DRAFT
        UserRole.DOCTOR,
        UserRole.PHARMACIST,
        -> StatusChipTone.REVIEWER
    }
