package com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QueueFilters() {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QueueFilterChip(label = "All", selected = true)
        QueueFilterChip(label = "Urgent", selected = false)
        QueueFilterChip(label = "Newest", selected = false)
    }
}

@Composable
private fun QueueFilterChip(
    label: String,
    selected: Boolean,
) {
    FilterChip(
        selected = selected,
        onClick = { },
        label = { Text(label) },
    )
}
