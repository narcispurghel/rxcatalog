package com.github.narcispurghel.rxcatalog.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.theme.PillShape
import com.github.narcispurghel.rxcatalog.ui.theme.RxDraft
import com.github.narcispurghel.rxcatalog.ui.theme.RxDraftContainerDark
import com.github.narcispurghel.rxcatalog.ui.theme.RxDraftContainerLight
import com.github.narcispurghel.rxcatalog.ui.theme.RxError
import com.github.narcispurghel.rxcatalog.ui.theme.RxErrorContainerDark
import com.github.narcispurghel.rxcatalog.ui.theme.RxErrorContainerLight
import com.github.narcispurghel.rxcatalog.ui.theme.RxSuccess
import com.github.narcispurghel.rxcatalog.ui.theme.RxSuccessContainerDark
import com.github.narcispurghel.rxcatalog.ui.theme.RxSuccessContainerLight
import com.github.narcispurghel.rxcatalog.ui.theme.RxWarning
import com.github.narcispurghel.rxcatalog.ui.theme.RxWarningContainerDark
import com.github.narcispurghel.rxcatalog.ui.theme.RxWarningContainerLight

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
    val colors = chipColors(tone = tone)
    Surface(
        modifier = modifier,
        color = colors.container,
        contentColor = colors.content,
        shape = PillShape,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun chipColors(tone: StatusChipTone): ChipColors {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    return when (tone) {
        StatusChipTone.APPROVED -> {
            if (darkTheme) {
                ChipColors(RxSuccessContainerDark, Color(0xFFDCFCE7), RxSuccess)
            } else {
                ChipColors(RxSuccessContainerLight, Color(0xFF14532D), RxSuccess)
            }
        }

        StatusChipTone.PENDING -> {
            if (darkTheme) {
                ChipColors(RxWarningContainerDark, Color(0xFFFFEDD5), RxWarning)
            } else {
                ChipColors(RxWarningContainerLight, Color(0xFF9A3412), RxWarning)
            }
        }

        StatusChipTone.NEEDS_REVISION,
        StatusChipTone.URGENT,
        -> {
            if (darkTheme) {
                ChipColors(RxErrorContainerDark, Color(0xFFFEE2E2), RxError)
            } else {
                ChipColors(RxErrorContainerLight, Color(0xFF991B1B), RxError)
            }
        }

        StatusChipTone.DRAFT -> {
            if (darkTheme) {
                ChipColors(RxDraftContainerDark, Color(0xFFE2E8F0), RxDraft)
            } else {
                ChipColors(RxDraftContainerLight, Color(0xFF334155), RxDraft)
            }
        }

        StatusChipTone.REVIEWER -> {
            ChipColors(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.onPrimaryContainer,
                MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private data class ChipColors(
    val container: Color,
    val content: Color,
    val border: Color,
)
