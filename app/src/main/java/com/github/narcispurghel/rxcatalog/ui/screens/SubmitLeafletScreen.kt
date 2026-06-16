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
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.components.common.DetailHeader
import com.github.narcispurghel.rxcatalog.ui.components.common.MetadataRow
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChip
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChipTone
import com.github.narcispurghel.rxcatalog.ui.viewmodels.SubmitLeafletUiState

@Composable
fun SubmitLeafletScreen(
    state: SubmitLeafletUiState,
    onMedicineIdChanged: (String) -> Unit,
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
                subtitle = state.errorMessage ?: "Draft the leaflet and send it to review when ready.",
            )

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
                        value = state.submission?.medicineName ?: if (state.medicineId.isBlank()) "Enter a medicine ID" else state.medicineId,
                    )
                    if (state.canEditMedicineId) {
                        OutlinedTextField(
                            value = state.medicineId,
                            onValueChange = onMedicineIdChanged,
                            enabled = !state.isLoading && !state.isSaving && !isReadOnly,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Medicine ID") },
                            placeholder = { Text("Paste the medicine ID from search or details") },
                        )
                    }
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = onTitleChanged,
                        enabled = !state.isLoading && !state.isSaving && !isReadOnly,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Title") },
                    )
                    OutlinedTextField(
                        value = state.content,
                        onValueChange = onContentChanged,
                        enabled = !state.isLoading && !state.isSaving && !isReadOnly,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Content") },
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
                    enabled = !state.isLoading && !state.isSaving && state.medicineId.isNotBlank() && !isReadOnly,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save draft")
                }
                Button(
                    onClick = onSubmitForReview,
                    enabled = !state.isLoading && !state.isSaving && state.medicineId.isNotBlank() && !isReadOnly,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Submit")
                }
            }
        }
    }
}

private fun String.toSubmissionTone(): StatusChipTone =
    when {
        contains("approved", ignoreCase = true) -> StatusChipTone.APPROVED
        contains("pending", ignoreCase = true) -> StatusChipTone.PENDING
        contains("rejected", ignoreCase = true) || contains("revision", ignoreCase = true) -> StatusChipTone.NEEDS_REVISION
        contains("draft", ignoreCase = true) -> StatusChipTone.DRAFT
        contains("review", ignoreCase = true) -> StatusChipTone.REVIEWER
        else -> StatusChipTone.DRAFT
    }
