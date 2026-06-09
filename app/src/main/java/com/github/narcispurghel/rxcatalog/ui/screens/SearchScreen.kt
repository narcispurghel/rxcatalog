@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.viewmodels.SearchResultItem
import com.github.narcispurghel.rxcatalog.ui.viewmodels.SearchUiState

@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onMedicine: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Search") })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(text = "Search medicines", style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(
                            value = state.query,
                            onValueChange = onQueryChanged,
                            label = { Text("Query") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                )
                            },
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = onSubmit) { Text("Submit leaflet") }
                            OutlinedButton(
                                enabled = state.medicines.isNotEmpty() && !state.isLoading,
                                onClick = { onMedicine(state.medicines.first().medicineId) },
                            ) {
                                Text("Open first result")
                            }
                        }
                    }
                }
            }
            when {
                state.errorMessage != null -> {
                    item {
                        SearchErrorCard(message = state.errorMessage)
                    }
                }

                state.isLoading -> {
                    item {
                        SearchLoadingCard()
                    }
                }

                state.medicines.isEmpty() -> {
                    item {
                        SearchEmptyCard(query = state.query)
                    }
                }

                else -> {
                    items(
                        items = state.medicines,
                        key = { it.medicineId },
                    ) { medicine ->
                        MedicineSearchCard(
                            medicine = medicine,
                            onMedicine = onMedicine,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicineSearchCard(
    medicine: SearchResultItem,
    onMedicine: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = medicine.canonicalName,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (!medicine.brandName.isNullOrBlank()) {
                    Text(text = medicine.brandName)
                }
                if (!medicine.activeIngredient.isNullOrBlank()) {
                    Text(text = "Active ingredient: ${medicine.activeIngredient}")
                }
                if (!medicine.atcCode.isNullOrBlank()) {
                    Text(text = "ATC code: ${medicine.atcCode}")
                }
                medicine.description?.let {
                    Text(text = it)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { onMedicine(medicine.medicineId) }) {
                    Text("Open")
                }
            }
        }
    }
}

@Composable
private fun SearchLoadingCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "Loading medicines", style = MaterialTheme.typography.titleMedium)
            Text(text = "Reading the local database for matching medicines.")
        }
    }
}

@Composable
private fun SearchErrorCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "Search unavailable", style = MaterialTheme.typography.titleMedium)
            Text(text = message)
        }
    }
}

@Composable
private fun SearchEmptyCard(query: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "No medicines found", style = MaterialTheme.typography.titleMedium)
            Text(
                text =
                    if (query.isBlank()) {
                        "The local database does not contain any medicines yet."
                    } else {
                        "No local matches for \"$query\"."
                    },
            )
        }
    }
}
