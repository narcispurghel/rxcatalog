package com.github.narcispurghel.rxcatalog.ui.components.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun RxCatalogTopAppBar(
	title: String,
	modifier: Modifier = Modifier,
) {
	val topPadding =
		WindowInsets.statusBars
			.asPaddingValues()
			.calculateTopPadding()
			.coerceAtMost(24.dp)

	Surface(
		modifier = modifier.fillMaxWidth(),
		color = MaterialTheme.colorScheme.surface,
	) {
		Column(
			modifier = Modifier.fillMaxWidth(),
		) {
			Row(
				modifier =
					Modifier
						.fillMaxWidth()
						.padding(top = topPadding)
						.height(44.dp)
						.padding(horizontal = 20.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = title,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.SemiBold,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}
			HorizontalDivider(
				thickness = 1.dp,
				color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
			)
		}
	}
}
