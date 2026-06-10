@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.components.common.MetadataRow
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChip
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChipTone
import com.github.narcispurghel.rxcatalog.ui.viewmodels.MySubmissionItem
import com.github.narcispurghel.rxcatalog.ui.viewmodels.MySubmissionsUiState
import java.util.Locale

@Composable
fun MySubmissionsScreen(
    state: MySubmissionsUiState,
    onEdit: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("My submissions") })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SubmissionsHeroCard(count = state.submissions.size)
            }

            when {
                state.errorMessage != null -> {
                    item {
                        FeedbackCard(
                            title = "Submissions unavailable",
                            body =
                                "We could not read your local leaflet submissions right now. " +
                                    state.errorMessage,
                        )
                    }
                }

                state.isLoading -> {
                    item {
                        FeedbackCard(
                            title = "Loading submissions",
                            body =
                                "Checking your drafts, pending reviews, and verified leaflet updates.",
                        )
                    }
                }

                state.submissions.isEmpty() -> {
                    item {
                        FeedbackCard(
                            title = "No submissions yet",
                            body =
                                "When you submit a leaflet for review, it will appear here with its latest status.",
                        )
                    }
                }

                else -> {
                    items(
                        items = state.submissions,
                        key = { it.submissionId },
                    ) { submission ->
                        SubmissionCard(
                            submission = submission,
                            onEdit = onEdit,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubmissionsHeroCard(count: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "My leaflet submissions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text =
                            "Track drafts, pending reviews, and verified leaflet updates without losing context.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                    )
                }
                StatusChip(
                    label = "$count total",
                    tone = StatusChipTone.REVIEWER,
                )
            }
            Text(
                text =
                    if (count == 0) {
                        "You do not have any saved submission records yet."
                    } else {
                        "Each submission shows its latest review state and last local update."
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
            )
        }
    }
}

@Composable
private fun SubmissionCard(
    submission: MySubmissionItem,
    onEdit: (String) -> Unit,
) {
    val appearance = submission.statusAppearance()
    RecordCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = submission.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = submission.medicineName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusChip(
                    label = appearance.label,
                    tone = appearance.tone,
                )
            }

            Text(
                text = appearance.supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            MetadataRow(
                label = "Last update",
                value = submission.updatedLabel.removePrefix("Updated ").ifBlank { submission.updatedLabel },
            )

            Button(onClick = { onEdit(submission.submissionId) }) {
                Icon(
                    imageVector = appearance.actionIcon,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(submission.actionLabel)
            }
        }
    }
}

@Composable
private fun FeedbackCard(
    title: String,
    body: String,
) {
    RecordCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecordCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

private data class SubmissionStatusAppearance(
    val label: String,
    val supportingText: String,
    val tone: StatusChipTone,
    val actionIcon: androidx.compose.ui.graphics.vector.ImageVector,
)

@Composable
private fun MySubmissionItem.statusAppearance(): SubmissionStatusAppearance {
    val normalized = statusLabel.trim().lowercase(Locale.US)
    return when {
        "approved" in normalized || "verified" in normalized -> {
            SubmissionStatusAppearance(
                label = "Verified leaflet",
                supportingText = "This submission is approved and currently represented as verified leaflet content.",
                tone = StatusChipTone.APPROVED,
                actionIcon = Icons.Filled.CheckCircle,
            )
        }

        "pending" in normalized -> {
            SubmissionStatusAppearance(
                label = "Pending review",
                supportingText = "A reviewer still needs to confirm the latest submission details.",
                tone = StatusChipTone.PENDING,
                actionIcon = Icons.Filled.PendingActions,
            )
        }

        "rejected" in normalized || "revision" in normalized -> {
            SubmissionStatusAppearance(
                label = "Needs revision",
                supportingText = "A reviewer requested changes before this leaflet can be verified.",
                tone = StatusChipTone.NEEDS_REVISION,
                actionIcon = Icons.Filled.SyncProblem,
            )
        }

        "draft" in normalized -> {
            SubmissionStatusAppearance(
                label = "Draft",
                supportingText = "Keep editing until the leaflet is ready to send for review.",
                tone = StatusChipTone.DRAFT,
                actionIcon = Icons.Filled.Edit,
            )
        }

        else -> {
            SubmissionStatusAppearance(
                label = statusLabel,
                supportingText = "This submission remains available in your local review history.",
                tone = StatusChipTone.DRAFT,
                actionIcon = Icons.Filled.Description,
            )
        }
    }
}
