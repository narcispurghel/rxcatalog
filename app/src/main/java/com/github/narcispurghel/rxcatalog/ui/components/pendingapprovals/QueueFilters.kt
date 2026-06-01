package com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun QueueFilters() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = true,
            onClick = { },
            label = { Text("All") },
        )
        FilterChip(
            selected = false,
            onClick = { },
            label = { Text("Urgent") },
        )
        FilterChip(
            selected = false,
            onClick = { },
            label = { Text("Newest") },
        )
    }
}
