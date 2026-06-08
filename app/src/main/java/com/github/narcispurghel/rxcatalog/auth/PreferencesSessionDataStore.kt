@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.narcispurghel.rxcatalog.common.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.Uuid

@Singleton
class PreferencesSessionDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SessionDataStore {
    override fun observeSessionPreferences(): Flow<SessionPreferences> =
        dataStore.data.map { preferences ->
            val isAuthenticated = preferences[IS_AUTHENTICATED_KEY] ?: false
            if (!isAuthenticated) {
                return@map SessionPreferences()
            }

            val currentUserId = preferences[CURRENT_USER_ID_KEY]?.let { rawUserId ->
                runCatching { Uuid.parse(rawUserId) }.getOrNull()
            }
            val currentUserRole = preferences[CURRENT_USER_ROLE_KEY]?.let { rawRole ->
                runCatching { UserRole.valueOf(rawRole) }.getOrNull()
            }

            if (currentUserId == null || currentUserRole == null) {
                SessionPreferences()
            } else {
                SessionPreferences(
                    currentUserId = currentUserId,
                    currentUserRole = currentUserRole,
                    isAuthenticated = true,
                )
            }
        }

    override suspend fun setSession(userId: Uuid, role: UserRole) {
        dataStore.edit { preferences ->
            preferences[CURRENT_USER_ID_KEY] = userId.toString()
            preferences[CURRENT_USER_ROLE_KEY] = role.name
            preferences[IS_AUTHENTICATED_KEY] = true
        }
    }

    override suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(CURRENT_USER_ID_KEY)
            preferences.remove(CURRENT_USER_ROLE_KEY)
            preferences[IS_AUTHENTICATED_KEY] = false
        }
    }

    private companion object {
        val CURRENT_USER_ID_KEY = stringPreferencesKey("current_user_id")
        val CURRENT_USER_ROLE_KEY = stringPreferencesKey("current_user_role")
        val IS_AUTHENTICATED_KEY = booleanPreferencesKey("is_authenticated")
    }
}
