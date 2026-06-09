@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.components.common.DetailHeader
import com.github.narcispurghel.rxcatalog.ui.viewmodels.LeafletDetailsUiState

@Composable
fun LeafletDetailsScreen(state: LeafletDetailsUiState) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Leaflet") })
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DetailHeader(
                title = state.leaflet?.title ?: "Leaflet details",
                subtitle =
                    state.leaflet?.medicineName?.let { "Approved leaflet for $it." }
                        ?: "Read the approved leaflet content for this medicine.",
            )
            if (state.isLoading) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(text = "Loading leaflet details", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Fetching the latest approved leaflet from the local database.")
                    }
                }
            }
            state.errorMessage?.let { message ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(text = "Leaflet unavailable", style = MaterialTheme.typography.titleMedium)
                        Text(text = message)
                    }
                }
            }
            state.leaflet?.let { leaflet ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(text = leaflet.medicineName, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Version ${leaflet.version}")
                        Text(text = leaflet.approvedAtLabel)
                        Text(text = leaflet.content)
                    }
                }
            }
        }
    }
}
