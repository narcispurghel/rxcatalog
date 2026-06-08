package com.github.narcispurghel.rxcatalog.auth

import com.github.narcispurghel.rxcatalog.common.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthFormStateTest {
    @Test
    fun `login form requires email and password`() {
        val state =
            AuthFormState(mode = AuthMode.LOGIN).copy(
                attemptedSubmit = true,
            )

        assertEquals("Email is required.", state.emailError)
        assertEquals("Password is required.", state.passwordError)
        assertFalse(state.isValid)
    }

    @Test
    fun `register form rejects invalid email short password and mismatched confirmation`() {
        val state =
            AuthFormState(
                mode = AuthMode.REGISTER,
                displayName = "Dr",
                email = "bad-email",
                password = "short",
                confirmPassword = "different",
                selectedRole = UserRole.DOCTOR,
                attemptedSubmit = true,
            )

        assertEquals("Enter a valid email address.", state.emailError)
        assertEquals("Password must be at least 8 characters.", state.passwordError)
        assertEquals("Passwords do not match.", state.confirmPasswordError)
        assertFalse(state.isValid)
    }

    @Test
    fun `valid register form has no validation errors`() {
        val state =
            AuthFormState(
                mode = AuthMode.REGISTER,
                displayName = "Pharmacist Jane",
                email = "jane@example.com",
                password = "password123",
                confirmPassword = "password123",
                selectedRole = UserRole.PHARMACIST,
                attemptedSubmit = true,
            )

        assertNull(state.displayNameError)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.confirmPasswordError)
        assertTrue(state.isValid)
    }
}
