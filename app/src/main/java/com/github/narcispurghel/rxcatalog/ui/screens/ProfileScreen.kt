@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.auth.AuthenticatedUser
import com.github.narcispurghel.rxcatalog.auth.SessionState
import com.github.narcispurghel.rxcatalog.ui.components.common.DetailHeader

@Composable
fun ProfileScreen(
    sessionState: SessionState,
    currentUser: AuthenticatedUser?,
    isLoggingOut: Boolean,
    logoutError: String?,
    onDismissLogoutError: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Profile") })
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DetailHeader(
                title = "Session profile",
                subtitle = "Live account and role details from the active session source.",
            )
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "Authenticated: ${sessionState is SessionState.Authenticated}")
                    Text(text = "Display name: ${currentUser?.displayName ?: "Unavailable"}")
                    Text(text = "Email: ${currentUser?.email ?: "Unavailable"}")
                    Text(text = "Role: ${currentUser?.role?.name ?: "none"}")
                }
            }
            if (logoutError != null) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = logoutError,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = onDismissLogoutError) {
                            Text("Dismiss")
                        }
                    }
                }
            }
            Button(
                onClick = onLogout,
                enabled = !isLoggingOut,
            ) {
                if (isLoggingOut) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Logout")
            }
        }
    }
}
