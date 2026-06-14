package com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.components.common.ActionCard
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChip
import com.github.narcispurghel.rxcatalog.ui.components.common.StatusChipTone
import com.github.narcispurghel.rxcatalog.ui.theme.RxCatalogTheme

@Composable
fun ApprovalQueueCard(
	item: ApprovalQueueItem,
	onReview: (String) -> Unit,
) {
	OutlinedCard(
		modifier = Modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.extraLarge,
	) {
		Column(
			modifier = Modifier.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			ListItem(
				modifier = Modifier.padding(0.dp),
				leadingContent = {
					Icon(
						imageVector = Icons.Filled.Description,
						contentDescription = null,
					)
				},
				headlineContent = {
					Text(
						text = item.medicineName,
					)
				},
				supportingContent = {
					Text(
						text = item.submissionId,
						style = MaterialTheme.typography.labelMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				},
			)

			Row(
				horizontalArrangement = Arrangement.spacedBy(8.dp),
			) {
				StatusChip(
					label = "Pending review",
					tone = StatusChipTone.PENDING,
				)
				if (item.isUrgent) {
					StatusChip(
						label = "Urgent",
						tone = StatusChipTone.URGENT,
					)
				}
			}

			HorizontalDivider()

			Column(
				verticalArrangement = Arrangement.spacedBy(8.dp),
			) {
				MetadataListItem(
					icon = Icons.Filled.Person,
					label = "Submitted by",
					value = item.submittedBy,
				)
				MetadataListItem(
					icon = Icons.Filled.AccessTime,
					label = "Received",
					value = item.createdAtLabel,
				)
				MetadataListItem(
					icon = Icons.Filled.Description,
					label = "Workflow",
					value = if (item.isUrgent) "Priority queue" else "Standard queue",
				)
			}

			Button(onClick = { onReview(item.submissionId) }) {
				Text("Review")
				Spacer(modifier = Modifier.width(8.dp))
				Icon(
					imageVector = Icons.AutoMirrored.Filled.ArrowForward,
					contentDescription = null,
				)
			}
		}
	}
}

@Composable
private fun MetadataListItem(
	icon: ImageVector,
	label: String,
	value: String,
) {
	ListItem(
		modifier = Modifier.padding(0.dp),
		leadingContent = {
			Icon(
				imageVector = icon,
				contentDescription = null,
			)
		},
		headlineContent = {
			Text(text = label)
		},
		supportingContent = {
			Text(text = value)
		},
	)
}

@Preview(
	device = Devices.PIXEL_9_PRO_XL,
	showSystemUi = true,
	uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
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
				ApprovalQueueCard(
					item =
						ApprovalQueueItem(
							submissionId = "mock-id",
							medicineName = "Mock medicine",
							submittedBy = "mock-id",
							createdAtLabel = "00 00 00",
							isUrgent = true,
						),
				) {
				}
			}
		}
	}
}
