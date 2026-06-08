package com.github.narcispurghel.rxcatalog.auth

sealed interface AuthError {
    data object EmailAlreadyRegistered : AuthError

    data object InvalidCredentials : AuthError

    data object InactiveAccount : AuthError
}
