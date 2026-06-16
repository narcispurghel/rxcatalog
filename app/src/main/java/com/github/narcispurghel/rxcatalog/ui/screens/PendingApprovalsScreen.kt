@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.components.common.DetailHeader
import com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals.ApprovalQueueCard
import com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals.EmptyQueueCard
import com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals.QueueFilters
import com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals.QueueErrorCard
import com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals.QueueLoadingCard
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
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
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
