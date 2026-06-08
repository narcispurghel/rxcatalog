package com.github.narcispurghel.rxcatalog.auth

sealed interface AuthResult {
    data class Success(val user: AuthenticatedUser) : AuthResult

    data class Failure(val error: AuthError) : AuthResult
}
