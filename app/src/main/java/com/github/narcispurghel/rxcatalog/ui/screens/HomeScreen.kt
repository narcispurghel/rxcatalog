@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.R
import com.github.narcispurghel.rxcatalog.auth.AuthenticatedUser
import com.github.narcispurghel.rxcatalog.auth.SessionState
import com.github.narcispurghel.rxcatalog.catalog.CatalogSeedIds
import com.github.narcispurghel.rxcatalog.common.UserRole
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChip
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChipTone
import com.github.narcispurghel.rxcatalog.ui.theme.RxCatalogTheme
import com.github.narcispurghel.rxcatalog.ui.viewmodels.HomeFeaturedMedicineItem
import com.github.narcispurghel.rxcatalog.ui.viewmodels.HomeUiState
import kotlin.uuid.Uuid

@Composable
fun HomeScreen(
	sessionState: SessionState,
	currentUser: AuthenticatedUser?,
	homeState: HomeUiState,
	onSearch: () -> Unit,
	onSubmit: () -> Unit,
	onApprovals: () -> Unit,
	onProfile: () -> Unit,
	onMedicine: (String) -> Unit,
) {
	val isReviewer = currentUser?.role in reviewerRoles
	val roleLabel = currentUser?.role?.toRoleLabel() ?: "No role"

	LazyColumn(
		modifier = Modifier.fillMaxSize(),
		contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 28.dp),
		verticalArrangement = Arrangement.spacedBy(20.dp),
	) {
		item {
			HomeTopBar(
				currentUser = currentUser,
				roleLabel = roleLabel,
				onProfile = onProfile,
			)
		}

		item {
			HomeHero(
				displayName = currentUser?.displayName.orEmpty(),
				isReviewer = isReviewer,
				homeState = homeState,
				onSearch = onSearch,
				onSubmit = onSubmit,
			)
		}

		item {
			QuickActionSection(
				isReviewer = isReviewer,
				onSearch = onSearch,
				onSubmit = onSubmit,
				onApprovals = onApprovals,
				onProfile = onProfile,
			)
		}

		if (isReviewer) {
			item {
				ReviewerPanel(onApprovals = onApprovals)
			}
		}

		item {
			FeaturedMedicinesSection(
				homeState = homeState,
				onMedicine = onMedicine,
			)
		}

		item {
			WorkspaceStatusPanel(
				sessionState = sessionState,
				currentUser = currentUser,
				roleLabel = roleLabel,
			)
		}
	}
}

@Composable
private fun HomeTopBar(
	currentUser: AuthenticatedUser?,
	roleLabel: String,
	onProfile: () -> Unit,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween,
		modifier = Modifier.fillMaxWidth(),
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(14.dp),
			modifier = Modifier.weight(1f),
		) {
			Image(
				painter =
					painterResource(
						if (isSystemInDarkTheme()) {
							R.drawable.rxcatalog_logo_dark
						} else {
							R.drawable.rxcatalog_logo_light
						},
					),
				modifier = Modifier.size(52.dp),
				contentScale = ContentScale.FillBounds,
				contentDescription = null,
			)
			Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
				Row {
					Text(
						text = "Rx",
						style = MaterialTheme.typography.titleLarge,
						color = MaterialTheme.colorScheme.primary,
					)
					Text(
						text = "Catalog",
						style = MaterialTheme.typography.titleLarge,
						color = MaterialTheme.colorScheme.onSurface,
					)
				}
				Text(
					text = roleLabel,
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}
		FilledTonalIconButton(
			onClick = onProfile,
			modifier = Modifier.size(48.dp),
		) {
			Icon(
				imageVector = Icons.Filled.AccountCircle,
				contentDescription = "Open profile",
			)
		}
	}
}

@Composable
private fun HomeHero(
	displayName: String,
	isReviewer: Boolean,
	homeState: HomeUiState,
	onSearch: () -> Unit,
	onSubmit: () -> Unit,
) {
	val heroShape = RoundedCornerShape(28.dp)

	Box(
		modifier =
			Modifier
				.fillMaxWidth()
				.clip(heroShape)
				.background(MaterialTheme.colorScheme.surfaceVariant)
				.padding(22.dp),
	) {
		Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				StatusChip(
					label = if (isReviewer) "Reviewer workspace" else "Catalog workspace",
					tone = if (isReviewer) StatusChipTone.REVIEWER else StatusChipTone.PENDING,
					icon = if (isReviewer) Icons.Outlined.VerifiedUser else Icons.Filled.Search,
				)
				Text(
					text = greetingTitle(displayName),
					style = MaterialTheme.typography.headlineMedium,
					color = MaterialTheme.colorScheme.onSurface,
				)
				Text(
					text =
						if (isReviewer) {
							"Search verified records, triage leaflet updates, and keep medicine details current."
						} else {
							"Find medicines, inspect leaflets, and send clear update requests for review."
						},
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}

			Row(
				horizontalArrangement = Arrangement.spacedBy(10.dp),
				modifier = Modifier.fillMaxWidth(),
			) {
				Button(
					onClick = onSearch,
					modifier = Modifier.weight(1f),
					contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
				) {
					Icon(Icons.Filled.Search, contentDescription = null)
					Spacer(Modifier.width(8.dp))
					Text("Search")
				}
				FilledTonalButton(
					onClick = onSubmit,
					modifier = Modifier.weight(1f),
					contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
				) {
					Icon(Icons.Filled.UploadFile, contentDescription = null)
					Spacer(Modifier.width(8.dp))
					Text("Submit")
				}
			}

			Row(
				horizontalArrangement = Arrangement.spacedBy(10.dp),
				modifier = Modifier.fillMaxWidth(),
			) {
				HeroMetric(
					value = homeState.medicineCount.toStatValue(homeState.isLoading),
					label = "medicine records",
					icon = Icons.Filled.Medication,
					modifier = Modifier.weight(1f),
				)
				HeroMetric(
					value = homeState.pendingApprovalsCount.toStatValue(homeState.isLoading),
					label = if (isReviewer) "pending reviews" else "my pending",
					icon = if (isReviewer) Icons.Filled.PendingActions else Icons.Filled.EditNote,
					modifier = Modifier.weight(1f),
				)
			}
		}
	}
}

@Composable
private fun HeroMetric(
	value: String,
	label: String,
	icon: ImageVector,
	modifier: Modifier = Modifier,
) {
	Surface(
		modifier = modifier.heightIn(min = 76.dp),
		shape = RoundedCornerShape(18.dp),
		color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
		contentColor = MaterialTheme.colorScheme.onSurface,
	) {
		Row(
			modifier = Modifier.padding(12.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(10.dp),
		) {
			Box(
				modifier =
					Modifier
						.size(36.dp)
						.clip(CircleShape)
						.background(MaterialTheme.colorScheme.primaryContainer),
				contentAlignment = Alignment.Center,
			) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onPrimaryContainer,
					modifier = Modifier.size(20.dp),
				)
			}
			Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
				Text(text = value, style = MaterialTheme.typography.titleMedium)
				Text(
					text = label,
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}
	}
}

@Composable
private fun QuickActionSection(
	isReviewer: Boolean,
	onSearch: () -> Unit,
	onSubmit: () -> Unit,
	onApprovals: () -> Unit,
	onProfile: () -> Unit,
) {
	Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
		SectionTitle(
			title = "Quick actions",
			subtitle = "Common paths for today",
		)
		Row(
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			modifier = Modifier.fillMaxWidth(),
		) {
			QuickActionCard(
				title = "Catalog",
				subtitle = "Search records",
				icon = Icons.Filled.Search,
				onClick = onSearch,
				modifier = Modifier.weight(1f),
			)
			QuickActionCard(
				title = "Leaflet",
				subtitle = "Submit update",
				icon = Icons.Filled.MedicalServices,
				onClick = onSubmit,
				modifier = Modifier.weight(1f),
			)
		}
		Row(
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			modifier = Modifier.fillMaxWidth(),
		) {
			QuickActionCard(
				title = if (isReviewer) "Approvals" else "Browse",
				subtitle = if (isReviewer) "Review queue" else "Find medicines",
				icon = if (isReviewer) Icons.Filled.PendingActions else Icons.Filled.Info,
				onClick = if (isReviewer) onApprovals else onSearch,
				modifier = Modifier.weight(1f),
				emphasized = isReviewer,
			)
			QuickActionCard(
				title = "Profile",
				subtitle = "Session details",
				icon = Icons.Filled.AccountCircle,
				onClick = onProfile,
				modifier = Modifier.weight(1f),
			)
		}
	}
}

@Composable
private fun QuickActionCard(
	title: String,
	subtitle: String,
	icon: ImageVector,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	emphasized: Boolean = false,
) {
	ElevatedCard(
		onClick = onClick,
		modifier = modifier.height(132.dp),
		shape = RoundedCornerShape(22.dp),
		colors =
			CardDefaults.elevatedCardColors(
				containerColor =
					if (emphasized) {
						MaterialTheme.colorScheme.primaryContainer
					} else {
						MaterialTheme.colorScheme.surface
					},
			),
	) {
		Column(
			modifier =
				Modifier
					.fillMaxSize()
					.padding(16.dp),
			verticalArrangement = Arrangement.SpaceBetween,
		) {
			Box(
				modifier =
					Modifier
						.size(42.dp)
						.clip(CircleShape)
						.background(
							if (emphasized) {
								MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
							} else {
								MaterialTheme.colorScheme.surfaceVariant
							},
						),
				contentAlignment = Alignment.Center,
			) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					tint =
						if (emphasized) {
							MaterialTheme.colorScheme.onPrimaryContainer
						} else {
							MaterialTheme.colorScheme.primary
						},
				)
			}
			Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
				Text(
					text = title,
					style = MaterialTheme.typography.titleMedium,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
				Text(
					text = subtitle,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}
	}
}

@Composable
private fun ReviewerPanel(onApprovals: () -> Unit) {
	Surface(
		shape = RoundedCornerShape(24.dp),
		color = MaterialTheme.colorScheme.surface,
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
		modifier = Modifier.fillMaxWidth(),
	) {
		Column(
			modifier = Modifier.padding(18.dp),
			verticalArrangement = Arrangement.spacedBy(14.dp),
		) {
			Row(
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				verticalAlignment = Alignment.Top,
				modifier = Modifier.fillMaxWidth(),
			) {
				Box(
					modifier =
						Modifier
							.size(44.dp)
							.clip(CircleShape)
							.background(MaterialTheme.colorScheme.primaryContainer),
					contentAlignment = Alignment.Center,
				) {
					Icon(
						imageVector = Icons.Outlined.VerifiedUser,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onPrimaryContainer,
					)
				}
				Column(
					modifier = Modifier.weight(1f),
					verticalArrangement = Arrangement.spacedBy(4.dp),
				) {
					Text("Reviewer queue", style = MaterialTheme.typography.titleMedium)
					Text(
						text = "Pending leaflet updates are waiting for clinical verification.",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				}
			}
			Button(onClick = onApprovals, modifier = Modifier.fillMaxWidth()) {
				Text("Open approvals")
				Spacer(Modifier.width(8.dp))
				Icon(
					imageVector = Icons.AutoMirrored.Filled.ArrowForward,
					contentDescription = null,
					modifier = Modifier.size(18.dp),
				)
			}
		}
	}
}

@Composable
private fun FeaturedMedicinesSection(
	homeState: HomeUiState,
	onMedicine: (String) -> Unit,
) {
	Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
		SectionTitle(
			title = "Featured medicines",
			subtitle = "Latest catalog records",
		)
		Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
			when {
				homeState.isLoading -> {
					repeat(3) {
						FeaturedMedicinePlaceholder()
					}
				}

				homeState.featuredMedicines.isEmpty() -> {
					EmptyFeaturedMedicinesCard()
				}

				else -> {
					homeState.featuredMedicines.forEachIndexed { index, medicine ->
						FeaturedMedicineRow(
							name = medicine.name,
							detail = medicine.detail,
							status = if (medicine.hasPendingReview) "Pending review" else "Catalog",
							icon = medicine.featuredIcon(index),
							onClick = { onMedicine(medicine.medicineId) },
						)
					}
				}
			}
		}
	}
}

@Composable
private fun FeaturedMedicineRow(
	name: String,
	detail: String,
	status: String,
	icon: ImageVector,
	onClick: () -> Unit,
) {
	OutlinedCard(
		onClick = onClick,
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(20.dp),
	) {
		Row(
			modifier = Modifier.padding(14.dp),
			verticalAlignment = Alignment.Top,
			horizontalArrangement = Arrangement.spacedBy(12.dp),
		) {
			Box(
				modifier =
					Modifier
						.size(44.dp)
						.clip(CircleShape)
						.background(MaterialTheme.colorScheme.surfaceVariant),
				contentAlignment = Alignment.Center,
			) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary,
				)
			}
			Column(
				modifier = Modifier.weight(1f),
				verticalArrangement = Arrangement.spacedBy(3.dp),
			) {
				Text(
					text = name,
					style = MaterialTheme.typography.titleMedium,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
				Text(
					text = detail,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}
			StatusChip(
				label = status,
				tone =
					if (status == "Pending review") {
						StatusChipTone.PENDING
					} else {
						StatusChipTone.DRAFT
					},
				modifier = Modifier.align(Alignment.Top),
			)
		}
	}
}

@Composable
private fun FeaturedMedicinePlaceholder() {
	Surface(
		modifier =
			Modifier
				.fillMaxWidth()
				.height(86.dp),
		shape = RoundedCornerShape(20.dp),
		color = MaterialTheme.colorScheme.surfaceContainerLow,
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
	) {
		Row(
			modifier = Modifier.padding(14.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp),
		) {
			Box(
				modifier =
					Modifier
						.size(44.dp)
						.clip(CircleShape)
						.background(MaterialTheme.colorScheme.surfaceVariant),
			)
			Column(
				verticalArrangement = Arrangement.spacedBy(8.dp),
				modifier = Modifier.weight(1f),
			) {
				Box(
					modifier =
						Modifier
							.fillMaxWidth(0.55f)
							.height(16.dp)
							.clip(RoundedCornerShape(8.dp))
							.background(MaterialTheme.colorScheme.surfaceVariant),
				)
				Box(
					modifier =
						Modifier
							.fillMaxWidth(0.8f)
							.height(12.dp)
							.clip(RoundedCornerShape(6.dp))
							.background(MaterialTheme.colorScheme.surfaceVariant),
				)
			}
		}
	}
}

@Composable
private fun EmptyFeaturedMedicinesCard() {
	OutlinedCard(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(20.dp),
	) {
		Text(
			text = "No catalog medicines available yet.",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.padding(18.dp),
		)
	}
}

@Composable
private fun WorkspaceStatusPanel(
	sessionState: SessionState,
	currentUser: AuthenticatedUser?,
	roleLabel: String,
) {
	Surface(
		shape = RoundedCornerShape(24.dp),
		color = MaterialTheme.colorScheme.surfaceContainerLow,
		modifier = Modifier.fillMaxWidth(),
	) {
		Column(
			modifier = Modifier.padding(18.dp),
			verticalArrangement = Arrangement.spacedBy(14.dp),
		) {
			SectionTitle(
				title = "Workspace",
				subtitle = "Signed-in context",
			)
			StatusChip(
				label =
					when (sessionState) {
						is SessionState.Authenticated -> "Signed in"
						SessionState.Loading -> "Checking session"
						SessionState.Unauthenticated -> "Signed out"
					},
				tone = StatusChipTone.APPROVED,
				icon = Icons.Filled.CheckCircle,
			)
			AccountInfoRow(
				icon = Icons.Filled.AccountCircle,
				label = "Display name",
				value = currentUser?.displayName ?: "Unavailable",
			)
			AccountInfoRow(
				icon = Icons.Outlined.Email,
				label = "Account",
				value = currentUser?.email ?: "Unavailable",
			)
			AccountInfoRow(
				icon = Icons.Outlined.VerifiedUser,
				label = "Access",
				value = roleLabel,
			)
		}
	}
}

@Composable
private fun AccountInfoRow(
	icon: ImageVector,
	label: String,
	value: String,
) {
	Row(
		horizontalArrangement = Arrangement.spacedBy(10.dp),
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier.fillMaxWidth(),
	) {
		Box(
			modifier =
				Modifier
					.size(38.dp)
					.clip(CircleShape)
					.background(MaterialTheme.colorScheme.surface),
			contentAlignment = Alignment.Center,
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.size(20.dp),
			)
		}
		Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
			Text(
				text = label,
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
			Text(
				text = value,
				style = MaterialTheme.typography.bodyMedium,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
	}
}

@Composable
private fun SectionTitle(
	title: String,
	subtitle: String,
) {
	Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleLarge,
			color = MaterialTheme.colorScheme.onSurface,
		)
		Text(
			text = subtitle,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
		)
	}
}

private fun greetingTitle(displayName: String): String =
	if (displayName.isBlank()) {
		"Medicine intelligence, ready when you are."
	} else {
		"Welcome back, ${displayName.substringBefore(' ')}."
	}

private fun Int.toStatValue(isLoading: Boolean): String =
	if (isLoading) {
		"..."
	} else {
		toString()
	}

private fun HomeFeaturedMedicineItem.featuredIcon(index: Int): ImageVector =
	when {
		hasPendingReview -> Icons.Filled.PendingActions
		name.contains("para", ignoreCase = true) -> Icons.Filled.Science
		name.contains("ibu", ignoreCase = true) -> Icons.Filled.Medication
		index % 3 == 0 -> Icons.Filled.LocalPharmacy
		index % 3 == 1 -> Icons.Filled.Medication
		else -> Icons.Filled.Science
	}

private val reviewerRoles = setOf(UserRole.DOCTOR, UserRole.PHARMACIST)

private fun UserRole.toRoleLabel(): String =
	when (this) {
		UserRole.USER -> "User access"
		UserRole.DOCTOR -> "Doctor reviewer"
		UserRole.PHARMACIST -> "Pharmacist reviewer"
	}

private val previewUser =
	AuthenticatedUser(
		userId = Uuid.parse(CatalogSeedIds.DOCTOR_USER_ID),
		email = "doctor.popescu@rxcatalog.test",
		displayName = "Dr. Andrei Popescu",
		role = UserRole.DOCTOR,
	)

@Preview(
	device = Devices.PIXEL_9_PRO_XL,
	showSystemUi = true,
	wallpaper = Wallpapers.NONE,
)
@Composable
fun ActionCardPreview() {
	RxCatalogTheme {
		val snackbarHostState = remember { SnackbarHostState() }

		Scaffold(
			containerColor = MaterialTheme.colorScheme.background,
			snackbarHost = { SnackbarHost(snackbarHostState) },
		) { outerPadding ->
			Box(modifier = Modifier.padding(outerPadding)) {
				HomeScreen(
					sessionState = SessionState.Authenticated(previewUser),
					currentUser = previewUser,
					homeState =
						HomeUiState(
							isLoading = false,
							medicineCount = 4,
							pendingApprovalsCount = 2,
							featuredMedicines =
								listOf(
									HomeFeaturedMedicineItem(
										medicineId = CatalogSeedIds.ASPIRIN_MEDICINE_ID,
										name = "Aspirin",
										detail = "Acetylsalicylic acid",
										hasPendingReview = true,
									),
									HomeFeaturedMedicineItem(
										medicineId = CatalogSeedIds.PARACETAMOL_MEDICINE_ID,
										name = "Paracetamol",
										detail = "Paracetamol",
										hasPendingReview = true,
									),
									HomeFeaturedMedicineItem(
										medicineId = CatalogSeedIds.IBUPROFEN_MEDICINE_ID,
										name = "Ibuprofen",
										detail = "Ibuprofen",
										hasPendingReview = false,
									),
								),
						),
					onSearch = {},
					onSubmit = {},
					onApprovals = {},
					onProfile = {},
					onMedicine = {},
				)
			}
		}
	}
}
