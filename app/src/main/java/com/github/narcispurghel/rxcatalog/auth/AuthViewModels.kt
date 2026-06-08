package com.github.narcispurghel.rxcatalog.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthNavigationEvent {
    data object NavigateHome : AuthNavigationEvent
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    authRepository: AuthRepository,
) : BaseAuthViewModel(AuthMode.LOGIN, authRepository)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    authRepository: AuthRepository,
) : BaseAuthViewModel(AuthMode.REGISTER, authRepository)

abstract class BaseAuthViewModel(
    private val mode: AuthMode,
    private val authRepository: AuthRepository,
) : ViewModel() {
    var uiState by mutableStateOf(AuthFormState(mode = mode))
        protected set

    private val _events = MutableSharedFlow<AuthNavigationEvent>()
    val events: SharedFlow<AuthNavigationEvent> = _events.asSharedFlow()

    fun onDisplayNameChanged(value: String) {
        uiState = uiState.copy(displayName = value, displayNameTouched = true, submitError = null)
    }

    fun onEmailChanged(value: String) {
        uiState = uiState.copy(email = value, emailTouched = true, submitError = null)
    }

    fun onPasswordChanged(value: String) {
        uiState = uiState.copy(password = value, passwordTouched = true, submitError = null)
    }

    fun onConfirmPasswordChanged(value: String) {
        uiState =
            uiState.copy(
                confirmPassword = value,
                confirmPasswordTouched = true,
                submitError = null,
            )
    }

    fun onRoleSelected(role: com.github.narcispurghel.rxcatalog.common.UserRole) {
        uiState = uiState.copy(selectedRole = role, submitError = null)
    }

    fun clearSubmitError() {
        uiState = uiState.copy(submitError = null)
    }

    fun submit() {
        val submissionState = uiState.copy(attemptedSubmit = true, submitError = null)
        uiState = submissionState

        if (!submissionState.isValid || submissionState.isSubmitting) {
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isSubmitting = true)

            val result =
                if (mode == AuthMode.LOGIN) {
                    authRepository.login(
                        email = submissionState.email.trim(),
                        password = submissionState.password,
                    )
                } else {
                    authRepository.register(
                        email = submissionState.email.trim(),
                        password = submissionState.password,
                        displayName = submissionState.displayName.trim(),
                        role = submissionState.selectedRole,
                    )
                }

            uiState =
                when (result) {
                    is AuthResult.Success -> {
                        _events.emit(AuthNavigationEvent.NavigateHome)
                        uiState.copy(isSubmitting = false)
                    }

                    is AuthResult.Failure ->
                        uiState.copy(
                            isSubmitting = false,
                            submitError = result.error.toMessage(),
                        )
                }
        }
    }
}

private fun AuthError.toMessage(): String =
    when (this) {
        AuthError.EmailAlreadyRegistered -> "An account with this email already exists."
        AuthError.InvalidCredentials -> "Invalid email or password."
        AuthError.InactiveAccount -> "This account is inactive."
    }
