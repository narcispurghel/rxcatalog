@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.catalog.LeafletDetailsItem
import com.github.narcispurghel.rxcatalog.ui.components.common.MetadataRow
import com.github.narcispurghel.rxcatalog.ui.components.common.RecordCard
import com.github.narcispurghel.rxcatalog.ui.components.common.RxCatalogTopAppBar
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChip
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChipTone
import com.github.narcispurghel.rxcatalog.ui.viewmodels.LeafletDetailsUiState

@Composable
fun LeafletDetailsScreen(state: LeafletDetailsUiState) {
	val leaflet = state.leaflet
	Column(modifier = Modifier.fillMaxSize()) {
		RxCatalogTopAppBar(title = "Leaflet")
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp),
		) {
			item {
				LeafletHeroCard(leaflet = leaflet)
			}

			when {
				state.errorMessage != null -> {
					item {
						LeafletFeedbackCard(
							title = "Leaflet unavailable",
							body =
								"We could not confirm the verified leaflet details right now. " +
									state.errorMessage,
						)
					}
				}

				state.isLoading -> {
					item {
						LeafletFeedbackCard(
							title = "Loading verified leaflet",
							body =
								"Checking the latest approved leaflet content, version, and review timestamp.",
						)
					}
				}

				leaflet != null -> {
					item {
						LeafletRecordCard(leaflet = leaflet)
					}
					item {
						LeafletContentCard(leaflet = leaflet)
					}
				}

				else -> {
					item {
						LeafletFeedbackCard(
							title = "No verified leaflet found",
							body =
								"A verified leaflet has not been recorded for this medicine in the local catalog.",
						)
					}
				}
			}
		}
	}
}

@Composable
private fun LeafletHeroCard(leaflet: LeafletDetailsItem?) {
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
						text = leaflet?.medicineName ?: "Leaflet details",
						style = MaterialTheme.typography.headlineSmall,
						fontWeight = FontWeight.SemiBold,
					)
					Text(
						text =
							leaflet?.title
								?: "Verified leaflet content and approval timing will appear here.",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				}
				StatusChip(
					label = if (leaflet != null) "Verified leaflet" else "Awaiting leaflet",
					tone =
						if (leaflet != null) {
							StatusChipTone.APPROVED
						} else {
							StatusChipTone.PENDING
						},
				)
			}
			Text(
				text =
					if (leaflet != null) {
						"Read the approved leaflet version for ${leaflet.medicineName}."
					} else {
						"Review the verified leaflet title, version, approval timing, and content."
					},
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}
	}
}

@Composable
private fun LeafletRecordCard(leaflet: LeafletDetailsItem) {
	RecordCard {
		Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
						text = "Verified leaflet record",
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.SemiBold,
					)
					Text(
						text = "This is the approved leaflet currently available in the catalog.",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				}
			}

			MetadataRow(label = "Leaflet title", value = leaflet.title)
			MetadataRow(label = "Medicine", value = leaflet.medicineName)
			MetadataRow(label = "Last approved", value = leaflet.approvedAtLabel)
			MetadataRow(label = "Version", value = "v${leaflet.version}")
		}
	}
}

@Composable
private fun LeafletContentCard(leaflet: LeafletDetailsItem) {
	RecordCard {
		Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
			Row(
				horizontalArrangement = Arrangement.spacedBy(10.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Icon(
					imageVector = Icons.Filled.Description,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary,
				)
				Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
					Text(
						text = "Leaflet content",
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.SemiBold,
					)
					Text(
						text = "Review the current verified wording before making a new submission.",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				}
			}

			HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
			Text(
				text = leaflet.content,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface,
			)
		}
	}
}

@Composable
private fun LeafletFeedbackCard(
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
