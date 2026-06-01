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
interface SubmittedLeafletDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(submission: SubmittedLeafletEntity)

    @Upsert
    suspend fun upsert(submission: SubmittedLeafletEntity)

    @Upsert
    suspend fun upsertAll(submissions: List<SubmittedLeafletEntity>)

    @Query("select * from submitted_leaflets where submissionId = :submissionId limit 1")
    suspend fun getById(submissionId: Uuid): SubmittedLeafletEntity?

    @Query("select * from submitted_leaflets where submissionId = :submissionId limit 1")
    fun observeById(submissionId: Uuid): Flow<SubmittedLeafletEntity?>

    @Query("select * from submitted_leaflets order by updatedAt desc, createdAt desc")
    fun observeAll(): Flow<List<SubmittedLeafletEntity>>

    @Query(
        """
        select * from submitted_leaflets
        where medicineId = :medicineId
        order by updatedAt desc, createdAt desc
        """
    )
    fun observeByMedicineId(medicineId: Uuid): Flow<List<SubmittedLeafletEntity>>

    @Query(
        """
        select * from submitted_leaflets
        where submittedByUserId = :submittedByUserId
        order by updatedAt desc, createdAt desc
        """
    )
    fun observeBySubmittedByUserId(submittedByUserId: Uuid): Flow<List<SubmittedLeafletEntity>>

    @Query(
        """
        select * from submitted_leaflets
        where reviewedByUserId = :reviewedByUserId
        order by reviewedAt desc, updatedAt desc
        """
    )
    fun observeByReviewedByUserId(reviewedByUserId: Uuid): Flow<List<SubmittedLeafletEntity>>

    @Query(
        """
        select * from submitted_leaflets
        where status = :status
        order by updatedAt desc, createdAt desc
        """
    )
    fun observeByStatus(status: SubmissionStatus): Flow<List<SubmittedLeafletEntity>>

    @Query(
        """
        select * from submitted_leaflets
        where submittedByUserId = :submittedByUserId
          and status = :status
        order by updatedAt desc, createdAt desc
        """
    )
    fun observeBySubmittedByUserIdAndStatus(
        submittedByUserId: Uuid,
        status: SubmissionStatus,
    ): Flow<List<SubmittedLeafletEntity>>

    @Query(
        """
        select * from submitted_leaflets
        where lower(title) like '%' || lower(:query) || '%'
           or lower(content) like '%' || lower(:query) || '%'
           or lower(coalesce(rejectionReason, '')) like '%' || lower(:query) || '%'
        order by updatedAt desc, createdAt desc
        """
    )
    fun search(query: String): Flow<List<SubmittedLeafletEntity>>

    @Query(
        """
        select * from submitted_leaflets
        where syncStatus = :syncStatus
        order by updatedAt desc, createdAt desc
        """
    )
    fun observeBySyncStatus(syncStatus: SyncStatus): Flow<List<SubmittedLeafletEntity>>

    @Query(
        """
        update submitted_leaflets
        set reviewedByUserId = :reviewedByUserId,
            status = :status,
            rejectionReason = :rejectionReason,
            reviewedAt = :reviewedAt,
            updatedAt = :updatedAt,
            syncStatus = :syncStatus
        where submissionId = :submissionId
        """
    )
    suspend fun updateReviewState(
        submissionId: Uuid,
        reviewedByUserId: Uuid?,
        status: SubmissionStatus,
        rejectionReason: String?,
        reviewedAt: Long?,
        updatedAt: Long,
        syncStatus: SyncStatus,
    )

    @Query(
        """
        update submitted_leaflets
        set title = :title,
            content = :content,
            updatedAt = :updatedAt,
            syncStatus = :syncStatus
        where submissionId = :submissionId
        """
    )
    suspend fun updateDraft(
        submissionId: Uuid,
        title: String,
        content: String,
        updatedAt: Long,
        syncStatus: SyncStatus,
    )

    @Query(
        """
        update submitted_leaflets
        set status = :status,
            updatedAt = :updatedAt,
            syncStatus = :syncStatus
        where submissionId = :submissionId
        """
    )
    suspend fun updateStatus(
        submissionId: Uuid,
        status: SubmissionStatus,
        updatedAt: Long,
        syncStatus: SyncStatus,
    )

    @Delete
    suspend fun delete(submission: SubmittedLeafletEntity)
}
