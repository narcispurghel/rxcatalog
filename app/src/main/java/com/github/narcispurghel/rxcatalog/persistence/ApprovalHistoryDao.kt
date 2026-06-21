@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.persistence

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
interface ApprovalHistoryDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(entry: ApprovalHistoryEntity)

	@Upsert
	suspend fun upsert(entry: ApprovalHistoryEntity)

	@Upsert
	suspend fun upsertAll(entries: List<ApprovalHistoryEntity>)

	@Query("select * from approval_history where approvalHistoryId = :approvalHistoryId limit 1")
	suspend fun getById(approvalHistoryId: Uuid): ApprovalHistoryEntity?

	@Query("select * from approval_history where approvalHistoryId = :approvalHistoryId limit 1")
	fun observeById(approvalHistoryId: Uuid): Flow<ApprovalHistoryEntity?>

	@Query(
		"""
        select * from approval_history
        where submissionId = :submissionId
        order by actedAt asc, createdAt asc
        """,
	)
	fun observeBySubmissionId(submissionId: Uuid): Flow<List<ApprovalHistoryEntity>>

	@Query(
		"""
        select * from approval_history
        where reviewerUserId = :reviewerUserId
        order by actedAt desc, createdAt desc
        """,
	)
	fun observeByReviewerUserId(reviewerUserId: Uuid): Flow<List<ApprovalHistoryEntity>>

	@Query(
		"""
        select * from approval_history
        where action = :action
        order by actedAt desc, createdAt desc
        """,
	)
	fun observeByAction(action: ApprovalAction): Flow<List<ApprovalHistoryEntity>>

	@Query(
		"""
        select * from approval_history
        where syncStatus = :syncStatus
        order by createdAt desc
        """,
	)
	fun observeBySyncStatus(syncStatus: SyncStatus): Flow<List<ApprovalHistoryEntity>>

	@Query(
		"""
        select * from approval_history
        where submissionId = :submissionId
        order by actedAt desc, createdAt desc
        limit 1
        """,
	)
	suspend fun getLatestForSubmission(submissionId: Uuid): ApprovalHistoryEntity?

	@Delete
	suspend fun delete(entry: ApprovalHistoryEntity)
}
