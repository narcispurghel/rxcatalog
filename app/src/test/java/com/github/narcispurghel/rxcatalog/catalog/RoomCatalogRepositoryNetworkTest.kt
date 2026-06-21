@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.catalog

import com.github.narcispurghel.rxcatalog.auth.PasswordHasher
import com.github.narcispurghel.rxcatalog.network.CatalogApiService
import com.github.narcispurghel.rxcatalog.network.OpenFdaApiService
import com.github.narcispurghel.rxcatalog.persistence.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
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
	private lateinit var openFdaApiService: OpenFdaApiService

	@Before
	fun setUp() {
		server = MockWebServer()
		server.start()
		apiService =
			Retrofit
				.Builder()
				.baseUrl(server.url("/"))
				.addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
				.build()
				.create(CatalogApiService::class.java)
		openFdaApiService =
			Retrofit
				.Builder()
				.baseUrl(server.url("/"))
				.addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
				.build()
				.create(OpenFdaApiService::class.java)
	}

	@After
	fun tearDown() {
		server.shutdown()
	}

	@Test
	fun `refreshMedicines parses remote medicines and stores them in the local catalogue`() =
		runTest {
			server.enqueue(
				MockResponse()
					.setResponseCode(200)
					.setBody(
						"""
						{
						  "results": [
						    {
						      "set_id": "openfda-set-aspirin",
						      "effective_time": "20240115",
						      "purpose": ["Pain reliever"],
						      "warnings": ["Reye's syndrome warning applies."],
						      "openfda": {
						        "brand_name": ["Aspirin Protect"],
						        "generic_name": ["Aspirin 100 mg"],
						        "substance_name": ["Acetylsalicylic acid"],
						        "product_type": ["HUMAN OTC DRUG"],
						        "rxcui": ["1191"],
						        "product_ndc": ["0000-0001"]
						      }
						    }
						  ]
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
					openFdaApiService = openFdaApiService,
				)

			repository.refreshMedicines("aspirin")

			val request = server.takeRequest()
			assertEquals("/drug/label.json", request.requestUrl?.encodedPath)
			assertEquals(25.toString(), request.requestUrl?.queryParameter("limit"))
			assertEquals(
				"openfda.brand_name:\"aspirin\" OR " +
					"openfda.generic_name:\"aspirin\" OR " +
					"openfda.substance_name:\"aspirin\"",
				request.requestUrl?.queryParameter("search"),
			)
			val stored = medicineDao.observeAll().first().single()
			assertNotNull(stored)
			assertEquals("Aspirin Protect", stored?.canonicalName)
			assertEquals(null, stored?.brandName)
			assertEquals("Acetylsalicylic acid", stored?.activeIngredient)
			assertEquals(null, stored?.atcCode)
			assertEquals(null, stored?.description)
			assertEquals(SyncStatus.SYNCED, stored?.syncStatus)
			val storedLeaflet = approvedLeafletDao.getByMedicineId(stored!!.medicineId)
			assertNotNull(storedLeaflet)
			assertEquals("Aspirin Protect OpenFDA label", storedLeaflet?.title)
		}

	@Test
	fun `refreshMedicineDetails uses OpenFDA details and stores approved leaflets`() =
		runTest {
			val medicineId = Uuid.parse("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
			server.enqueue(
				MockResponse()
					.setResponseCode(200)
					.setBody(
						"""
						{
						  "results": [
						    {
						      "set_id": "openfda-set-ibuprofen",
						      "effective_time": "20240115",
						      "purpose": ["Pain reliever/fever reducer"],
						      "indications_and_usage": ["Temporarily relieves minor aches and pains."],
						      "dosage_and_administration": ["Use as directed on the package."],
						      "warnings": ["Stomach bleeding warning applies."],
						      "openfda": {
						        "brand_name": ["Nurofen"],
						        "generic_name": ["Ibuprofen 200 mg"],
						        "substance_name": ["Ibuprofen"],
						        "product_type": ["HUMAN OTC DRUG"],
						        "rxcui": ["5640"]
						      }
						    }
						  ]
						}
						""".trimIndent(),
					),
			)

			val medicineDao = FakeMedicineDao()
			medicineDao.upsert(
				MedicineEntity(
					medicineId = medicineId,
					canonicalName = "Ibuprofen 200 mg",
					brandName = "Nurofen",
					activeIngredient = "Ibuprofen",
					atcCode = "M01AE01",
					description = null,
					createdAt = 1700000000000,
					updatedAt = 1700000000000,
					syncStatus = SyncStatus.SYNCED,
				),
			)
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
					openFdaApiService = openFdaApiService,
				)

			repository.refreshMedicineDetails(medicineId)

			val request = server.takeRequest()
			assertEquals("/drug/label.json", request.requestUrl?.encodedPath)
			assertEquals("1", request.requestUrl?.queryParameter("limit"))
			assertEquals(
				"openfda.brand_name:\"Nurofen\" OR " +
					"openfda.generic_name:\"Ibuprofen 200 mg\" OR " +
					"openfda.substance_name:\"Ibuprofen\"",
				request.requestUrl?.queryParameter("search"),
			)
			val storedMedicine = medicineDao.getById(medicineId)
			assertNotNull(storedMedicine)
			assertEquals("Nurofen", storedMedicine?.canonicalName)
			assertEquals("Nurofen", storedMedicine?.brandName)
			assertEquals("Pain reliever/fever reducer", storedMedicine?.description)
			val storedLeaflet = approvedLeafletDao.getByMedicineId(medicineId)
			assertNotNull(storedLeaflet)
			assertEquals("Nurofen OpenFDA label", storedLeaflet?.title)
			assertEquals(true, storedLeaflet?.content?.contains("Warnings") == true)
			assertEquals(1, storedLeaflet?.version)
			assertEquals(medicineId, storedLeaflet?.medicineId)
		}

	@Test
	fun `refreshMedicineDetails keeps cached leaflet when OpenFDA has no matching result`() =
		runTest {
			val medicineId = Uuid.parse("cccccccc-cccc-cccc-cccc-cccccccccccc")
			val leafletId = Uuid.parse("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")
			server.enqueue(
				MockResponse()
					.setResponseCode(200)
					.setBody(
						"""
						{
						  "results": []
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
					openFdaApiService = openFdaApiService,
				)

			repository.refreshMedicineDetails(medicineId)

			val request = server.takeRequest()
			assertEquals("/drug/label.json", request.requestUrl?.encodedPath)
			assertNotNull(approvedLeafletDao.getByMedicineId(medicineId))
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
					openFdaApiService = openFdaApiService,
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
				it.title.contains(query, ignoreCase = true) ||
					it.content.contains(query, ignoreCase = true)
			}
		}

	override fun observeByApprover(approvedByUserId: Uuid): Flow<List<ApprovedLeafletEntity>> =
		leaflets.map { entries ->
			entries.values.filter { it.approvedByUserId == approvedByUserId }
		}

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

	override fun observeByNormalizedEmail(normalizedEmail: String): Flow<UserEntity?> =
		error("Not used")

	override fun observeAll(): Flow<List<UserEntity>> = error("Not used")

	override fun observeByRole(
		role: com.github.narcispurghel.rxcatalog.persistence.UserRole,
	): Flow<List<UserEntity>> = error("Not used")

	override fun observeActiveUsers(): Flow<List<UserEntity>> = error("Not used")

	override fun observeBySyncStatus(syncStatus: SyncStatus): Flow<List<UserEntity>> =
		error("Not used")

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

	override fun observeBySubmissionId(submissionId: Uuid): Flow<List<ApprovalHistoryEntity>> =
		error("Not used")

	override fun observeByReviewerUserId(reviewerUserId: Uuid): Flow<List<ApprovalHistoryEntity>> =
		error("Not used")

	override fun observeByAction(action: ApprovalAction): Flow<List<ApprovalHistoryEntity>> =
		error("Not used")

	override fun observeBySyncStatus(syncStatus: SyncStatus): Flow<List<ApprovalHistoryEntity>> =
		error("Not used")

	override suspend fun getLatestForSubmission(submissionId: Uuid): ApprovalHistoryEntity? =
		error("Not used")

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

	override fun observeByMedicineId(medicineId: Uuid): Flow<List<SubmittedLeafletEntity>> =
		error("Not used")

	override fun observeBySubmittedByUserId(
		submittedByUserId: Uuid,
	): Flow<List<SubmittedLeafletEntity>> = error("Not used")

	override fun observeByReviewedByUserId(
		reviewedByUserId: Uuid,
	): Flow<List<SubmittedLeafletEntity>> = error("Not used")

	override fun observeByStatus(status: SubmissionStatus): Flow<List<SubmittedLeafletEntity>> =
		error("Not used")

	override fun observeBySubmittedByUserIdAndStatus(
		submittedByUserId: Uuid,
		status: SubmissionStatus,
	): Flow<List<SubmittedLeafletEntity>> = error("Not used")

	override fun search(query: String): Flow<List<SubmittedLeafletEntity>> = error("Not used")

	override fun observeBySyncStatus(syncStatus: SyncStatus): Flow<List<SubmittedLeafletEntity>> =
		error("Not used")

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
