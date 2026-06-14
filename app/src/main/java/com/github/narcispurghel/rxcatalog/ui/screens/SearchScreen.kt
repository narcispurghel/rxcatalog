@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.github.narcispurghel.rxcatalog.ui.components.common.MetadataRow
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChip
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChipTone
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
        TopAppBar(title = { Text("Search medicines") })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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
                            label = { Text("Medicine or ATC code") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                )
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
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
private fun MedicineSearchCard(
    medicine: SearchResultItem,
    onMedicine: (String) -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    medicine.brandName
                        ?.takeIf { it.isNotBlank() }
                        ?.let { brandName ->
                            Text(
                                text = brandName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    Text(
                        text = medicine.canonicalName,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    val ingredientLine =
                        medicine.activeIngredient
                            ?.takeIf { it.isNotBlank() }
                            ?: "Ingredient not listed"
                    Text(
                        text = ingredientLine,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusChip(
                    label = medicine.leafletStatusLabel(),
                    tone = medicine.leafletStatusTone(),
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                DataPill(
                    text = medicine.atcCode?.takeIf { it.isNotBlank() } ?: "ATC code unavailable",
                )
                if (!medicine.activeIngredient.isNullOrBlank()) {
                    DataPill(text = "Ingredient listed")
                }
            }
            medicine.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetadataRow(
                    label = "Brand",
                    value = medicine.brandName?.takeIf { it.isNotBlank() } ?: "Not specified",
                )
                MetadataRow(
                    label = "ATC code",
                    value = medicine.atcCode?.takeIf { it.isNotBlank() } ?: "Unavailable",
                )
                MetadataRow(
                    label = "Leaflet",
                    value =
                        if (medicine.description.isNullOrBlank()) {
                            "Not listed"
                        } else {
                            "Details available"
                        },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { onMedicine(medicine.medicineId) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open record")
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

@Composable
private fun DataPill(text: String) {
    SuggestionChip(
        onClick = { },
        label = { Text(text) },
    )
}

private fun SearchResultItem.leafletStatusLabel(): String =
    if (description.isNullOrBlank()) {
        "Leaflet unavailable"
    } else {
        "Leaflet listed"
    }

private fun SearchResultItem.leafletStatusTone(): StatusChipTone =
    if (description.isNullOrBlank()) {
        StatusChipTone.DRAFT
    } else {
        StatusChipTone.REVIEWER
    }
