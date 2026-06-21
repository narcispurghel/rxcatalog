package com.github.narcispurghel.rxcatalog.catalog

import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface CatalogRepository {
    fun observeMedicines(query: String): Flow<List<MedicineListItem>>

    fun observeMedicineDetails(medicineId: Uuid): Flow<MedicineDetailsItem?>

    fun observeLeafletDetails(leafletId: Uuid): Flow<LeafletDetailsItem?>

    fun observeSubmissionDetails(submissionId: Uuid): Flow<SubmissionDetailsItem?>

    fun observeSubmissionsForUser(userId: Uuid): Flow<List<SubmissionListItem>>

    fun observePendingApprovals(): Flow<List<PendingApprovalListItem>>

    suspend fun ensureSeedData()

    suspend fun refreshMedicines(query: String)

    suspend fun refreshMedicineDetails(medicineId: Uuid)

    suspend fun refreshLeafletDetails(leafletId: Uuid)

    suspend fun saveSubmissionDraft(
        submissionId: Uuid?,
        medicineId: Uuid,
        submittedByUserId: Uuid,
        title: String,
        content: String,
        isUrgent: Boolean,
    ): Uuid

    suspend fun submitForReview(submissionId: Uuid, reviewedAt: Long = System.currentTimeMillis())

    suspend fun reviewSubmission(
        submissionId: Uuid,
        reviewerUserId: Uuid,
        approve: Boolean,
        notes: String?,
        reviewedAt: Long = System.currentTimeMillis(),
    )
}
