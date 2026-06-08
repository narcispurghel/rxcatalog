@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.auth

import com.github.narcispurghel.rxcatalog.common.UserRole
import kotlin.uuid.Uuid

data class AuthenticatedUser(
    val userId: Uuid,
    val email: String,
    val displayName: String,
    val role: UserRole,
)
