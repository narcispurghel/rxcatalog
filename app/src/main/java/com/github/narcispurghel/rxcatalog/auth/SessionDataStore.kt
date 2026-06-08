@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.auth

import com.github.narcispurghel.rxcatalog.common.UserRole
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface SessionDataStore {
    fun observeSessionPreferences(): Flow<SessionPreferences>

    suspend fun setSession(userId: Uuid, role: UserRole)

    suspend fun clearSession()
}
