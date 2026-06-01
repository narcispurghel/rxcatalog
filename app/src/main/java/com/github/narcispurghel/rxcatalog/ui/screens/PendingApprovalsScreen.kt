@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.components.common.DetailHeader
import com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals.*

@Composable
fun PendingApprovalsScreen(onReview: (String) -> Unit) {
    val queue = sampleApprovalQueue()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Pending approvals") })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
            item {
                DetailHeader(
                    title = "Review queue",
                    subtitle = "Prioritize urgent submissions and keep the waiting list moving.",
                )
            }
            if (queue.isEmpty()) {
                item {
                    EmptyQueueCard()
                }
            } else {
                items(queue) { item ->
                    ApprovalQueueCard(
                        item = item,
                        onReview = onReview,
                    )
                }
            }
        }
    }
}
