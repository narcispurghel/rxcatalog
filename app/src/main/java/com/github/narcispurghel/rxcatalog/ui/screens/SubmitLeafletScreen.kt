@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.components.common.DetailHeader

@Composable
fun SubmitLeafletScreen(
    submissionId: String?,
    medicineId: String?,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Submit leaflet") })
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DetailHeader(
                title = "Submission details",
                subtitle = "This screen currently shows route context only. Form fields will arrive with the submission flow.",
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "Submission: ${submissionId.orEmpty().ifBlank { "new draft" }}")
                    Text(text = "Medicine: ${medicineId.orEmpty().ifBlank { "not selected yet" }}")
                    Text(
                        text = "No editable fields are available in this screen pass.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            OutlinedButton(
                onClick = { },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Continue") }
        }
    }
}
