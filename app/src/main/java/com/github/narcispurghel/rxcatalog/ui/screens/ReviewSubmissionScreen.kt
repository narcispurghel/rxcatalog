@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.components.common.DetailHeader

@Composable
fun ReviewSubmissionScreen(submissionId: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Review submission") })
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DetailHeader(
                title = "Submission $submissionId",
                subtitle = "Review the submission details and add feedback.",
            )
            OutlinedTextField(
                value = "",
                onValueChange = { },
                label = { Text("Rejection reason") },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = { }) { Text("Approve") }
                OutlinedButton(onClick = { }) { Text("Reject") }
            }
        }
    }
}
