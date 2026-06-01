@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.components.common.DetailHeader

@Composable
fun LeafletDetailsScreen(leafletId: String) {
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
                title = "Leaflet $leafletId",
                subtitle = "Read the approved leaflet content for this medicine.",
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "Content placeholder")
                    Text(text = "Detailed content will appear here.")
                }
            }
        }
    }
}
