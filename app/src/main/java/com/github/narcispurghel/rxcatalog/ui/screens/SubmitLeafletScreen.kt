@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
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
                    .fillMaxSize()
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DetailHeader(
                title = "Submission shell",
                subtitle = "Edit a pending proposal or create a new one later.",
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "submissionId: ${submissionId.orEmpty().ifBlank { "new" }}")
                    Text(text = "medicineId: ${medicineId.orEmpty().ifBlank { "not selected" }}")
                }
            }
            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save placeholder") }
        }
    }
}
