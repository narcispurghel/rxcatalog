@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.catalog

import androidx.room.withTransaction
import com.github.narcispurghel.rxcatalog.auth.PasswordHasher
import com.github.narcispurghel.rxcatalog.network.CatalogApiService
import com.github.narcispurghel.rxcatalog.network.OpenFdaApiService
import com.github.narcispurghel.rxcatalog.network.dto.ApprovedLeafletDto
import com.github.narcispurghel.rxcatalog.network.dto.MedicineDto
import com.github.narcispurghel.rxcatalog.network.dto.OpenFdaDrugLabelDto
import com.github.narcispurghel.rxcatalog.network.dto.OpenFdaFieldsDto
import com.github.narcispurghel.rxcatalog.persistence.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlin.uuid.Uuid

@Singleton
class RoomCatalogRepository
	@Inject
	constructor(
		private val database: RxCatalogDatabase,
		private val medicineDao: MedicineDao,
		private val approvedLeafletDao: ApprovedLeafletDao,
		private val submittedLeafletDao: SubmittedLeafletDao,
		private val approvalHistoryDao: ApprovalHistoryDao,
		private val userDao: UserDao,
		private val passwordHasher: PasswordHasher,
		private val catalogApiService: CatalogApiService,
		private val openFdaApiService: OpenFdaApiService,
	) : CatalogRepository {
		private val seedMutex = Mutex()

		@Volatile
		private var seedChecked = false

		override fun observeMedicines(query: String): Flow<List<MedicineListItem>> =
			flow {
				ensureSeedData()
				val medicineFlow =
					if (query.isBlank()) {
						medicineDao.observeAll()
					} else {
						medicineDao.search(query)
					}

				emitAll(
					medicineFlow.combine(approvedLeafletDao.observeAll()) { medicines, leaflets ->
						val medicineIdsWithLeaflets = leaflets.map { it.medicineId }.toSet()
						medicines.map { medicine ->
							medicine.toMedicineListItem(
								hasApprovedLeaflet = medicine.medicineId in medicineIdsWithLeaflets,
							)
						}
					},
				)
			}

		override fun observeMedicineDetails(medicineId: Uuid): Flow<MedicineDetailsItem?> =
			medicineDao
				.observeById(medicineId)
				.combine(approvedLeafletDao.observeByMedicineId(medicineId)) { medicine, leaflet ->
					medicine?.toMedicineDetailsItem(leaflet)
				}

		override fun observeLeafletDetails(leafletId: Uuid): Flow<LeafletDetailsItem?> =
			approvedLeafletDao
				.observeById(leafletId)
				.combine(medicineDao.observeAll()) { leaflet, medicines ->
					leaflet?.toLeafletDetailsItem(medicines)
				}

		override fun observeSubmissionDetails(submissionId: Uuid): Flow<SubmissionDetailsItem?> =
			flow {
				ensureSeedData()
				emitAll(
					combine(
						submittedLeafletDao.observeById(submissionId),
						medicineDao.observeAll(),
						userDao.observeAll(),
						approvalHistoryDao.observeBySubmissionId(submissionId),
					) { submission, medicines, users, history ->
						val currentSubmission = submission ?: return@combine null
						val medicine =
							medicines.firstOrNull { it.medicineId == currentSubmission.medicineId }
								?: return@combine null
						currentSubmission.toSubmissionDetailsItem(
							medicine = medicine,
							usersById = users.associateBy { it.userId },
							latestHistory = history.lastOrNull(),
						)
					},
				)
			}

		override fun observeSubmissionsForUser(userId: Uuid): Flow<List<SubmissionListItem>> =
			flow {
				ensureSeedData()
				emitAll(
					submittedLeafletDao
						.observeBySubmittedByUserId(userId)
						.combine(medicineDao.observeAll()) { submissions, medicines ->
							val medicinesById = medicines.associateBy { it.medicineId }
							submissions.mapNotNull { submission ->
								val medicine =
									medicinesById[submission.medicineId] ?: return@mapNotNull null
								submission.toSubmissionListItem(medicine)
							}
						},
				)
			}

		override fun observePendingApprovals(): Flow<List<PendingApprovalListItem>> =
			flow {
				ensureSeedData()
				emitAll(
					combine(
						submittedLeafletDao.observeByStatus(SubmissionStatus.PENDING_REVIEW),
						medicineDao.observeAll(),
						userDao.observeAll(),
					) { submissions, medicines, users ->
						val medicinesById = medicines.associateBy { it.medicineId }
						val usersById = users.associateBy { it.userId }
						submissions.mapNotNull { submission ->
							val medicine =
								medicinesById[submission.medicineId] ?: return@mapNotNull null
							val submitter =
								usersById[submission.submittedByUserId] ?: return@mapNotNull null
							submission.toPendingApprovalListItem(medicine, submitter)
						}
					},
				)
			}

		override suspend fun ensureSeedData() {
			if (seedChecked) {
				return
			}

			seedMutex.withLock {
				if (seedChecked) {
					return
				}

				val hasMedicines = medicineDao.observeAll().first().isNotEmpty()
				if (!hasMedicines) {
					val seedData = buildCatalogSeedData(passwordHasher)
					userDao.upsertAll(seedData.users)
					medicineDao.upsertAll(seedData.medicines)
					submittedLeafletDao.upsertAll(seedData.submissions)
				}

				seedChecked = true
			}
		}

		override suspend fun refreshMedicines(query: String) {
			val response =
				openFdaApiService.searchDrugLabels(
					search = query.toOpenFdaMedicineSearch(),
				)
			val now = System.currentTimeMillis()
			val currentMedicines = medicineDao.observeAll().first().associateBy { it.medicineId }
			val remoteMedicines =
				response.results
					.mapNotNull { dto ->
						dto.toMedicineEntity(currentMedicines, now)?.let { medicine -> dto to medicine }
					}.distinctBy { (_, medicine) -> medicine.medicineId }
			medicineDao.upsertAll(remoteMedicines.map { (_, medicine) -> medicine })
			remoteMedicines.forEach { (dto, medicine) ->
				dto.toOpenFdaApprovedLeaflet(
					medicine = medicine,
					now = now,
					currentLeaflet = approvedLeafletDao.getByMedicineId(medicine.medicineId),
				)?.let { approvedLeaflet ->
					approvedLeafletDao.upsert(approvedLeaflet)
				}
			}
		}

		override suspend fun refreshMedicineDetails(medicineId: Uuid) {
			val currentMedicine = medicineDao.getById(medicineId)
			val searchQuery = currentMedicine?.toOpenFdaDetailSearch() ?: return
			val response =
				openFdaApiService.searchDrugLabels(
					search = searchQuery,
					limit = 1,
				)
			val drugLabel = response.results.firstOrNull() ?: return
			val now = System.currentTimeMillis()
			val updatedMedicine =
				drugLabel.toMedicineEntity(
					medicineId = medicineId,
					currentMedicine = currentMedicine,
					now = now,
				)
			medicineDao.upsert(updatedMedicine)
			drugLabel.toOpenFdaApprovedLeaflet(
				medicine = updatedMedicine,
				now = now,
				currentLeaflet = approvedLeafletDao.getByMedicineId(medicineId),
			)?.let { approvedLeaflet ->
				approvedLeafletDao.upsert(approvedLeaflet)
			}
		}

		override suspend fun refreshLeafletDetails(leafletId: Uuid) {
			val response = catalogApiService.getLeafletDetails(leafletId.toString())
			val now = System.currentTimeMillis()
			val medicineId = response.approvedLeaflet?.medicineId?.toUuid()
			val currentMedicine = medicineId?.let { medicineDao.getById(it) }
			if (medicineId != null) {
				medicineDao.upsert(
					response.medicine.toMedicineEntity(
						createdAt = currentMedicine?.createdAt ?: now,
						updatedAt = response.medicine.lastUpdatedAt ?: now,
					),
				)
			}

			val currentLeaflet = approvedLeafletDao.getById(leafletId)
			val approvedLeaflet = response.approvedLeaflet
			if (approvedLeaflet == null) {
				if (currentLeaflet != null) {
					approvedLeafletDao.delete(currentLeaflet)
				}
			} else {
				upsertApprovedLeaflet(approvedLeaflet, now, currentLeaflet)
			}
		}

		override suspend fun saveSubmissionDraft(
			submissionId: Uuid?,
			medicineId: Uuid,
			submittedByUserId: Uuid,
			title: String,
			content: String,
		): Uuid {
			ensureSeedData()
			val now = System.currentTimeMillis()
			val resolvedSubmissionId = submissionId ?: Uuid.random()
			val currentSubmission = submittedLeafletDao.getById(resolvedSubmissionId)
			if (currentSubmission?.status == SubmissionStatus.APPROVED) {
				return resolvedSubmissionId
			}
			val submission =
				SubmittedLeafletEntity(
					submissionId = resolvedSubmissionId,
					medicineId = currentSubmission?.medicineId ?: medicineId,
					submittedByUserId = currentSubmission?.submittedByUserId ?: submittedByUserId,
					reviewedByUserId = null,
					title = title,
					content = content,
					status = SubmissionStatus.DRAFT,
					rejectionReason = null,
					createdAt = currentSubmission?.createdAt ?: now,
					updatedAt = now,
					reviewedAt = null,
					syncStatus =
						if (currentSubmission == null) {
							com.github.narcispurghel.rxcatalog.persistence.SyncStatus.PENDING_CREATE
						} else {
							com.github.narcispurghel.rxcatalog.persistence.SyncStatus.PENDING_UPDATE
						},
				)
			submittedLeafletDao.upsert(submission)
			return resolvedSubmissionId
		}

		override suspend fun submitForReview(
			submissionId: Uuid,
			reviewedAt: Long,
		) {
			ensureSeedData()
			submittedLeafletDao.updateStatus(
				submissionId = submissionId,
				status = SubmissionStatus.PENDING_REVIEW,
				updatedAt = reviewedAt,
				syncStatus = com.github.narcispurghel.rxcatalog.persistence.SyncStatus.PENDING_UPDATE,
			)
		}

		override suspend fun reviewSubmission(
			submissionId: Uuid,
			reviewerUserId: Uuid,
			approve: Boolean,
			notes: String?,
			reviewedAt: Long,
		) {
			ensureSeedData()
			database.withTransaction {
				val current = submittedLeafletDao.getById(submissionId) ?: return@withTransaction
				val status = if (approve) SubmissionStatus.APPROVED else SubmissionStatus.REJECTED
				submittedLeafletDao.updateReviewState(
					submissionId = submissionId,
					reviewedByUserId = reviewerUserId,
					status = status,
					rejectionReason = if (approve) null else notes?.takeIf { it.isNotBlank() },
					reviewedAt = reviewedAt,
					updatedAt = reviewedAt,
					syncStatus = com.github.narcispurghel.rxcatalog.persistence.SyncStatus.PENDING_UPDATE,
				)

				if (approve) {
					val currentLeaflet = approvedLeafletDao.getByMedicineId(current.medicineId)
					approvedLeafletDao.upsert(
						ApprovedLeafletEntity(
							leafletId = currentLeaflet?.leafletId ?: Uuid.random(),
							medicineId = current.medicineId,
							sourceSubmissionId = current.submissionId,
							approvedByUserId = reviewerUserId,
							title = current.title,
							content = current.content,
							version = (currentLeaflet?.version ?: 0) + 1,
							approvedAt = reviewedAt,
							createdAt = currentLeaflet?.createdAt ?: reviewedAt,
							updatedAt = reviewedAt,
							syncStatus =
								com.github.narcispurghel.rxcatalog.persistence.SyncStatus.PENDING_UPDATE,
						),
					)
				}

				approvalHistoryDao.insert(
					ApprovalHistoryEntity(
						approvalHistoryId = Uuid.random(),
						submissionId = current.submissionId,
						reviewerUserId = reviewerUserId,
						action = if (approve) ApprovalAction.APPROVE else ApprovalAction.REJECT,
						notes = notes?.takeIf { it.isNotBlank() },
						actedAt = reviewedAt,
						createdAt = reviewedAt,
						syncStatus = com.github.narcispurghel.rxcatalog.persistence.SyncStatus.PENDING_UPDATE,
					),
				)
			}
		}

		private fun MedicineEntity.toMedicineListItem(
			hasApprovedLeaflet: Boolean,
		): MedicineListItem =
			MedicineListItem(
				medicineId = medicineId.toString(),
				canonicalName = canonicalName,
				brandName = brandName,
				activeIngredient = activeIngredient,
				atcCode = atcCode,
				description = description,
				hasApprovedLeaflet = hasApprovedLeaflet,
			)

		private fun MedicineEntity.toMedicineDetailsItem(
			leaflet: ApprovedLeafletEntity?,
		): MedicineDetailsItem =
			MedicineDetailsItem(
				medicineId = medicineId.toString(),
				canonicalName = canonicalName,
				brandName = brandName,
				activeIngredient = activeIngredient,
				atcCode = atcCode,
				description = description,
				approvedLeaflet =
					leaflet?.toApprovedLeafletItem(
						medicineId = medicineId.toString(),
					),
			)

		private fun ApprovedLeafletEntity.toApprovedLeafletItem(
			medicineId: String,
		): ApprovedLeafletItem =
			ApprovedLeafletItem(
				leafletId = leafletId.toString(),
				medicineId = medicineId,
				title = title,
				content = content,
				version = version,
				approvedAtLabel = approvedAt.toRelativeLabel(),
			)

		private fun ApprovedLeafletEntity.toLeafletDetailsItem(
			medicines: List<MedicineEntity>,
		): LeafletDetailsItem {
			val medicineName =
				medicines.firstOrNull { it.medicineId == medicineId }?.canonicalName ?: "Medicine"
			return LeafletDetailsItem(
				leafletId = leafletId.toString(),
				medicineName = medicineName,
				title = title,
				content = content,
				version = version,
				approvedAtLabel = approvedAt.toRelativeLabel(),
			)
		}

		private fun SubmittedLeafletEntity.toSubmissionListItem(
			medicine: MedicineEntity,
		): SubmissionListItem =
			SubmissionListItem(
				submissionId = submissionId.toString(),
				medicineName = medicine.canonicalName,
				title = title,
				statusLabel = status.toLabel(),
				updatedLabel = "Updated ${updatedAt.toRelativeLabel()}",
				actionLabel = if (status == SubmissionStatus.APPROVED) "Open" else "Edit",
			)

		private fun SubmittedLeafletEntity.toPendingApprovalListItem(
			medicine: MedicineEntity,
			submitter: UserEntity,
		): PendingApprovalListItem =
			PendingApprovalListItem(
				submissionId = submissionId.toString(),
				medicineName = medicine.canonicalName,
				submittedBy = submitter.displayName,
				createdAtLabel = createdAt.toRelativeLabel(),
				isUrgent = isUrgent(),
			)

		private fun SubmittedLeafletEntity.toSubmissionDetailsItem(
			medicine: MedicineEntity,
			usersById: Map<Uuid, UserEntity>,
			latestHistory: ApprovalHistoryEntity?,
		): SubmissionDetailsItem =
			SubmissionDetailsItem(
				submissionId = submissionId.toString(),
				medicineId = medicine.medicineId.toString(),
				medicineName = medicine.canonicalName,
				submittedBy = usersById[submittedByUserId]?.displayName ?: "Unknown submitter",
				title = title,
				content = content,
				statusLabel = status.toLabel(),
				createdAtLabel = "Submitted ${createdAt.toRelativeLabel()}",
				updatedAtLabel = "Updated ${updatedAt.toRelativeLabel()}",
				reviewedAtLabel = reviewedAt?.let { "Reviewed ${it.toRelativeLabel()}" },
				reviewedBy =
					listOfNotNull(
						reviewedByUserId,
						latestHistory?.reviewerUserId,
					).firstOrNull()?.let { reviewerId ->
						usersById[reviewerId]?.displayName
					},
				rejectionReason = rejectionReason,
				latestReviewNote = latestHistory?.notes,
			)

		private fun SubmissionStatus.toLabel(): String =
			when (this) {
				SubmissionStatus.DRAFT -> "Draft"
				SubmissionStatus.PENDING_REVIEW -> "Pending review"
				SubmissionStatus.APPROVED -> "Approved"
				SubmissionStatus.REJECTED -> "Rejected"
				SubmissionStatus.WITHDRAWN -> "Withdrawn"
			}

		private fun SubmittedLeafletEntity.isUrgent(): Boolean =
			status == SubmissionStatus.PENDING_REVIEW &&
				System.currentTimeMillis() - createdAt <= 2 * HOUR

		private suspend fun upsertApprovedLeaflet(
			dto: ApprovedLeafletDto,
			now: Long,
			currentLeaflet: ApprovedLeafletEntity?,
		) {
			val leafletId = dto.id.toUuid()
			val medicineId = dto.medicineId.toUuid()
			val entity =
				ApprovedLeafletEntity(
					leafletId = leafletId,
					medicineId = medicineId,
					sourceSubmissionId = null,
					approvedByUserId = null,
					title = dto.title,
					content = dto.content,
					version = dto.version,
					approvedAt = dto.publishedAt ?: now,
					createdAt = currentLeaflet?.createdAt ?: dto.publishedAt ?: now,
					updatedAt = dto.publishedAt ?: now,
					syncStatus = com.github.narcispurghel.rxcatalog.persistence.SyncStatus.SYNCED,
				)

			if (currentLeaflet != null && currentLeaflet.leafletId != leafletId) {
				approvedLeafletDao.delete(currentLeaflet)
			}
			approvedLeafletDao.upsert(entity)
		}

		private fun Long.toRelativeLabel(now: Long = System.currentTimeMillis()): String {
			val elapsed = (now - this).coerceAtLeast(0L)
			return when {
				elapsed < HOUR -> "${elapsed / MINUTE} min ago"
				elapsed < DAY -> "${elapsed / HOUR} hour${if (elapsed / HOUR == 1L) "" else "s"} ago"
				elapsed < 7 * DAY -> "${elapsed / DAY} day${if (elapsed / DAY == 1L) "" else "s"} ago"
				else -> "Older"
			}
		}

		private companion object {
			private const val MINUTE = 60_000L
			private const val HOUR = 60 * MINUTE
			private const val DAY = 24 * HOUR
		}

		private fun MedicineDto.toMedicineEntity(
			createdAt: Long,
			updatedAt: Long,
		): MedicineEntity =
			MedicineEntity(
				medicineId = id.toUuid(),
				canonicalName = canonicalName,
				brandName = brandName,
				activeIngredient = activeIngredient,
				atcCode = atcCode,
				description = description,
				createdAt = createdAt,
				updatedAt = updatedAt,
				syncStatus = com.github.narcispurghel.rxcatalog.persistence.SyncStatus.SYNCED,
			)

		private fun OpenFdaDrugLabelDto.toMedicineEntity(
			currentMedicines: Map<Uuid, MedicineEntity>,
			now: Long,
		): MedicineEntity? {
			val fields = openFda ?: return null
			val productName =
				fields.brandName.firstClean()
					?: fields.genericName.firstClean()
					?: fields.substanceName.firstClean()
					?: return null
			val activeIngredient =
				fields.substanceName.firstClean()
					?: fields.genericName.firstClean()
			val medicineId = stableOpenFdaMedicineId(fields, setId, productName)
			val currentMedicine = currentMedicines[medicineId]

			return MedicineEntity(
				medicineId = medicineId,
				canonicalName = productName,
				brandName = null,
				activeIngredient = activeIngredient,
				atcCode = null,
				description = null,
				createdAt = currentMedicine?.createdAt ?: now,
				updatedAt = now,
				syncStatus = com.github.narcispurghel.rxcatalog.persistence.SyncStatus.SYNCED,
			)
		}

		private fun OpenFdaDrugLabelDto.toMedicineEntity(
			medicineId: Uuid,
			currentMedicine: MedicineEntity?,
			now: Long,
		): MedicineEntity {
			val fields = openFda
			val productName =
				fields?.brandName?.firstClean()
					?: fields?.genericName?.firstClean()
					?: fields?.substanceName?.firstClean()
					?: currentMedicine?.canonicalName
					?: "Medicine"
			val activeIngredient =
				fields?.substanceName?.firstClean()
					?: fields?.genericName?.firstClean()
					?: currentMedicine?.activeIngredient
			val description =
				purpose.firstClean()
					?: indicationsAndUsage.firstClean()
					?: currentMedicine?.description

			return MedicineEntity(
				medicineId = medicineId,
				canonicalName = productName,
				brandName = fields?.brandName?.firstClean(),
				activeIngredient = activeIngredient,
				atcCode = currentMedicine?.atcCode,
				description = description,
				createdAt = currentMedicine?.createdAt ?: now,
				updatedAt = now,
				syncStatus = com.github.narcispurghel.rxcatalog.persistence.SyncStatus.SYNCED,
			)
		}

		private fun OpenFdaDrugLabelDto.toOpenFdaApprovedLeaflet(
			medicine: MedicineEntity,
			now: Long,
			currentLeaflet: ApprovedLeafletEntity?,
		): ApprovedLeafletEntity? {
			val sections =
				listOfNotNull(
					purpose.firstClean()?.let { "Purpose\n$it" },
					indicationsAndUsage.firstClean()?.let { "Indications and usage\n$it" },
					dosageAndAdministration.firstClean()?.let { "Dosage and administration\n$it" },
					warnings.firstClean()?.let { "Warnings\n$it" },
					doNotUse.firstClean()?.let { "Do not use\n$it" },
					askDoctor.firstClean()?.let { "Ask a doctor\n$it" },
					stopUse.firstClean()?.let { "Stop use\n$it" },
					pregnancyOrBreastFeeding.firstClean()?.let { "Pregnancy or breast-feeding\n$it" },
					keepOutOfReachOfChildren.firstClean()?.let {
						"Keep out of reach of children\n$it"
					},
				)
			if (sections.isEmpty()) return null

			val approvedAt = effectiveTime.toEpochMillisOrNull() ?: now
			return ApprovedLeafletEntity(
				leafletId = currentLeaflet?.leafletId ?: stableLeafletId(medicine.medicineId),
				medicineId = medicine.medicineId,
				sourceSubmissionId = currentLeaflet?.sourceSubmissionId,
				approvedByUserId = currentLeaflet?.approvedByUserId,
				title = "${medicine.canonicalName} OpenFDA label",
				content = sections.joinToString("\n\n"),
				version = currentLeaflet?.version ?: 1,
				approvedAt = approvedAt,
				createdAt = currentLeaflet?.createdAt ?: approvedAt,
				updatedAt = now,
				syncStatus = com.github.narcispurghel.rxcatalog.persistence.SyncStatus.SYNCED,
			)
		}

		private fun stableOpenFdaMedicineId(
			fields: OpenFdaFieldsDto,
			setId: String?,
			canonicalName: String,
		): Uuid {
			val stableKey =
				listOfNotNull(
					fields.rxcui.firstClean()?.let { "rxcui:$it" },
					fields.productNdc.firstClean()?.let { "product_ndc:$it" },
					fields.splId.firstClean()?.let { "spl_id:$it" },
					setId?.takeIf { it.isNotBlank() }?.let { "set_id:$it" },
					fields.brandName.firstClean()?.let { "brand:$it|generic:$canonicalName" },
					"openfda:$canonicalName",
				).first()

			return Uuid.parse(
				UUID
					.nameUUIDFromBytes(stableKey.toByteArray(StandardCharsets.UTF_8))
					.toString(),
			)
		}

		private fun String.toOpenFdaMedicineSearch(): String =
			trim()
				.takeIf { it.isNotBlank() }
				?.let { query ->
					val escapedQuery = query.replace("\"", "\\\"")
					"openfda.brand_name:\"$escapedQuery\" OR " +
						"openfda.generic_name:\"$escapedQuery\" OR " +
					"openfda.substance_name:\"$escapedQuery\""
				} ?: "_exists_:openfda.brand_name"

		private fun MedicineEntity.toOpenFdaDetailSearch(): String =
			listOfNotNull(
				brandName?.takeIf { it.isNotBlank() }?.let {
					"openfda.brand_name:${it.quoteForOpenFda()}"
				},
				canonicalName.takeIf { it.isNotBlank() }?.let {
					"openfda.generic_name:${it.quoteForOpenFda()}"
				},
				activeIngredient?.takeIf { it.isNotBlank() }?.let {
					"openfda.substance_name:${it.quoteForOpenFda()}"
				},
			).joinToString(" OR ")

		private fun String.quoteForOpenFda(): String = "\"${replace("\"", "\\\"")}\""

		private fun List<String>.firstClean(): String? =
			firstOrNull { it.isNotBlank() }
				?.replace(Regex("\\s+"), " ")
				?.trim()

		private fun String.toUuid(): Uuid = Uuid.parse(this)

		private fun stableLeafletId(medicineId: Uuid): Uuid =
			Uuid.parse(
				UUID
					.nameUUIDFromBytes(
						"openfda-leaflet:$medicineId".toByteArray(StandardCharsets.UTF_8),
					).toString(),
			)

		private fun String?.toEpochMillisOrNull(): Long? {
			if (this == null || length != 8) return null
			val year = substring(0, 4).toIntOrNull() ?: return null
			val month = substring(4, 6).toIntOrNull() ?: return null
			val day = substring(6, 8).toIntOrNull() ?: return null
			return java.time.LocalDate
				.of(year, month, day)
				.atStartOfDay(java.time.ZoneOffset.UTC)
				.toInstant()
				.toEpochMilli()
		}
	}
