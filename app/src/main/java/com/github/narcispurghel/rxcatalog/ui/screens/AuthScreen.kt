package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.narcispurghel.rxcatalog.auth.AuthFormState
import com.github.narcispurghel.rxcatalog.auth.AuthMode
import com.github.narcispurghel.rxcatalog.common.UserRole

@Composable
fun AuthScreen(
    state: AuthFormState,
    onDisplayNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onRoleSelected: (UserRole) -> Unit,
    onSubmit: () -> Unit,
    onSwitch: () -> Unit,
    onDismissError: () -> Unit,
) {
    val isRegister = state.mode == AuthMode.REGISTER
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AuthHeader(
                    isRegister = isRegister,
                )

                if (isRegister) {
                    AuthTextField(
                        value = state.displayName,
                        onValueChange = onDisplayNameChanged,
                        label = "Display name",
                        isError = state.displayNameError != null,
                        errorText = state.displayNameError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    )
                }

                AuthTextField(
                    value = state.email,
                    onValueChange = onEmailChanged,
                    label = "Email",
                    isError = state.emailError != null,
                    errorText = state.emailError,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                )

                PasswordField(
                    value = state.password,
                    onValueChange = onPasswordChanged,
                    label = "Password",
                    isError = state.passwordError != null,
                    errorText = state.passwordError,
                    visible = passwordVisible,
                    onVisibilityChange = { passwordVisible = !passwordVisible },
                    imeAction = if (isRegister) ImeAction.Next else ImeAction.Done,
                )

                if (isRegister) {
                    PasswordField(
                        value = state.confirmPassword,
                        onValueChange = onConfirmPasswordChanged,
                        label = "Confirm password",
                        isError = state.confirmPasswordError != null,
                        errorText = state.confirmPasswordError,
                        visible = confirmPasswordVisible,
                        onVisibilityChange = {
                            confirmPasswordVisible = !confirmPasswordVisible
                        },
                        imeAction = ImeAction.Done,
                    )
                    RoleSelector(
                        selectedRole = state.selectedRole,
                        onRoleSelected = onRoleSelected,
                    )
                }

                state.submitError?.let { error ->
                    Card(
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            TextButton(onClick = onDismissError) {
                                Text("Dismiss")
                            }
                        }
                    }
                }

                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSubmitting,
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(if (isRegister) "Create account" else "Sign in")
                }

                OutlinedButton(
                    onClick = onSwitch,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSubmitting,
                ) {
                    Text(if (isRegister) "Back to sign in" else "Create account")
                }
            }
        }
    }
}

@Composable
private fun AuthHeader(isRegister: Boolean) {
    val title = if (isRegister) "Create your RxCatalog account" else "Sign in to RxCatalog"
    val subtitle =
        if (isRegister) {
            "Browse medicines, submit leaflet updates, or review verified information."
        } else {
            "Search medicines, manage submissions, and review verified information."
        }
    val icon =
        if (isRegister) {
            Icons.Filled.PersonAdd
        } else {
            Icons.AutoMirrored.Filled.Login
        }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.medium,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .padding(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    errorText: String?,
    keyboardOptions: KeyboardOptions,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = isError,
        supportingText = errorText?.let { { Text(it) } },
        keyboardOptions = keyboardOptions,
        shape = MaterialTheme.shapes.medium,
        colors = authTextFieldColors(),
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    errorText: String?,
    visible: Boolean,
    onVisibilityChange: () -> Unit,
    imeAction: ImeAction,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = isError,
        supportingText = errorText?.let { { Text(it) } },
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = imeAction,
            ),
        shape = MaterialTheme.shapes.medium,
        visualTransformation =
            if (visible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
        trailingIcon = {
            TextButton(onClick = onVisibilityChange) {
                Text(if (visible) "Hide" else "Show")
            }
        },
        colors = authTextFieldColors(),
    )
}

@Composable
private fun RoleSelector(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Role",
            style = MaterialTheme.typography.titleSmall,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            UserRole.entries.forEach { role ->
                FilterChip(
                    selected = role == selectedRole,
                    onClick = { onRoleSelected(role) },
                    label = { Text(role.toDisplayLabel()) },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                )
            }
        }
    }
}

@Composable
private fun authTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        errorContainerColor = MaterialTheme.colorScheme.errorContainer,
    )

private fun UserRole.toDisplayLabel(): String =
    when (this) {
        UserRole.USER -> "User"
        UserRole.DOCTOR -> "Doctor reviewer"
        UserRole.PHARMACIST -> "Pharmacist reviewer"
    }
