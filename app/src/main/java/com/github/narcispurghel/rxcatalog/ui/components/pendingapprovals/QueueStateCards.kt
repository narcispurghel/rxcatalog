package com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun QueueLoadingCard() {
	OutlinedCard(
		modifier = Modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.extraLarge,
	) {
		ListItem(
			leadingContent = {
				Icon(
					imageVector = Icons.Filled.HourglassTop,
					contentDescription = null,
				)
			},
			headlineContent = {
				Text(text = "Loading review queue")
			},
			supportingContent = {
				Text(text = "Checking stored submissions and reviewer priority markers.")
			},
		)
	}
}

@Composable
fun QueueErrorCard(message: String) {
	OutlinedCard(
		modifier = Modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.extraLarge,
	) {
		ListItem(
			leadingContent = {
				Icon(
					imageVector = Icons.Filled.ErrorOutline,
					contentDescription = null,
				)
			},
			headlineContent = {
				Text(text = "Review queue unavailable")
			},
			supportingContent = {
				Text(
					text =
						"Reviewer data could not be loaded. Check the local queue source and try again.\n$message",
				)
			},
		)
	}
}
