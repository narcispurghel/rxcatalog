@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.components.common.ActionCard
import com.github.narcispurghel.rxcatalog.ui.components.common.DetailHeader

@Composable
fun MedicineDetailsScreen(
    medicineId: String,
    onOpenLeaflet: (String) -> Unit,
    onSubmit: (String) -> Unit,
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
                title = "Medicine $medicineId",
                subtitle = "View the medicine overview and available actions.",
            )
            ActionCard(
                title = "Approved leaflet",
                subtitle = "Open the leaflet route for this medicine.",
                label = "Open leaflet",
                icon = Icons.Filled.Info,
                onClick = { onOpenLeaflet("leaflet-$medicineId") },
            )
            ActionCard(
                title = "Suggest an update",
                subtitle = "Open the submit flow with the medicine prefilled.",
                label = "Submit proposal",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = { onSubmit(medicineId) },
            )
        }
    }
}
