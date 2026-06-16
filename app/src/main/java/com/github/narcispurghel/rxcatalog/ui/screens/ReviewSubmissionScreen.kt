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
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.github.narcispurghel.rxcatalog.ui.viewmodels.ReviewSubmissionUiState

@Composable
fun ReviewSubmissionScreen(
    state: ReviewSubmissionUiState,
    onReviewerNoteChanged: (String) -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
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
                title = state.submission?.title ?: "Submission review",
                subtitle = state.errorMessage ?: "Assess the leaflet and record the reviewer note before deciding.",
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
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusChip(
                                label = state.submission.statusLabel,
                                tone = state.submission.statusLabel.toSubmissionTone(),
                            )
                            StatusChip(
                                label = "Reviewer workflow",
                                tone = StatusChipTone.REVIEWER,
                            )
                        }
                    }

                    MetadataRow(
                        label = "Submission",
                        value = state.submission?.submissionId ?: "Unknown",
                    )
                    MetadataRow(
                        label = "Medicine",
                        value = state.submission?.medicineName ?: "Unknown",
                    )
                    MetadataRow(
                        label = "Submitted by",
                        value = state.submission?.submittedBy ?: "Unknown",
                    )
                    MetadataRow(
                        label = "Created",
                        value = state.submission?.createdAtLabel ?: "Pending",
                    )
                    MetadataRow(
                        label = "Updated",
                        value = state.submission?.updatedAtLabel ?: "Pending",
                    )
                    if (!state.submission?.reviewedAtLabel.isNullOrBlank()) {
                        MetadataRow(
                            label = "Reviewed",
                            value = state.submission?.reviewedAtLabel.orEmpty(),
                        )
                    }
                    if (!state.submission?.reviewedBy.isNullOrBlank()) {
                        MetadataRow(
                            label = "Reviewed by",
                            value = state.submission?.reviewedBy.orEmpty(),
                        )
                    }
                    if (!state.submission?.rejectionReason.isNullOrBlank()) {
                        MetadataRow(
                            label = "Rejection reason",
                            value = state.submission?.rejectionReason.orEmpty(),
                        )
                    }
                }
            }

            HorizontalDivider()

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(text = "Submission content", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = state.submission?.content ?: "Submission details will appear here once loaded.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    Text(text = "Reviewer note", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.reviewerNote,
                        onValueChange = onReviewerNoteChanged,
                        enabled = !state.isLoading && !state.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        placeholder = { Text("Add the change needed before this leaflet can be verified") },
                    )
                }
            }

            if (state.isLoading || state.isSaving) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onApprove,
                    enabled = !state.isLoading && !state.isSaving && state.submission != null,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Approve")
                }
                OutlinedButton(
                    onClick = onReject,
                    enabled = !state.isLoading && !state.isSaving && state.submission != null,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Reject")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Filled.ErrorOutline, contentDescription = null)
                Text(
                    text = "Approvals and rejections write to local Room state.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
        else -> StatusChipTone.REVIEWER
    }
