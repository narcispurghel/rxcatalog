package com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChip
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChipTone

@Composable
fun ReviewerHero(
    pendingCount: Int,
    urgentCount: Int,
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        border =
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Filled.PendingActions,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reviewer dashboard",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = "Keep leaflet reviews moving with a compact queue, clear status, and safer handoff notes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryPill(
                    icon = Icons.Filled.Schedule,
                    label = "$pendingCount pending",
                    tone = StatusChipTone.PENDING,
                )
                SummaryPill(
                    icon = Icons.Filled.LocalHospital,
                    label = "$urgentCount urgent",
                    tone = if (urgentCount > 0) StatusChipTone.URGENT else StatusChipTone.DRAFT,
                )
            }
        }
    }
}

@Composable
private fun SummaryPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tone: StatusChipTone,
) {
    StatusChip(label = label, tone = tone, icon = icon)
}
