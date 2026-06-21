package com.github.narcispurghel.rxcatalog.ui.components.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.ui.theme.RxCatalogTheme

@Composable
fun ActionCard(
	title: String,
	subtitle: String,
	label: String,
	icon: ImageVector,
	onClick: () -> Unit,
) {
	ElevatedCard(
		modifier = Modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.extraLarge,
	) {
		Column(
			modifier = Modifier.padding(20.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
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
					Text(
						text = title,
						style = MaterialTheme.typography.titleMedium,
					)
				},
				supportingContent = {
					Text(
						text = subtitle,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				},
			)
			Button(onClick = onClick) {
				Text(label)
			}
		}
	}
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
				ActionCard(
					title = "Preview",
					subtitle = "Preview",
					label = "Preview",
					icon = Icons.AutoMirrored.Outlined.Comment,
				) {
				}
			}
		}
	}
}
