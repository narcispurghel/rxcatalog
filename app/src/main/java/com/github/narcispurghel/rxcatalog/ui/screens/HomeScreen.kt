@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.*
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.R
import com.github.narcispurghel.rxcatalog.auth.AuthenticatedUser
import com.github.narcispurghel.rxcatalog.auth.SessionState
import com.github.narcispurghel.rxcatalog.catalog.CatalogSeedIds
import com.github.narcispurghel.rxcatalog.common.UserRole
import com.github.narcispurghel.rxcatalog.ui.components.common.MetadataRow
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChip
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChipTone
import com.github.narcispurghel.rxcatalog.ui.theme.RxCatalogTheme
import kotlin.uuid.Uuid

@Composable
fun HomeScreen(
	sessionState: SessionState,
	currentUser: AuthenticatedUser?,
	onSearch: () -> Unit,
	onSubmit: () -> Unit,
	onApprovals: () -> Unit,
	onProfile: () -> Unit,
	onMedicine: (String) -> Unit,
) {
	Column(
		modifier =
			Modifier
				.fillMaxSize()
				.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
		verticalArrangement = Arrangement.spacedBy(20.dp),
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween,
			modifier = Modifier.fillMaxWidth(),
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(20.dp),
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
					modifier = Modifier.size(60.dp),
					contentScale = ContentScale.FillBounds,
					contentDescription = null,
				)
				Row {
					Text(
						"Rx",
						style = MaterialTheme.typography.headlineMedium,
						color = MaterialTheme.colorScheme.primary,
					)
					Text("Catalog", style = MaterialTheme.typography.headlineMedium)
				}
			}
		}

		Text(
			text = "Search medicines, submit leaflet updates, and review verified information from one workspace.",
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
		)
		LazyColumn(
			modifier =
				Modifier
					.weight(1f)
					.fillMaxWidth(),
			verticalArrangement = Arrangement.spacedBy(16.dp),
		) {
			item {
				OutlinedCard(
					modifier = Modifier.fillMaxWidth(),
					shape = MaterialTheme.shapes.extraLarge,
				) {
					Column(
						modifier = Modifier.padding(20.dp),
						verticalArrangement = Arrangement.spacedBy(4.dp),
					) {
						Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
							Text(
								text = "Your workspace",
								style = MaterialTheme.typography.titleMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
							)
							Text(
								text =
									if (currentUser?.role in setOf(UserRole.DOCTOR, UserRole.PHARMACIST)) {
										"You're signed in with reviewer access."
									} else {
										"You're signed in and can browse medicines and submit updates."
									},
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
							)
						}
						Row(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							modifier = Modifier.fillMaxWidth(),
						) {
							AssistChip(onClick = {}, label = {
								Text("Signed in")
							})
							StatusChip(
								label = currentUser?.role?.toRoleLabel() ?: "No role",
								tone =
									currentUser?.role?.toStatusChipTone()
										?: StatusChipTone.DRAFT,
							)
						}
						HorizontalDivider(modifier = Modifier.padding(bottom = 10.dp))
						Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
							Row(
								horizontalArrangement = Arrangement.spacedBy(8.dp),
								verticalAlignment = Alignment.CenterVertically,
							) {
								IconButton(
									onClick = { },
									colors =
										IconButtonDefaults.iconButtonColors().copy(
											containerColor = MaterialTheme.colorScheme.surfaceVariant,
										),
								) {
									Icon(
										imageVector = Icons.Default.AccountCircle,
										tint = MaterialTheme.colorScheme.onSurfaceVariant,
										contentDescription = null,
									)
								}
								Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
									Text(
										"Display name:",
										style = MaterialTheme.typography.bodySmall,
										color = MaterialTheme.colorScheme.onSurfaceVariant,
									)
									Text(
										currentUser?.displayName ?: "Unavailable",
										style = MaterialTheme.typography.bodyMedium,
									)
								}
							}
							Row(
								horizontalArrangement = Arrangement.spacedBy(8.dp),
								verticalAlignment = Alignment.CenterVertically,
							) {
								IconButton(
									onClick = { },
									colors =
										IconButtonDefaults.iconButtonColors().copy(
											containerColor = MaterialTheme.colorScheme.surfaceVariant,
										),
								) {
									Icon(
										imageVector = Icons.Outlined.Email,
										tint = MaterialTheme.colorScheme.onSurfaceVariant,
										contentDescription = null,
									)
								}
								Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
									Text(
										"Account:",
										style = MaterialTheme.typography.bodySmall,
										color = MaterialTheme.colorScheme.onSurfaceVariant,
									)
									Text(
										currentUser?.email ?: "Unavailable",
										style = MaterialTheme.typography.bodyMedium,
									)
								}
							}
						}
					}
				}
			}
			item {
				HomeActionCard(
					title = "Search medicines",
					subtitle = "Browse catalog records by name, brand, ingredient, or ATC code.",
					label = "Open catalog",
					icon = Icons.Filled.Search,
					onClick = onSearch,
				)
			}
			item {
				HomeActionCard(
					title = "Submit leaflet",
					subtitle = "Prepare a leaflet update for reviewer verification.",
					label = "Open submission form",
					icon = Icons.Filled.MedicalServices,
					onClick = onSubmit,
				)
			}
			if (currentUser?.role in setOf(UserRole.DOCTOR, UserRole.PHARMACIST)) {
				item {
					HomeActionCard(
						title = "Review queue",
						subtitle = "Review pending submissions and confirm verified information.",
						label = "View pending approvals",
						icon = Icons.Filled.PendingActions,
						onClick = onApprovals,
						badge = "Reviewer access",
					)
				}
			}
			item {
				HomeActionCard(
					title = "Profile",
					subtitle = "Check session details and manage access.",
					label = "Open profile",
					icon = Icons.Filled.AccountCircle,
					onClick = onProfile,
				)
			}
			item {
				HomeActionCard(
					title = "Sample medicine",
					subtitle = "Jump into a seeded medicine record for UI inspection.",
					label = "Open record",
					icon = Icons.Filled.Info,
					onClick = { onMedicine(CatalogSeedIds.ASPIRIN_MEDICINE_ID) },
					secondary = true,
				)
			}
		}
	}
}

@Composable
private fun HomeActionCard(
	title: String,
	subtitle: String,
	label: String,
	icon: ImageVector,
	onClick: () -> Unit,
	badge: String? = null,
	secondary: Boolean = false,
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
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				verticalAlignment = Alignment.Top,
			) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					modifier = Modifier.size(32.dp),
					tint = MaterialTheme.colorScheme.primary,
				)
				Column(
					modifier = Modifier.weight(1f),
					verticalArrangement = Arrangement.spacedBy(6.dp),
				) {
					badge?.let {
						StatusChip(
							label = it,
							tone = StatusChipTone.REVIEWER,
						)
					}
					Text(text = title, style = MaterialTheme.typography.titleMedium)
					Text(
						text = subtitle,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				}
			}
			if (secondary) {
				OutlinedButton(onClick = onClick) {
					Text(label)
					Spacer(modifier = Modifier.width(8.dp))
					Icon(
						imageVector = Icons.AutoMirrored.Filled.ArrowForward,
						contentDescription = null,
					)
				}
			} else {
				OutlinedButton(onClick = onClick) {
					Text(label)
				}
			}
		}
	}
}

private fun UserRole.toRoleLabel(): String =
	when (this) {
		UserRole.USER -> "User access"
		UserRole.DOCTOR -> "Doctor reviewer"
		UserRole.PHARMACIST -> "Pharmacist reviewer"
	}

private fun UserRole.toStatusChipTone(): StatusChipTone =
	when (this) {
		UserRole.USER -> StatusChipTone.DRAFT

		UserRole.DOCTOR,
		UserRole.PHARMACIST,
		-> StatusChipTone.REVIEWER
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
// 	uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
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
