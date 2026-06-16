@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.catalog

import app.cash.turbine.test
import com.github.narcispurghel.rxcatalog.auth.PasswordHasher
import com.github.narcispurghel.rxcatalog.network.CatalogApiService
import com.github.narcispurghel.rxcatalog.persistence.ApprovedLeafletDao
import com.github.narcispurghel.rxcatalog.persistence.ApprovedLeafletEntity
import com.github.narcispurghel.rxcatalog.persistence.ApprovalHistoryDao
import com.github.narcispurghel.rxcatalog.persistence.ApprovalHistoryEntity
import com.github.narcispurghel.rxcatalog.persistence.ApprovalAction
import com.github.narcispurghel.rxcatalog.persistence.FavoriteDao
import com.github.narcispurghel.rxcatalog.persistence.MedicineDao
import com.github.narcispurghel.rxcatalog.persistence.MedicineEntity
import com.github.narcispurghel.rxcatalog.persistence.SubmissionStatus
import com.github.narcispurghel.rxcatalog.persistence.SubmittedLeafletDao
import com.github.narcispurghel.rxcatalog.persistence.SubmittedLeafletEntity
import com.github.narcispurghel.rxcatalog.persistence.SyncStatus
import com.github.narcispurghel.rxcatalog.persistence.RxCatalogDatabase
import com.github.narcispurghel.rxcatalog.persistence.UserDao
import com.github.narcispurghel.rxcatalog.persistence.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlin.uuid.Uuid

class RoomCatalogRepositoryNetworkTest {
    private val passwordHasher = PasswordHasher()
    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
        }

    private lateinit var server: MockWebServer
    private lateinit var apiService: CatalogApiService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        apiService =
            Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(CatalogApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `refreshMedicines parses remote medicines and stores them in the local catalogue`() =
        runTest {
            val medicineId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """
                        {
                          "items": [
                            {
                              "id": "$medicineId",
                              "canonical_name": "Aspirin 100 mg",
                              "brand_name": "Aspirin Protect",
                              "active_ingredient": "Acetylsalicylic acid",
                              "atc_code": "B01AC06",
                              "description": "Antiplatelet medicine",
                              "last_updated_at": 1700000000000
                            }
                          ]
                        }
                        """.trimIndent(),
                    ),
            )

            val medicineDao = FakeMedicineDao()
            val repository =
                RoomCatalogRepository(
                    database = NoOpRxCatalogDatabase(),
                    medicineDao = medicineDao,
                    approvedLeafletDao = FakeApprovedLeafletDao(),
                    submittedLeafletDao = NoOpSubmittedLeafletDao(),
                    approvalHistoryDao = NoOpApprovalHistoryDao(),
                    userDao = NoOpUserDao(),
                    passwordHasher = passwordHasher,
                    catalogApiService = apiService,
                )

            repository.refreshMedicines("aspirin")

            val request = server.takeRequest()
            assertEquals("/v1/medicines?query=aspirin", request.path)
            val stored = medicineDao.getById(Uuid.parse(medicineId))
            assertNotNull(stored)
            assertEquals("Aspirin 100 mg", stored?.canonicalName)
            assertEquals("Aspirin Protect", stored?.brandName)
            assertEquals("Acetylsalicylic acid", stored?.activeIngredient)
            assertEquals("B01AC06", stored?.atcCode)
            assertEquals("Antiplatelet medicine", stored?.description)
            assertEquals(SyncStatus.SYNCED, stored?.syncStatus)
        }

    @Test
    fun `refreshMedicineDetails parses remote details and stores approved leaflets`() =
        runTest {
            val medicineId = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
            val leafletId = "dddddddd-dddd-dddd-dddd-dddddddddddd"
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """
                        {
                          "medicine": {
                            "id": "$medicineId",
                            "canonical_name": "Ibuprofen 200 mg",
                            "brand_name": "Nurofen",
                            "active_ingredient": "Ibuprofen",
                            "atc_code": "M01AE01",
                            "description": "Pain relief medicine",
                            "last_updated_at": 1700000100000
                          },
                          "approved_leaflet": {
                            "id": "$leafletId",
                            "medicine_id": "$medicineId",
                            "title": "Ibuprofen leaflet",
                            "content": "Approved leaflet content",
                            "version": 2,
                            "published_at": 1700000200000
                          },
                          "recent_submissions": []
                        }
                        """.trimIndent(),
                    ),
            )

            val medicineDao = FakeMedicineDao()
            val approvedLeafletDao = FakeApprovedLeafletDao()
            val repository =
                RoomCatalogRepository(
                    database = NoOpRxCatalogDatabase(),
                    medicineDao = medicineDao,
                    approvedLeafletDao = approvedLeafletDao,
                    submittedLeafletDao = NoOpSubmittedLeafletDao(),
                    approvalHistoryDao = NoOpApprovalHistoryDao(),
                    userDao = NoOpUserDao(),
                    passwordHasher = passwordHasher,
                    catalogApiService = apiService,
                )

            repository.refreshMedicineDetails(Uuid.parse(medicineId))

            val request = server.takeRequest()
            assertEquals("/v1/medicines/$medicineId", request.path)
            val storedMedicine = medicineDao.getById(Uuid.parse(medicineId))
            assertNotNull(storedMedicine)
            assertEquals("Ibuprofen 200 mg", storedMedicine?.canonicalName)
            assertEquals("Nurofen", storedMedicine?.brandName)
            assertEquals("Pain relief medicine", storedMedicine?.description)
            val storedLeaflet = approvedLeafletDao.getById(Uuid.parse(leafletId))
            assertNotNull(storedLeaflet)
            assertEquals("Ibuprofen leaflet", storedLeaflet?.title)
            assertEquals("Approved leaflet content", storedLeaflet?.content)
            assertEquals(2, storedLeaflet?.version)
            assertEquals(Uuid.parse(medicineId), storedLeaflet?.medicineId)
        }

    @Test
    fun `refreshMedicineDetails deletes stale approved leaflets when backend omits them`() =
        runTest {
            val medicineId = Uuid.parse("cccccccc-cccc-cccc-cccc-cccccccccccc")
            val leafletId = Uuid.parse("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """
                        {
                          "medicine": {
                            "id": "${medicineId}",
                            "canonical_name": "Ibuprofen 200 mg",
                            "brand_name": "Nurofen",
                            "active_ingredient": "Ibuprofen",
                            "atc_code": "M01AE01",
                            "description": "Pain relief medicine",
                            "last_updated_at": 1700000300000
                          },
                          "approved_leaflet": null,
                          "recent_submissions": []
                        }
                        """.trimIndent(),
                    ),
            )

            val medicineDao = FakeMedicineDao()
            medicineDao.upsert(
                MedicineEntity(
                    medicineId = medicineId,
                    canonicalName = "Cached medicine",
                    brandName = "Cached brand",
                    activeIngredient = "Cached ingredient",
                    atcCode = "CACHED",
                    description = "Cached description",
                    createdAt = 1700000000000,
                    updatedAt = 1700000000000,
                    syncStatus = SyncStatus.SYNCED,
                ),
            )
            val approvedLeafletDao = FakeApprovedLeafletDao()
            approvedLeafletDao.upsert(
                ApprovedLeafletEntity(
                    leafletId = leafletId,
                    medicineId = medicineId,
                    title = "Cached leaflet",
                    content = "Cached leaflet content",
                    version = 1,
                    approvedAt = 1700000000000,
                    createdAt = 1700000000000,
                    updatedAt = 1700000000000,
                    syncStatus = SyncStatus.SYNCED,
                ),
            )
            val repository =
                RoomCatalogRepository(
                    database = NoOpRxCatalogDatabase(),
                    medicineDao = medicineDao,
                    approvedLeafletDao = approvedLeafletDao,
                    submittedLeafletDao = NoOpSubmittedLeafletDao(),
                    approvalHistoryDao = NoOpApprovalHistoryDao(),
                    userDao = NoOpUserDao(),
                    passwordHasher = passwordHasher,
                    catalogApiService = apiService,
                )

            repository.refreshMedicineDetails(medicineId)

            val request = server.takeRequest()
            assertEquals("/v1/medicines/$medicineId", request.path)
            assertEquals(null, approvedLeafletDao.getByMedicineId(medicineId))
        }

    @Test
    fun `refreshLeafletDetails hydrates uncached leaflet routes`() =
        runTest {
            val medicineId = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
            val leafletId = "dddddddd-dddd-dddd-dddd-dddddddddddd"
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """
                        {
                          "medicine": {
                            "id": "$medicineId",
                            "canonical_name": "Ibuprofen 200 mg",
                            "brand_name": "Nurofen",
                            "active_ingredient": "Ibuprofen",
                            "atc_code": "M01AE01",
                            "description": "Pain relief medicine",
                            "last_updated_at": 1700000400000
                          },
                          "approved_leaflet": {
                            "id": "$leafletId",
                            "medicine_id": "$medicineId",
                            "title": "Ibuprofen leaflet",
                            "content": "Approved leaflet content",
                            "version": 3,
                            "published_at": 1700000500000
                          },
                          "recent_submissions": []
                        }
                        """.trimIndent(),
                    ),
            )

            val repository =
                RoomCatalogRepository(
                    database = NoOpRxCatalogDatabase(),
                    medicineDao = FakeMedicineDao(),
                    approvedLeafletDao = FakeApprovedLeafletDao(),
                    submittedLeafletDao = NoOpSubmittedLeafletDao(),
                    approvalHistoryDao = NoOpApprovalHistoryDao(),
                    userDao = NoOpUserDao(),
                    passwordHasher = passwordHasher,
                    catalogApiService = apiService,
                )

            repository.refreshLeafletDetails(Uuid.parse(leafletId))

            val request = server.takeRequest()
            assertEquals("/v1/leaflets/$leafletId", request.path)
        }
}

private class FakeMedicineDao : MedicineDao {
    private val medicines = MutableStateFlow<Map<Uuid, MedicineEntity>>(emptyMap())

    override suspend fun insert(medicine: MedicineEntity) {
        medicines.value = medicines.value + (medicine.medicineId to medicine)
    }

    override suspend fun upsert(medicine: MedicineEntity) = insert(medicine)

    override suspend fun upsertAll(medicines: List<MedicineEntity>) {
        this.medicines.value = this.medicines.value + medicines.associateBy { it.medicineId }
    }

    override suspend fun getById(medicineId: Uuid): MedicineEntity? = medicines.value[medicineId]

    override fun observeById(medicineId: Uuid): Flow<MedicineEntity?> =
        medicines.map { it[medicineId] }

    override fun observeAll(): Flow<List<MedicineEntity>> = medicines.map { it.values.toList() }

    override fun search(query: String): Flow<List<MedicineEntity>> =
        medicines.map { entries ->
            entries.values.filter {
                it.canonicalName.contains(query, ignoreCase = true) ||
                    (it.brandName?.contains(query, ignoreCase = true) == true) ||
                    (it.activeIngredient?.contains(query, ignoreCase = true) == true) ||
                    (it.atcCode?.contains(query, ignoreCase = true) == true)
            }
        }

    override fun observeBySyncStatus(syncStatus: SyncStatus): Flow<List<MedicineEntity>> =
        medicines.map { entries -> entries.values.filter { it.syncStatus == syncStatus } }

    override suspend fun updateDetails(
        medicineId: Uuid,
        canonicalName: String,
        brandName: String?,
        activeIngredient: String?,
        atcCode: String?,
        description: String?,
        updatedAt: Long,
        syncStatus: SyncStatus,
    ) {
        val current = medicines.value[medicineId] ?: return
        medicines.value =
            medicines.value + (
                medicineId to
                    current.copy(
                        canonicalName = canonicalName,
                        brandName = brandName,
                        activeIngredient = activeIngredient,
                        atcCode = atcCode,
                        description = description,
                        updatedAt = updatedAt,
                        syncStatus = syncStatus,
                    )
                )
    }

    override suspend fun delete(medicine: MedicineEntity) {
        medicines.value = medicines.value - medicine.medicineId
    }
}

private class FakeApprovedLeafletDao : ApprovedLeafletDao {
    private val leaflets = MutableStateFlow<Map<Uuid, ApprovedLeafletEntity>>(emptyMap())

    override suspend fun insert(leaflet: ApprovedLeafletEntity) {
        leaflets.value = leaflets.value + (leaflet.leafletId to leaflet)
    }

    override suspend fun upsert(leaflet: ApprovedLeafletEntity) = insert(leaflet)

    override suspend fun upsertAll(leaflets: List<ApprovedLeafletEntity>) {
        this.leaflets.value = this.leaflets.value + leaflets.associateBy { it.leafletId }
    }

    override suspend fun getById(leafletId: Uuid): ApprovedLeafletEntity? = leaflets.value[leafletId]

    override fun observeById(leafletId: Uuid): Flow<ApprovedLeafletEntity?> =
        leaflets.map { it[leafletId] }

    override suspend fun getByMedicineId(medicineId: Uuid): ApprovedLeafletEntity? =
        leaflets.value.values.firstOrNull { it.medicineId == medicineId }

    override fun observeByMedicineId(medicineId: Uuid): Flow<ApprovedLeafletEntity?> =
        leaflets.map { entries -> entries.values.firstOrNull { it.medicineId == medicineId } }

    override fun observeAll(): Flow<List<ApprovedLeafletEntity>> = leaflets.map { it.values.toList() }

    override fun search(query: String): Flow<List<ApprovedLeafletEntity>> =
        leaflets.map { entries ->
            entries.values.filter {
                it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
            }
        }

    override fun observeByApprover(approvedByUserId: Uuid): Flow<List<ApprovedLeafletEntity>> =
        leaflets.map { entries -> entries.values.filter { it.approvedByUserId == approvedByUserId } }

    override fun observeBySyncStatus(syncStatus: SyncStatus): Flow<List<ApprovedLeafletEntity>> =
        leaflets.map { entries -> entries.values.filter { it.syncStatus == syncStatus } }

    override suspend fun update(
        leafletId: Uuid,
        sourceSubmissionId: Uuid?,
        approvedByUserId: Uuid?,
        title: String,
        content: String,
        version: Int,
        approvedAt: Long,
        updatedAt: Long,
        syncStatus: SyncStatus,
    ) {
        val current = leaflets.value[leafletId] ?: return
        leaflets.value =
            leaflets.value + (
                leafletId to
                    current.copy(
                        sourceSubmissionId = sourceSubmissionId,
                        approvedByUserId = approvedByUserId,
                        title = title,
                        content = content,
                        version = version,
                        approvedAt = approvedAt,
                        updatedAt = updatedAt,
                        syncStatus = syncStatus,
                    )
                )
    }

    override suspend fun delete(leaflet: ApprovedLeafletEntity) {
        leaflets.value = leaflets.value - leaflet.leafletId
    }
}

private class NoOpUserDao : UserDao {
    override suspend fun insert(user: UserEntity) = error("Not used")
    override suspend fun upsert(user: UserEntity) = error("Not used")
    override suspend fun upsertAll(users: List<UserEntity>) = error("Not used")
    override suspend fun getById(userId: Uuid): UserEntity? = error("Not used")
    override suspend fun getActiveById(userId: Uuid): UserEntity? = error("Not used")
    override fun observeById(userId: Uuid): Flow<UserEntity?> = error("Not used")
    override fun observeActiveById(userId: Uuid): Flow<UserEntity?> = error("Not used")
    override suspend fun getByNormalizedEmail(normalizedEmail: String): UserEntity? = error("Not used")
    override fun observeByNormalizedEmail(normalizedEmail: String): Flow<UserEntity?> = error("Not used")
    override fun observeAll(): Flow<List<UserEntity>> = error("Not used")
    override fun observeByRole(role: com.github.narcispurghel.rxcatalog.persistence.UserRole): Flow<List<UserEntity>> =
        error("Not used")
    override fun observeActiveUsers(): Flow<List<UserEntity>> = error("Not used")
    override fun observeBySyncStatus(syncStatus: SyncStatus): Flow<List<UserEntity>> = error("Not used")
    override fun search(query: String): Flow<List<UserEntity>> = error("Not used")
    override suspend fun updateActiveState(
        userId: Uuid,
        isActive: Boolean,
        updatedAt: Long,
        syncStatus: SyncStatus,
    ) = error("Not used")

    override suspend fun updateLastLogin(
        userId: Uuid,
        lastLoginAt: Long,
        updatedAt: Long,
        syncStatus: SyncStatus,
    ) = error("Not used")

    override suspend fun updateRole(
        userId: Uuid,
        role: com.github.narcispurghel.rxcatalog.persistence.UserRole,
        updatedAt: Long,
        syncStatus: SyncStatus,
    ) = error("Not used")

    override suspend fun delete(user: UserEntity) = error("Not used")
}

private class NoOpApprovalHistoryDao : ApprovalHistoryDao {
    override suspend fun insert(entry: ApprovalHistoryEntity) = error("Not used")
    override suspend fun upsert(entry: ApprovalHistoryEntity) = error("Not used")
    override suspend fun upsertAll(entries: List<ApprovalHistoryEntity>) = error("Not used")
    override suspend fun getById(approvalHistoryId: Uuid): ApprovalHistoryEntity? = error("Not used")
    override fun observeById(approvalHistoryId: Uuid): Flow<ApprovalHistoryEntity?> = error("Not used")
    override fun observeBySubmissionId(submissionId: Uuid): Flow<List<ApprovalHistoryEntity>> = error("Not used")
    override fun observeByReviewerUserId(reviewerUserId: Uuid): Flow<List<ApprovalHistoryEntity>> = error("Not used")
    override fun observeByAction(action: ApprovalAction): Flow<List<ApprovalHistoryEntity>> = error("Not used")

    override fun observeBySyncStatus(syncStatus: SyncStatus): Flow<List<ApprovalHistoryEntity>> = error("Not used")
    override suspend fun getLatestForSubmission(submissionId: Uuid): ApprovalHistoryEntity? = error("Not used")
    override suspend fun delete(entry: ApprovalHistoryEntity) = error("Not used")
}

private class NoOpRxCatalogDatabase : RxCatalogDatabase() {
    override fun userDao(): UserDao = error("Not used")
    override fun medicineDao(): MedicineDao = error("Not used")
    override fun submittedLeafletDao(): SubmittedLeafletDao = error("Not used")
    override fun approvedLeafletDao(): ApprovedLeafletDao = error("Not used")
    override fun approvalHistoryDao(): ApprovalHistoryDao = error("Not used")
    override fun favoriteDao(): FavoriteDao = error("Not used")
    override fun createInvalidationTracker() = androidx.room.InvalidationTracker(this)
    override fun clearAllTables() = Unit
}

private class NoOpSubmittedLeafletDao : SubmittedLeafletDao {
    override suspend fun insert(submission: SubmittedLeafletEntity) = error("Not used")
    override suspend fun upsert(submission: SubmittedLeafletEntity) = error("Not used")
    override suspend fun upsertAll(submissions: List<SubmittedLeafletEntity>) = error("Not used")
    override suspend fun getById(submissionId: Uuid): SubmittedLeafletEntity? = error("Not used")
    override fun observeById(submissionId: Uuid): Flow<SubmittedLeafletEntity?> = error("Not used")
    override fun observeAll(): Flow<List<SubmittedLeafletEntity>> = error("Not used")
    override fun observeByMedicineId(medicineId: Uuid): Flow<List<SubmittedLeafletEntity>> = error("Not used")
    override fun observeBySubmittedByUserId(submittedByUserId: Uuid): Flow<List<SubmittedLeafletEntity>> =
        error("Not used")

    override fun observeByReviewedByUserId(reviewedByUserId: Uuid): Flow<List<SubmittedLeafletEntity>> =
        error("Not used")

    override fun observeByStatus(status: SubmissionStatus): Flow<List<SubmittedLeafletEntity>> = error("Not used")
    override fun observeBySubmittedByUserIdAndStatus(
        submittedByUserId: Uuid,
        status: SubmissionStatus,
    ): Flow<List<SubmittedLeafletEntity>> = error("Not used")

    override fun search(query: String): Flow<List<SubmittedLeafletEntity>> = error("Not used")
    override fun observeBySyncStatus(syncStatus: SyncStatus): Flow<List<SubmittedLeafletEntity>> = error("Not used")
    override suspend fun updateReviewState(
        submissionId: Uuid,
        reviewedByUserId: Uuid?,
        status: SubmissionStatus,
        rejectionReason: String?,
        reviewedAt: Long?,
        updatedAt: Long,
        syncStatus: SyncStatus,
    ) = error("Not used")

    override suspend fun updateDraft(
        submissionId: Uuid,
        title: String,
        content: String,
        updatedAt: Long,
        syncStatus: SyncStatus,
    ) = error("Not used")

    override suspend fun updateStatus(
        submissionId: Uuid,
        status: SubmissionStatus,
        updatedAt: Long,
        syncStatus: SyncStatus,
    ) = error("Not used")

    override suspend fun delete(submission: SubmittedLeafletEntity) = error("Not used")
}
