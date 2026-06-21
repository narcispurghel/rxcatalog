package com.github.narcispurghel.rxcatalog.ui.components.search

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.components.common.MetadataRow
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChip
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChipTone
import com.github.narcispurghel.rxcatalog.ui.viewmodels.SearchResultItem

@Composable
fun MedicineSearchCard(
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
				verticalAlignment = androidx.compose.ui.Alignment.Top,
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
					value = if (medicine.hasApprovedLeaflet) "Approved" else "Not approved",
				)
			}
			Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
				OutlinedButton(onClick = { onMedicine(medicine.medicineId) }) {
					Spacer(modifier = Modifier.width(8.dp))
					Text("Open record")
				}
			}
		}
	}
}

@Composable
private fun DataPill(text: String) {
	androidx.compose.material3.SuggestionChip(
		onClick = { /* Informational chip only. */ },
		enabled = false,
		label = { Text(text) },
	)
}

private fun SearchResultItem.leafletStatusLabel(): String =
	if (hasApprovedLeaflet) {
		"Approved leaflet"
	} else {
		"No approved leaflet"
	}

private fun SearchResultItem.leafletStatusTone(): StatusChipTone =
	if (hasApprovedLeaflet) {
		StatusChipTone.REVIEWER
	} else {
		StatusChipTone.DRAFT
	}
