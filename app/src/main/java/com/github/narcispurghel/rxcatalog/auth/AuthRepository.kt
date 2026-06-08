package com.github.narcispurghel.rxcatalog.auth

import com.github.narcispurghel.rxcatalog.common.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeSession(): Flow<SessionState>

    fun observeCurrentUser(): Flow<AuthenticatedUser?>

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        role: UserRole,
    ): AuthResult

    suspend fun login(email: String, password: String): AuthResult

    suspend fun logout()
}
