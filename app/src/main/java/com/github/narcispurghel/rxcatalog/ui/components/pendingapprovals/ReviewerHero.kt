package com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChip
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChipTone

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewerHero(
    pendingCount: Int,
    urgentCount: Int,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ListItem(
                modifier = Modifier.padding(0.dp),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.PendingActions,
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(
                        text = "Reviewer dashboard",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                supportingContent = {
                    Text(
                        text = "Keep leaflet reviews moving with a compact queue, clear status, and safer handoff notes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SummaryChip(
                    icon = Icons.Filled.Schedule,
                    label = "$pendingCount pending",
                    tone = StatusChipTone.PENDING,
                )
                SummaryChip(
                    icon = Icons.Filled.LocalHospital,
                    label = "$urgentCount urgent",
                    tone = if (urgentCount > 0) StatusChipTone.URGENT else StatusChipTone.DRAFT,
                )
            }
        }
    }
}

@Composable
private fun SummaryChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tone: StatusChipTone,
) {
    StatusChip(
        label = label,
        tone = tone,
        icon = icon,
    )
}
