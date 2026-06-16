@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.components.search.MedicineSearchCard
import com.github.narcispurghel.rxcatalog.ui.viewmodels.SearchUiState

@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onMedicine: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Search medicines") })
        LazyColumn(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Search medicines",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = "Search by medicine name, brand, ingredient, or ATC code.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedTextField(
                            value = state.query,
                            onValueChange = onQueryChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("Medicine or ATC code")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                )
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.extraLarge,
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                                    focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    errorContainerColor = MaterialTheme.colorScheme.errorContainer,
                                ),
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
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
                        SearchEmptyCard(
                            query = state.query,
                            onSubmit = onSubmit,
                        )
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
private fun SearchLoadingCard() {
    SearchStateCard(
        icon = Icons.Filled.Sync,
        title = "Loading medicines",
        message = "Checking local records for medicines and leaflet details.",
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
        )
    }
}

@Composable
private fun SearchErrorCard(message: String) {
    SearchStateCard(
        icon = Icons.Filled.ErrorOutline,
        title = "Search unavailable",
        message = message,
    )
}

@Composable
private fun SearchEmptyCard(
    query: String,
    onSubmit: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(text = "No medicines found", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                text =
                    if (query.isBlank()) {
                        "No medicine records are available yet. You can add a leaflet submission to start review."
                    } else {
                        "No local matches for \"$query\". Try a medicine name, active ingredient, or ATC code."
                    },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = onSubmit) {
                Text("Submit leaflet")
            }
        }
    }
}

@Composable
private fun SearchStateCard(
    icon: ImageVector,
    title: String,
    message: String,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                )
            },
            headlineContent = {
                Text(text = title)
            },
            supportingContent = {
                Text(text = message)
            },
            trailingContent = {
                trailingContent?.invoke()
            },
        )
    }
}
