package com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EmptyQueueCard() {
	OutlinedCard(
		modifier = Modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.extraLarge,
	) {
		Column(
			modifier = Modifier.padding(20.dp),
		) {
			ListItem(
				modifier = Modifier.padding(0.dp),
				leadingContent = {
					Icon(
						imageVector = Icons.Filled.TaskAlt,
						contentDescription = null,
					)
				},
				headlineContent = {
					Text(text = "Queue is clear")
				},
				supportingContent = {
					Text(
						text = "There are no leaflet submissions waiting for review right now.",
					)
				},
			)
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = "New submissions will appear here with urgency flags and review timestamps.",
				style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
				color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}
	}
}
