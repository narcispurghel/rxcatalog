@file:OptIn(
	androidx.compose.material3.ExperimentalMaterial3Api::class,
	kotlin.uuid.ExperimentalUuidApi::class,
)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.components.common.DetailHeader
import com.github.narcispurghel.rxcatalog.ui.components.common.MetadataRow
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChip
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChipTone
import com.github.narcispurghel.rxcatalog.ui.viewmodels.SubmissionMedicineOption
import com.github.narcispurghel.rxcatalog.ui.viewmodels.SubmitLeafletUiState

@Composable
fun SubmitLeafletScreen(
	state: SubmitLeafletUiState,
	onMedicineSelected: (String) -> Unit,
	onTitleChanged: (String) -> Unit,
	onContentChanged: (String) -> Unit,
	onSaveDraft: () -> Unit,
	onSubmitForReview: () -> Unit,
) {
	val isReadOnly = state.submission?.statusLabel.equals("Approved", ignoreCase = true)
	Column(modifier = Modifier.fillMaxSize()) {
		TopAppBar(title = { Text("Submit leaflet") })
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
				title = if (state.submission == null) "New submission" else "Edit submission",
				subtitle =
					state.errorMessage ?: "Draft the leaflet and send it to review when ready.",
			)

			state.errorMessage?.let { message ->
				Card(
					modifier = Modifier.fillMaxWidth(),
					colors =
						CardDefaults.cardColors(
							containerColor = MaterialTheme.colorScheme.errorContainer,
						),
				) {
					Text(
						text = message,
						modifier = Modifier.padding(16.dp),
						color = MaterialTheme.colorScheme.onErrorContainer,
						style = MaterialTheme.typography.bodyMedium,
					)
				}
			}

			OutlinedCard(
				modifier = Modifier.fillMaxWidth(),
				shape = MaterialTheme.shapes.extraLarge,
			) {
				Column(
					modifier = Modifier.padding(16.dp),
					verticalArrangement = Arrangement.spacedBy(12.dp),
				) {
					if (state.submission != null) {
						StatusChip(
							label = state.submission.statusLabel,
							tone = state.submission.statusLabel.toSubmissionTone(),
						)
					}
					MetadataRow(
						label = "Submission",
						value = state.submission?.submissionId ?: "New draft",
					)
					MetadataRow(
						label = "Medicine",
						value =
							state.submission?.medicineName
								?: state.selectedMedicineLabel
								?: "Choose a medicine",
					)
					if (state.canEditMedicineId) {
						MedicineSelectorField(
							options = state.availableMedicines,
							selectedMedicineLabel = state.selectedMedicineLabel,
							hasSelection = state.medicineId.isNotBlank(),
							onMedicineSelected = onMedicineSelected,
							enabled = !state.isLoading && !state.isSaving && !isReadOnly,
						)
					}
					OutlinedTextField(
						value = state.title,
						onValueChange = onTitleChanged,
						enabled = !state.isLoading && !state.isSaving && !isReadOnly,
						modifier = Modifier.fillMaxWidth(),
						placeholder = { Text("Summarize the leaflet update") },
					)
					OutlinedTextField(
						value = state.content,
						onValueChange = onContentChanged,
						enabled = !state.isLoading && !state.isSaving && !isReadOnly,
						modifier = Modifier.fillMaxWidth(),
						placeholder = { Text("Describe the leaflet details to review") },
						minLines = 8,
					)
					if (isReadOnly) {
						Text(
							text = "Approved submissions are read-only.",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
						)
					}
				}
			}

			if (state.isLoading || state.isSaving) {
				LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
			}

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
			) {
				OutlinedButton(
					onClick = onSaveDraft,
					enabled = !state.isLoading && !state.isSaving && !isReadOnly,
					modifier = Modifier.weight(1f),
				) {
					Text("Save draft")
				}
				Button(
					onClick = onSubmitForReview,
					enabled = !state.isLoading && !state.isSaving && !isReadOnly,
					modifier = Modifier.weight(1f),
				) {
					Text("Submit")
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicineSelectorField(
	options: List<SubmissionMedicineOption>,
	selectedMedicineLabel: String?,
	hasSelection: Boolean,
	onMedicineSelected: (String) -> Unit,
	enabled: Boolean,
) {
	var expanded by rememberSaveable { mutableStateOf(false) }

	ExposedDropdownMenuBox(
		expanded = expanded,
		onExpandedChange = { isExpanded ->
			if (enabled) {
				expanded = isExpanded
			}
		},
	) {
		OutlinedTextField(
			value = selectedMedicineLabel.orEmpty(),
			onValueChange = {},
			modifier =
				Modifier
					.fillMaxWidth()
					.menuAnchor(MenuAnchorType.PrimaryNotEditable),
			readOnly = true,
			enabled = enabled,
			placeholder = { Text("Select a medicine") },
			supportingText = {
				Text(
					if (hasSelection) {
						"Medicine IDs are handled internally."
					} else {
						"Choose the medicine this leaflet update belongs to."
					},
				)
			},
			isError = !hasSelection,
			trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
		)
		ExposedDropdownMenu(
			expanded = expanded,
			onDismissRequest = { expanded = false },
		) {
			options.forEach { option ->
				DropdownMenuItem(
					text = {
						Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
							Text(
								text = option.title,
								maxLines = 1,
								overflow = TextOverflow.Ellipsis,
							)
							Text(
								text = option.supportingText,
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
								maxLines = 2,
								overflow = TextOverflow.Ellipsis,
							)
						}
					},
					onClick = {
						onMedicineSelected(option.medicineId)
						expanded = false
					},
				)
			}
		}
	}
}

private fun String.toSubmissionTone(): StatusChipTone =
	when {
		contains("approved", ignoreCase = true) -> StatusChipTone.APPROVED

		contains("pending", ignoreCase = true) -> StatusChipTone.PENDING

		contains(
			"rejected",
			ignoreCase = true,
		) || contains("revision", ignoreCase = true) -> StatusChipTone.NEEDS_REVISION

		contains("draft", ignoreCase = true) -> StatusChipTone.DRAFT

		contains("review", ignoreCase = true) -> StatusChipTone.REVIEWER

		else -> StatusChipTone.DRAFT
	}
