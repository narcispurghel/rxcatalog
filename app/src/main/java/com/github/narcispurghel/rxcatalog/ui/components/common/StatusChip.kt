package com.github.narcispurghel.rxcatalog.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

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

	Surface(
		modifier = modifier,
		shape = MaterialTheme.shapes.small,
		color = palette.container,
		contentColor = palette.content,
	) {
		Row(
			modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
			horizontalArrangement = Arrangement.spacedBy(6.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			icon?.let {
				Icon(
					imageVector = it,
					contentDescription = null,
					modifier = Modifier.size(16.dp),
				)
			}
			Text(
				text = label,
				style = MaterialTheme.typography.labelMedium,
			)
		}
	}
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
