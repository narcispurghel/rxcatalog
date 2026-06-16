package com.github.narcispurghel.rxcatalog.ui.components.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
        leadingIcon =
            icon?.let { customIcon ->
                {
                    Icon(
                        imageVector = customIcon,
                        tint = palette.content,
                        contentDescription = null,
                    )
                }
            } ?: toneIcon(tone)?.let { toneIcon ->
                {
                    Icon(
                        imageVector = toneIcon,
                        tint = palette.content,
                        contentDescription = null,
                    )
                }
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
        StatusChipTone.APPROVED ->
            TonePalette(
                container = MaterialTheme.colorScheme.tertiaryContainer,
                content = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        StatusChipTone.PENDING ->
            TonePalette(
                container = MaterialTheme.colorScheme.secondaryContainer,
                content = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        StatusChipTone.NEEDS_REVISION ->
            TonePalette(
                container = MaterialTheme.colorScheme.errorContainer,
                content = MaterialTheme.colorScheme.onErrorContainer,
            )
        StatusChipTone.DRAFT ->
            TonePalette(
                container = MaterialTheme.colorScheme.surfaceVariant,
                content = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        StatusChipTone.URGENT ->
            TonePalette(
                container = MaterialTheme.colorScheme.errorContainer,
                content = MaterialTheme.colorScheme.onErrorContainer,
            )
        StatusChipTone.REVIEWER ->
            TonePalette(
                container = MaterialTheme.colorScheme.primaryContainer,
                content = MaterialTheme.colorScheme.onPrimaryContainer,
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
