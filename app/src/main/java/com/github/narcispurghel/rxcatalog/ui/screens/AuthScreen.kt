package com.github.narcispurghel.rxcatalog.ui.screens

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    imageVector = if (isRegister) Icons.Filled.PersonAdd else Icons.AutoMirrored.Filled.Login,
                    contentDescription = null,
                )
                Text(
                    text = if (isRegister) "Create account" else "Welcome back",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text =
                        if (isRegister) {
                            "Register to submit or review medicine leaflets."
                        } else {
                            "Sign in to browse the catalog and manage submissions."
                        },
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
                    ElevatedCard(
                        colors =
                            CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                        modifier = Modifier.fillMaxWidth(),
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
                    Text(if (isRegister) "Back to login" else "Switch to register")
                }
            }
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
                    label = { Text(role.name.lowercase().replaceFirstChar(Char::titlecase)) },
                )
            }
        }
    }
}
