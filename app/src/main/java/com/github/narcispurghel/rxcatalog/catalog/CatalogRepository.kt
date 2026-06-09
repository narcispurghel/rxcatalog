package com.github.narcispurghel.rxcatalog.catalog

import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface CatalogRepository {
    fun observeMedicines(query: String): Flow<List<MedicineListItem>>

    fun observeMedicineDetails(medicineId: Uuid): Flow<MedicineDetailsItem?>

    fun observeLeafletDetails(leafletId: Uuid): Flow<LeafletDetailsItem?>

    fun observeSubmissionsForUser(userId: Uuid): Flow<List<SubmissionListItem>>

    fun observePendingApprovals(): Flow<List<PendingApprovalListItem>>

    suspend fun ensureSeedData()

    suspend fun refreshMedicines(query: String)

    suspend fun refreshMedicineDetails(medicineId: Uuid)

    suspend fun refreshLeafletDetails(leafletId: Uuid)
}
