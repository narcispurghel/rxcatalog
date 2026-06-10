@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.components.common.DetailHeader
import com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals.ApprovalQueueCard
import com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals.EmptyQueueCard
import com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals.QueueFilters
import com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals.ReviewerHero
import com.github.narcispurghel.rxcatalog.ui.viewmodels.PendingApprovalsUiState

@Composable
fun PendingApprovalsScreen(
    state: PendingApprovalsUiState,
    onReview: (String) -> Unit,
) {
    val queue = state.queue
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Pending approvals") })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ReviewerHero(
                    pendingCount = queue.size,
                    urgentCount = queue.count { it.isUrgent },
                )
            }
            item {
                QueueFilters()
            }
            when {
                state.errorMessage != null -> {
                    item {
                        QueueErrorCard(message = state.errorMessage)
                    }
                }

                state.isLoading -> {
                    item {
                        QueueLoadingCard()
                    }
                }

                queue.isEmpty() -> {
                    item {
                        EmptyQueueCard()
                    }
                }

                else -> {
                    item {
                        DetailHeader(
                            title = "Review queue",
                            subtitle =
                                "Prioritize urgent submissions, verify reviewer notes, and keep the queue moving.",
                        )
                    }
                    items(
                        items = queue,
                        key = { it.submissionId },
                    ) { item ->
                        ApprovalQueueCard(
                            item = item,
                            onReview = onReview,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueLoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Icon(
                    imageVector = Icons.Filled.HourglassTop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(10.dp),
                )
            }
            Text(text = "Loading review queue", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Checking stored submissions and reviewer priority markers.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun QueueErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.28f)),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.errorContainer,
            ) {
                Icon(
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(10.dp),
                )
            }
            Text(text = "Review queue unavailable", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Reviewer data could not be loaded. Check the local queue source and try again.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = message, style = MaterialTheme.typography.bodySmall)
        }
    }
}
