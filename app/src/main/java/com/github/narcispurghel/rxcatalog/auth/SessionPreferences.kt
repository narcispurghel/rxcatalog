@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.auth

import com.github.narcispurghel.rxcatalog.common.UserRole
import kotlin.uuid.Uuid

data class SessionPreferences(
    val currentUserId: Uuid? = null,
    val currentUserRole: UserRole? = null,
    val isAuthenticated: Boolean = false,
)
