package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    title: String,
    subtitle: String,
    primaryAction: String,
    secondaryAction: String,
    tertiaryAction: String,
    icon: ImageVector = Icons.Filled.AccountCircle,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
    onTertiary: () -> Unit,
    onSwitch: () -> Unit,
    switchLabel: String,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(imageVector = icon, contentDescription = null)
                Text(text = title, style = MaterialTheme.typography.headlineMedium)
                Text(text = subtitle)
                Button(
                    onClick = onPrimary,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(primaryAction) }
                OutlinedButton(onClick = onSecondary, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        secondaryAction,
                    )
                }
                FilledTonalButton(onClick = onTertiary, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        tertiaryAction,
                    )
                }
                OutlinedButton(onClick = onSwitch, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        switchLabel,
                    )
                }
            }
        }
    }
}
