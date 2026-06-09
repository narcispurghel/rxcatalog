package com.github.narcispurghel.rxcatalog.catalog

import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface CatalogRepository {
    fun observeMedicines(query: String): Flow<List<MedicineListItem>>

    fun observeSubmissionsForUser(userId: Uuid): Flow<List<SubmissionListItem>>

    fun observePendingApprovals(): Flow<List<PendingApprovalListItem>>

    suspend fun ensureSeedData()
}

