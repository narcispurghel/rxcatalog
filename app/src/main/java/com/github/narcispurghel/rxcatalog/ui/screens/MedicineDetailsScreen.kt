@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.components.common.ActionCard
import com.github.narcispurghel.rxcatalog.ui.components.common.DetailHeader
import com.github.narcispurghel.rxcatalog.ui.viewmodels.MedicineDetailsUiState

@Composable
fun MedicineDetailsScreen(
    state: MedicineDetailsUiState,
    onOpenLeaflet: () -> Unit,
    onSubmit: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Medicine") })
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DetailHeader(
                title = state.medicine?.canonicalName ?: "Medicine details",
                subtitle =
                    state.medicine?.description
                        ?: "View the medicine overview and available actions.",
            )
            if (state.isLoading) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(text = "Loading medicine details", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Fetching the latest medicine data from the local database and API.")
                    }
                }
            }
            state.errorMessage?.let { message ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(text = "Details unavailable", style = MaterialTheme.typography.titleMedium)
                        Text(text = message)
                    }
                }
            }
            state.medicine?.let { medicine ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(text = "Medicine overview", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Brand: ${medicine.brandName ?: "Not listed"}")
                        Text(text = "Active ingredient: ${medicine.activeIngredient ?: "Not listed"}")
                        Text(text = "ATC code: ${medicine.atcCode ?: "Not listed"}")
                    }
                }
                medicine.approvedLeaflet?.let { leaflet ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(text = "Approved leaflet", style = MaterialTheme.typography.titleMedium)
                            Text(text = leaflet.title)
                            Text(text = leaflet.approvedAtLabel)
                            Button(onClick = onOpenLeaflet) {
                                Text("Open leaflet")
                            }
                        }
                    }
                }
            }
            ActionCard(
                title = "Approved leaflet",
                subtitle = "Open the leaflet route for this medicine.",
                label = if (state.medicine?.approvedLeaflet != null) "Open leaflet" else "No leaflet yet",
                icon = Icons.Filled.Info,
                onClick = {
                    if (state.medicine?.approvedLeaflet != null) {
                        onOpenLeaflet()
                    }
                },
            )
            ActionCard(
                title = "Suggest an update",
                subtitle = "Open the submit flow with the medicine prefilled.",
                label = "Submit proposal",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = onSubmit,
            )
        }
    }
}
