package com.github.narcispurghel.rxcatalog.ui.components.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MetadataRow(
	label: String,
	value: String,
	modifier: Modifier = Modifier,
) {
	ListItem(
		modifier = modifier.fillMaxWidth(),
		overlineContent = {
			Text(
				text = label,
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		},
		headlineContent = {
			Text(
				text = value,
				style = MaterialTheme.typography.bodyMedium,
			)
		},
	)
}
