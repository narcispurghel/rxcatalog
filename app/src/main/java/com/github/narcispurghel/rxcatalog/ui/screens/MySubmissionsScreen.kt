@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.viewmodels.MySubmissionItem
import com.github.narcispurghel.rxcatalog.ui.viewmodels.MySubmissionsUiState

@Composable
fun MySubmissionsScreen(
    state: MySubmissionsUiState,
    onEdit: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("My submissions") })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SummaryCard(count = state.submissions.size)
            }

            when {
                state.errorMessage != null -> {
                    item {
                        SubmissionsErrorCard(message = state.errorMessage)
                    }
                }

                state.isLoading -> {
                    item {
                        SubmissionsLoadingCard()
                    }
                }

                state.submissions.isEmpty() -> {
                    item {
                        EmptySubmissionsCard()
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
private fun SummaryCard(count: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = "Local submissions", style = MaterialTheme.typography.titleMedium)
            Text(text = "$count submissions stored in Room.")
        }
    }
}

@Composable
private fun SubmissionCard(
    submission: MySubmissionItem,
    onEdit: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = submission.title, style = MaterialTheme.typography.titleMedium)
                Text(text = submission.medicineName)
                Text(text = submission.statusLabel)
                Text(text = submission.updatedLabel)
            }
            OutlinedButton(onClick = { onEdit(submission.submissionId) }) {
                Text(submission.actionLabel)
            }
        }
    }
}

@Composable
private fun SubmissionsLoadingCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "Loading submissions", style = MaterialTheme.typography.titleMedium)
            Text(text = "Reading your local drafts and review history.")
        }
    }
}

@Composable
private fun SubmissionsErrorCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "Submissions unavailable", style = MaterialTheme.typography.titleMedium)
            Text(text = message)
        }
    }
}

@Composable
private fun EmptySubmissionsCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "No submissions yet", style = MaterialTheme.typography.titleMedium)
            Text(text = "Submitted leaflets from the local database will appear here.")
        }
    }
}
