package com.github.narcispurghel.rxcatalog.ui.components.common

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
@Suppress("UNUSED_PARAMETER")
fun StatusChip(
	label: String,
	tone: StatusChipTone,
	modifier: Modifier = Modifier,
	icon: ImageVector? = null,
) {
	val palette = tonePalette(tone)

	AssistChip(
		onClick = {},
		modifier = modifier,
		enabled = false,
		colors =
			AssistChipDefaults.assistChipColors(
				containerColor = palette.container,
				labelColor = palette.content,
				leadingIconContentColor = palette.content,
				disabledContainerColor = palette.container,
				disabledLabelColor = palette.content,
				disabledLeadingIconContentColor = palette.content,
			),
		label = {
			Text(
				text = label,
				style = MaterialTheme.typography.labelMedium,
			)
		},
	)
}

private data class TonePalette(
	val container: Color,
	val content: Color,
)

@Composable
private fun tonePalette(tone: StatusChipTone): TonePalette =
	when (tone) {
		StatusChipTone.APPROVED -> {
			TonePalette(
				container = MaterialTheme.colorScheme.tertiaryContainer,
				content = MaterialTheme.colorScheme.onTertiaryContainer,
			)
		}

		StatusChipTone.PENDING -> {
			TonePalette(
				container = MaterialTheme.colorScheme.secondaryContainer,
				content = MaterialTheme.colorScheme.onSecondaryContainer,
			)
		}

		StatusChipTone.NEEDS_REVISION -> {
			TonePalette(
				container = MaterialTheme.colorScheme.errorContainer,
				content = MaterialTheme.colorScheme.onErrorContainer,
			)
		}

		StatusChipTone.DRAFT -> {
			TonePalette(
				container = MaterialTheme.colorScheme.surfaceVariant,
				content = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}

		StatusChipTone.URGENT -> {
			TonePalette(
				container = MaterialTheme.colorScheme.errorContainer,
				content = MaterialTheme.colorScheme.onErrorContainer,
			)
		}

		StatusChipTone.REVIEWER -> {
			TonePalette(
				container = MaterialTheme.colorScheme.primaryContainer,
				content = MaterialTheme.colorScheme.onPrimaryContainer,
			)
		}
	}
