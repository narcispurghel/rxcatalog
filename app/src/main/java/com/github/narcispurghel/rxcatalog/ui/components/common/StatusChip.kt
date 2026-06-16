package com.github.narcispurghel.rxcatalog.ui.components.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

enum class StatusChipTone {
	APPROVED,
	PENDING,
	NEEDS_REVISION,
	DRAFT,
	URGENT,
	REVIEWER,
}

@Composable
fun StatusChip(
	label: String,
	tone: StatusChipTone,
	modifier: Modifier = Modifier,
	icon: ImageVector? = null,
) {
	AssistChip(
		onClick = {},
		modifier = modifier,
		enabled = false,
		label = {
			Text(
				text = label,
				style = MaterialTheme.typography.labelMedium,
			)
		},
		leadingIcon =
			icon?.let { chipIcon ->
				{
					Icon(
						imageVector = chipIcon,
						contentDescription = null,
					)
				}
			} ?: toneIcon(tone)?.let { toneIcon ->
				{
					Icon(
						imageVector = toneIcon,
						tint =
							if (tone == StatusChipTone.REVIEWER) {
								MaterialTheme.colorScheme.secondary
							} else {
								MaterialTheme.colorScheme.primary
							},
						contentDescription = null,
					)
				}
			},
	)
}

private fun toneIcon(tone: StatusChipTone): ImageVector? =
	when (tone) {
		StatusChipTone.APPROVED -> Icons.Filled.CheckCircle
		StatusChipTone.PENDING -> Icons.Filled.Schedule
		StatusChipTone.NEEDS_REVISION -> Icons.Filled.EditNote
		StatusChipTone.DRAFT -> Icons.Filled.EditNote
		StatusChipTone.URGENT -> Icons.Filled.PriorityHigh
		StatusChipTone.REVIEWER -> Icons.Filled.VerifiedUser
	}
