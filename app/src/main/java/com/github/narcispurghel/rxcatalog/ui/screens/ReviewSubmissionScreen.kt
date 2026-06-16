@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.components.common.DetailHeader
import com.github.narcispurghel.rxcatalog.ui.components.common.MetadataRow
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChip
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChipTone

@Composable
fun ReviewSubmissionScreen(submissionId: String) {
	Column(modifier = Modifier.fillMaxSize()) {
		TopAppBar(title = { Text("Review submission") })
		Column(
			modifier =
				Modifier
					.weight(1f)
					.fillMaxWidth()
					.verticalScroll(rememberScrollState())
					.padding(20.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp),
		) {
			DetailHeader(
				title = "Submission $submissionId",
				subtitle = "This screen is a review shell for the current route context. Review actions are not wired yet.",
			)

			ReviewSummaryCard(submissionId = submissionId)

			OutlinedCard(
				modifier = Modifier.fillMaxWidth(),
				shape = MaterialTheme.shapes.extraLarge,
			) {
				Column(
					modifier = Modifier.padding(16.dp),
					verticalArrangement = Arrangement.spacedBy(12.dp),
				) {
					Text(
						text = "Reviewer note",
						style = MaterialTheme.typography.titleMedium,
					)
					Text(
						text = "Reviewer note is shown for layout only in this pass.",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
					OutlinedTextField(
						value = "",
						onValueChange = { },
						enabled = false,
						placeholder = {
							Text(
								"Add the change needed before this leaflet can be verified",
							)
						},
						modifier = Modifier.fillMaxWidth(),
						minLines = 4,
						shape = MaterialTheme.shapes.extraLarge,
						colors =
							OutlinedTextFieldDefaults.colors(
								focusedBorderColor = MaterialTheme.colorScheme.primary,
								cursorColor = MaterialTheme.colorScheme.primary,
								focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
								unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
								disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
								errorContainerColor = MaterialTheme.colorScheme.errorContainer,
							),
					)
				}
			}

			OutlinedCard(
				modifier = Modifier.fillMaxWidth(),
				shape = MaterialTheme.shapes.extraLarge,
			) {
				Row(
					modifier = Modifier.padding(16.dp),
					horizontalArrangement = Arrangement.spacedBy(12.dp),
				) {
					Icon(
						imageVector = Icons.Filled.ErrorOutline,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurfaceVariant,
					)
					Text(
						text = "Approval and rejection controls are present for visual consistency only.",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				}
			}

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
			) {
				Button(
					onClick = { },
					enabled = false,
					modifier = Modifier.weight(1f),
				) {
					Text("Approve")
				}
				OutlinedButton(
					onClick = { },
					enabled = false,
					modifier = Modifier.weight(1f),
				) {
					Text("Reject")
				}
			}
		}
	}
}

@Composable
private fun ReviewSummaryCard(submissionId: String) {
	OutlinedCard(
		modifier = Modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.extraLarge,
	) {
		Column(
			modifier = Modifier.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				StatusChip(
					label = "Pending review",
					tone = StatusChipTone.PENDING,
				)
				StatusChip(
					label = "Reviewer workflow",
					tone = StatusChipTone.REVIEWER,
				)
			}

			Text(
				text = "Leaflet verification",
				style = MaterialTheme.typography.titleMedium,
			)
			Text(
				text = "Submission $submissionId is ready for a clinical review pass and reviewer feedback.",
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)

			HorizontalDivider()

			Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
				MetadataRow(
					label = "Submission",
					value = submissionId,
				)
				MetadataRow(
					label = "Submitted by",
					value = "Placeholder reviewer handoff",
				)
				MetadataRow(
					label = "Received",
					value = "Pending timestamp",
				)
				MetadataRow(
					label = "Decision",
					value = "Approve when complete and clinically clear",
				)
			}
		}
	}
}
