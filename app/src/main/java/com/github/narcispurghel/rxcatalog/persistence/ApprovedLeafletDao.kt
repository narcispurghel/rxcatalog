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
interface ApprovedLeafletDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(leaflet: ApprovedLeafletEntity)

    @Upsert
    suspend fun upsert(leaflet: ApprovedLeafletEntity)

    @Upsert
    suspend fun upsertAll(leaflets: List<ApprovedLeafletEntity>)

    @Query("select * from approved_leaflets where leafletId = :leafletId limit 1")
    suspend fun getById(leafletId: Uuid): ApprovedLeafletEntity?

    @Query("select * from approved_leaflets where leafletId = :leafletId limit 1")
    fun observeById(leafletId: Uuid): Flow<ApprovedLeafletEntity?>

    @Query("select * from approved_leaflets where medicineId = :medicineId limit 1")
    suspend fun getByMedicineId(medicineId: Uuid): ApprovedLeafletEntity?

    @Query("select * from approved_leaflets where medicineId = :medicineId limit 1")
    fun observeByMedicineId(medicineId: Uuid): Flow<ApprovedLeafletEntity?>

    @Query("select * from approved_leaflets order by approvedAt desc, updatedAt desc")
    fun observeAll(): Flow<List<ApprovedLeafletEntity>>

    @Query(
        """
        select * from approved_leaflets
        where lower(title) like '%' || lower(:query) || '%'
           or lower(content) like '%' || lower(:query) || '%'
        order by approvedAt desc, updatedAt desc
        """
    )
    fun search(query: String): Flow<List<ApprovedLeafletEntity>>

    @Query(
        """
        select * from approved_leaflets
        where approvedByUserId = :approvedByUserId
        order by approvedAt desc, updatedAt desc
        """
    )
    fun observeByApprover(approvedByUserId: Uuid): Flow<List<ApprovedLeafletEntity>>

    @Query(
        """
        select * from approved_leaflets
        where syncStatus = :syncStatus
        order by updatedAt desc
        """
    )
    fun observeBySyncStatus(syncStatus: SyncStatus): Flow<List<ApprovedLeafletEntity>>

    @Query(
        """
        update approved_leaflets
        set sourceSubmissionId = :sourceSubmissionId,
            approvedByUserId = :approvedByUserId,
            title = :title,
            content = :content,
            version = :version,
            approvedAt = :approvedAt,
            updatedAt = :updatedAt,
            syncStatus = :syncStatus
        where leafletId = :leafletId
        """
    )
    suspend fun update(
        leafletId: Uuid,
        sourceSubmissionId: Uuid?,
        approvedByUserId: Uuid?,
        title: String,
        content: String,
        version: Int,
        approvedAt: Long,
        updatedAt: Long,
        syncStatus: SyncStatus,
    )

    @Delete
    suspend fun delete(leaflet: ApprovedLeafletEntity)
}
