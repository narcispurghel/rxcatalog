package com.github.narcispurghel.rxcatalog.auth

import com.github.narcispurghel.rxcatalog.common.UserRole

enum class AuthMode {
    LOGIN,
    REGISTER,
}

data class AuthFormState(
    val mode: AuthMode,
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val selectedRole: UserRole = UserRole.USER,
    val displayNameTouched: Boolean = false,
    val emailTouched: Boolean = false,
    val passwordTouched: Boolean = false,
    val confirmPasswordTouched: Boolean = false,
    val attemptedSubmit: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
) {
    val displayNameError: String?
        get() {
            if (mode != AuthMode.REGISTER || !shouldShow(displayNameTouched)) return null
            return when {
                displayName.isBlank() -> "Display name is required."
                displayName.trim().length < 2 -> "Display name must be at least 2 characters."
                else -> null
            }
        }

    val emailError: String?
        get() {
            if (!shouldShow(emailTouched)) return null
            return when {
                email.isBlank() -> "Email is required."
                !EMAIL_PATTERN.matches(email.trim()) -> "Enter a valid email address."
                else -> null
            }
        }

    val passwordError: String?
        get() {
            if (!shouldShow(passwordTouched)) return null
            return when {
                password.isBlank() -> "Password is required."
                password.length < 8 -> "Password must be at least 8 characters."
                else -> null
            }
        }

    val confirmPasswordError: String?
        get() {
            if (mode != AuthMode.REGISTER || !shouldShow(confirmPasswordTouched)) return null
            return when {
                confirmPassword.isBlank() -> "Confirm your password."
                confirmPassword != password -> "Passwords do not match."
                else -> null
            }
        }

    val isValid: Boolean
        get() =
            emailError == null &&
                passwordError == null &&
                (mode == AuthMode.LOGIN || (displayNameError == null && confirmPasswordError == null)) &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                (mode == AuthMode.LOGIN || (displayName.isNotBlank() && confirmPassword.isNotBlank()))

    private fun shouldShow(touched: Boolean): Boolean = touched || attemptedSubmit

    private companion object {
        private val EMAIL_PATTERN =
            Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
