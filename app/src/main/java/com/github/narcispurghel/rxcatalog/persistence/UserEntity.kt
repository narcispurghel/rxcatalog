@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.persistence

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.uuid.Uuid

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["role"]),
        Index(value = ["syncStatus"]),
    ],
)
data class UserEntity(
    @PrimaryKey
    val userId: Uuid,
    val email: String,
    val displayName: String,
    val passwordHash: String,
    val role: UserRole,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
    val lastLoginAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)
