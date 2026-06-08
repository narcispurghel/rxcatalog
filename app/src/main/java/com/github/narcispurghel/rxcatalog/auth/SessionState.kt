package com.github.narcispurghel.rxcatalog.auth

sealed interface SessionState {
    data object Loading : SessionState

    data object Unauthenticated : SessionState

    data class Authenticated(val user: AuthenticatedUser) : SessionState
}
