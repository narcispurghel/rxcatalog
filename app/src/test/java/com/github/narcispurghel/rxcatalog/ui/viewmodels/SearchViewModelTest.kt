package com.github.narcispurghel.rxcatalog.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.github.narcispurghel.rxcatalog.catalog.CatalogRepository
import com.github.narcispurghel.rxcatalog.catalog.LeafletDetailsItem
import com.github.narcispurghel.rxcatalog.catalog.MedicineDetailsItem
import com.github.narcispurghel.rxcatalog.catalog.MedicineListItem
import com.github.narcispurghel.rxcatalog.catalog.PendingApprovalListItem
import com.github.narcispurghel.rxcatalog.catalog.SubmissionDetailsItem
import com.github.narcispurghel.rxcatalog.catalog.SubmissionListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule
    val mainDispatcherRule = com.github.narcispurghel.rxcatalog.MainDispatcherRule()

    @Test
    fun `search refresh keeps running after a thrown refresh`() = runTest {
        val repository = FakeCatalogRepository()
        val viewModel = SearchViewModel(SavedStateHandle(mapOf("query" to "")), repository)

        advanceTimeBy(300)
        viewModel.onQueryChanged("one")
        advanceTimeBy(300)
        viewModel.onQueryChanged("two")
        advanceTimeBy(300)

        assertEquals(listOf("", "one", "two"), repository.refreshQueries)
    }
}

private class FakeCatalogRepository : CatalogRepository {
    val refreshQueries = mutableListOf<String>()

    override fun observeMedicines(query: String): Flow<List<MedicineListItem>> = flowOf(emptyList())

    override fun observeMedicineDetails(medicineId: Uuid): Flow<MedicineDetailsItem?> = flowOf(null)

    override fun observeLeafletDetails(leafletId: Uuid): Flow<LeafletDetailsItem?> = flowOf(null)

    override fun observeSubmissionDetails(submissionId: Uuid): Flow<SubmissionDetailsItem?> = flowOf(null)

    override fun observeSubmissionsForUser(userId: Uuid): Flow<List<SubmissionListItem>> = flowOf(emptyList())

    override fun observePendingApprovals(): Flow<List<PendingApprovalListItem>> = flowOf(emptyList())

    override suspend fun ensureSeedData() = Unit

    override suspend fun refreshMedicines(query: String) {
        refreshQueries += query
        if (query == "one") {
            throw IllegalStateException("boom")
        }
    }

    override suspend fun refreshMedicineDetails(medicineId: Uuid) = Unit

    override suspend fun refreshLeafletDetails(leafletId: Uuid) = Unit

    override suspend fun saveSubmissionDraft(
        submissionId: Uuid?,
        medicineId: Uuid,
        submittedByUserId: Uuid,
        title: String,
        content: String,
    ): Uuid = Uuid.random()

    override suspend fun submitForReview(submissionId: Uuid, reviewedAt: Long) = Unit

    override suspend fun reviewSubmission(
        submissionId: Uuid,
        reviewerUserId: Uuid,
        approve: Boolean,
        notes: String?,
        reviewedAt: Long,
    ) = Unit
}
