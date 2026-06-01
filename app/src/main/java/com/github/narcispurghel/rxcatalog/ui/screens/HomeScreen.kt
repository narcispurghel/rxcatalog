@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.common.UserRole
import com.github.narcispurghel.rxcatalog.ui.components.common.ActionCard
import com.github.narcispurghel.rxcatalog.ui.session.DemoSessionState

@Composable
fun HomeScreen(
    sessionState: DemoSessionState,
    onSearch: () -> Unit,
    onSubmit: () -> Unit,
    onApprovals: () -> Unit,
    onProfile: () -> Unit,
    onMedicine: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Home") })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(text = "Authenticated: ${sessionState.isAuthenticated}")
                        Text(text = "Role: ${sessionState.role?.name ?: "none"}")
                    }
                }
            }
            item {
                ActionCard(
                    title = "Browse catalog",
                    subtitle = "Open the searchable catalog shell.",
                    label = "Search medicines",
                    icon = Icons.Filled.Search,
                    onClick = onSearch,
                )
            }
            item {
                ActionCard(
                    title = "Submit leaflet",
                    subtitle = "Create or edit a pending proposal.",
                    label = "Open submission form",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onSubmit,
                )
            }
            if (sessionState.role in setOf(UserRole.DOCTOR, UserRole.PHARMACIST)) {
                item {
                    ActionCard(
                        title = "Review queue",
                        subtitle = "Reviewer-only access point.",
                        label = "View pending approvals",
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        onClick = onApprovals,
                    )
                }
            }
            item {
                ActionCard(
                    title = "Profile",
                    subtitle = "Review session details and log out.",
                    label = "Open profile",
                    icon = Icons.Filled.AccountCircle,
                    onClick = onProfile,
                )
            }
            item {
                ActionCard(
                    title = "Sample medicine",
                    subtitle = "Jump to a medicine details route.",
                    label = "Open medicine",
                    icon = Icons.Filled.Info,
                    onClick = { onMedicine("med-001") },
                )
            }
        }
    }
}
