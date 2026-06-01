@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Upsert
    suspend fun upsert(user: UserEntity)

    @Upsert
    suspend fun upsertAll(users: List<UserEntity>)

    @Query("select * from users where userId = :userId limit 1")
    suspend fun getById(userId: Uuid): UserEntity?

    @Query("select * from users where userId = :userId limit 1")
    fun observeById(userId: Uuid): Flow<UserEntity?>

    @Query("select * from users order by displayName collate nocase asc")
    fun observeAll(): Flow<List<UserEntity>>

    @Query("select * from users where role = :role order by displayName collate nocase asc")
    fun observeByRole(role: UserRole): Flow<List<UserEntity>>

    @Query("select * from users where isActive = 1 order by displayName collate nocase asc")
    fun observeActiveUsers(): Flow<List<UserEntity>>

    @Query(
        """
        select * from users
        where syncStatus = :syncStatus
        order by updatedAt desc
        """
    )
    fun observeBySyncStatus(syncStatus: SyncStatus): Flow<List<UserEntity>>

    @Query(
        """
        select * from users
        where lower(email) like '%' || lower(:query) || '%'
           or lower(displayName) like '%' || lower(:query) || '%'
        order by displayName collate nocase asc
        """
    )
    fun search(query: String): Flow<List<UserEntity>>

    @Query(
        """
        update users
        set isActive = :isActive,
            updatedAt = :updatedAt,
            syncStatus = :syncStatus
        where userId = :userId
        """
    )
    suspend fun updateActiveState(
        userId: Uuid,
        isActive: Boolean,
        updatedAt: Long,
        syncStatus: SyncStatus,
    )

    @Query(
        """
        update users
        set lastLoginAt = :lastLoginAt,
            updatedAt = :updatedAt,
            syncStatus = :syncStatus
        where userId = :userId
        """
    )
    suspend fun updateLastLogin(
        userId: Uuid,
        lastLoginAt: Long,
        updatedAt: Long,
        syncStatus: SyncStatus,
    )

    @Query(
        """
        update users
        set role = :role,
            updatedAt = :updatedAt,
            syncStatus = :syncStatus
        where userId = :userId
        """
    )
    suspend fun updateRole(
        userId: Uuid,
        role: UserRole,
        updatedAt: Long,
        syncStatus: SyncStatus,
    )

    @Delete
    suspend fun delete(user: UserEntity)
}
