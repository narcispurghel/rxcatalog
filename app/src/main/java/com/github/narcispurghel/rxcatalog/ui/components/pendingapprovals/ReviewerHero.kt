package com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReviewerHero(
    pendingCount: Int,
    urgentCount: Int,
) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.PendingActions,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reviewer dashboard",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = "Process pending leaflet submissions without losing context.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AssistChip(
                    onClick = { },
                    label = { Text("$pendingCount pending") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Schedule, contentDescription = null)
                    },
                )
                AssistChip(
                    onClick = { },
                    label = { Text("$urgentCount urgent") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.FilterAlt, contentDescription = null)
                    },
                )
            }
        }
    }
}
