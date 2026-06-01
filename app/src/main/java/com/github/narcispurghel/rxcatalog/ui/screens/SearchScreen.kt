@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchScreen(
    query: String?,
    onMedicine: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    var queryText by remember { mutableStateOf(query.orEmpty()) }

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
                            value = queryText,
                            onValueChange = { queryText = it },
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
                                onClick = { onMedicine("med-001") },
                            ) { Text("Open result") }
                        }
                    }
                }
            }
            items(listOf("med-001", "med-002", "med-003")) { medicineId ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Medicine $medicineId",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(text = "Route placeholder for search integration.")
                        }
                        OutlinedButton(onClick = { onMedicine(medicineId) }) { Text("Open") }
                    }
                }
            }
        }
    }
}
