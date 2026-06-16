@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.catalog.MedicineDetailsItem
import com.github.narcispurghel.rxcatalog.ui.components.common.RecordCard
import com.github.narcispurghel.rxcatalog.ui.components.common.MetadataRow
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChip
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChipTone
import com.github.narcispurghel.rxcatalog.ui.viewmodels.MedicineDetailsUiState

@Composable
fun MedicineDetailsScreen(
    state: MedicineDetailsUiState,
    onOpenLeaflet: () -> Unit,
    onSubmit: () -> Unit,
) {
    val medicine = state.medicine
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Medicine") })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                MedicineHeroCard(medicine = medicine)
            }

            when {
                state.errorMessage != null -> {
                    item {
                        DetailsFeedbackCard(
                            title = "Medicine details unavailable",
                            body = "We could not confirm this medicine record right now. ${state.errorMessage}",
                        )
                    }
                }

                state.isLoading -> {
                    item {
                        DetailsFeedbackCard(
                            title = "Loading medicine details",
                            body =
                                "Checking the latest medicine record, verified leaflet state, and " +
                                    "clinical metadata.",
                        )
                    }
                }

                medicine != null -> {
                    item {
                        MedicineOverviewCard(medicine = medicine)
                    }
                    item {
                        LeafletStateCard(
                            medicine = medicine,
                            onOpenLeaflet = onOpenLeaflet,
                        )
                    }
                }

                else -> {
                    item {
                        DetailsFeedbackCard(
                            title = "Medicine record not found",
                            body =
                                "This medicine is not available in the local catalog yet. You can " +
                                    "still submit a leaflet update.",
                        )
                    }
                }
            }

            item {
                MedicineActionsCard(
                    hasVerifiedLeaflet = medicine?.approvedLeaflet != null,
                    onOpenLeaflet = onOpenLeaflet,
                    onSubmit = onSubmit,
                )
            }
        }
    }
}

@Composable
private fun MedicineHeroCard(medicine: MedicineDetailsItem?) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
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
                    Text(
                        text = medicine?.canonicalName ?: "Medicine details",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text =
                            medicine?.brandName
                                ?: medicine?.activeIngredient
                                ?: "Verified leaflet history and clinical metadata will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusChip(
                    label = if (medicine?.approvedLeaflet != null) "Verified leaflet" else "No verified leaflet",
                    tone =
                        if (medicine?.approvedLeaflet != null) {
                            StatusChipTone.APPROVED
                        } else {
                            StatusChipTone.DRAFT
                        },
                )
            }

            Text(
                text =
                    medicine?.description
                        ?: "Review the medicine identity, active ingredient, ATC metadata, and " +
                            "verified leaflet status.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MedicineOverviewCard(medicine: MedicineDetailsItem) {
    RecordCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Clinical record",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            MetadataRow(
                label = "Active ingredient",
                value = medicine.activeIngredient ?: "Not listed",
            )
            MetadataRow(
                label = "ATC code",
                value = medicine.atcCode ?: "Not listed",
            )
            MetadataRow(
                label = "Brand name",
                value = medicine.brandName ?: "Not listed",
            )
            medicine.description?.takeIf { it.isNotBlank() }?.let { description ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LeafletStateCard(
    medicine: MedicineDetailsItem,
    onOpenLeaflet: () -> Unit,
) {
    RecordCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Leaflet status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text =
                                if (medicine.approvedLeaflet != null) {
                                    "This medicine has a verified leaflet ready to open."
                                } else {
                                    "No verified leaflet is available for this medicine yet."
                                },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                StatusChip(
                    label =
                        if (medicine.approvedLeaflet != null) {
                            "Verified leaflet"
                        } else {
                            "Pending leaflet"
                        },
                    tone =
                        if (medicine.approvedLeaflet != null) {
                            StatusChipTone.APPROVED
                        } else {
                            StatusChipTone.PENDING
                        },
                )
            }

            if (medicine.approvedLeaflet != null) {
                MetadataRow(label = "Verified leaflet", value = medicine.approvedLeaflet.title)
                MetadataRow(
                    label = "Last approved",
                    value = medicine.approvedLeaflet.approvedAtLabel,
                )
                MetadataRow(
                    label = "Version",
                    value = "v${medicine.approvedLeaflet.version}",
                )
                OutlinedButton(onClick = onOpenLeaflet) {
                    Icon(imageVector = Icons.Filled.Description, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open leaflet")
                }
            } else {
                Text(
                    text =
                        "Submit an updated leaflet when verified content is not yet available or " +
                            "needs revision.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MedicineActionsCard(
    hasVerifiedLeaflet: Boolean,
    onOpenLeaflet: () -> Unit,
    onSubmit: () -> Unit,
) {
    RecordCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Next actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text =
                    "Open the verified leaflet when it is available, or submit a new proposal for review.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    enabled = hasVerifiedLeaflet,
                    onClick = onOpenLeaflet,
                ) {
                    Text(if (hasVerifiedLeaflet) "Open leaflet" else "No verified leaflet")
                }
                Button(onClick = onSubmit) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit proposal")
                }
            }
        }
    }
}

@Composable
private fun DetailsFeedbackCard(
    title: String,
    body: String,
) {
    RecordCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
