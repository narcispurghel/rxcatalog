@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.catalog

import com.github.narcispurghel.rxcatalog.auth.PasswordHasher
import com.github.narcispurghel.rxcatalog.network.CatalogApiService
import com.github.narcispurghel.rxcatalog.network.dto.ApprovedLeafletDto
import com.github.narcispurghel.rxcatalog.network.dto.MedicineDto
import com.github.narcispurghel.rxcatalog.persistence.MedicineDao
import com.github.narcispurghel.rxcatalog.persistence.MedicineEntity
import com.github.narcispurghel.rxcatalog.persistence.ApprovedLeafletDao
import com.github.narcispurghel.rxcatalog.persistence.ApprovedLeafletEntity
import com.github.narcispurghel.rxcatalog.persistence.SubmissionStatus
import com.github.narcispurghel.rxcatalog.persistence.SubmittedLeafletDao
import com.github.narcispurghel.rxcatalog.persistence.SubmittedLeafletEntity
import com.github.narcispurghel.rxcatalog.persistence.UserDao
import com.github.narcispurghel.rxcatalog.persistence.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.Uuid

@Singleton
class RoomCatalogRepository @Inject constructor(
    private val medicineDao: MedicineDao,
    private val approvedLeafletDao: ApprovedLeafletDao,
    private val submittedLeafletDao: SubmittedLeafletDao,
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher,
    private val catalogApiService: CatalogApiService,
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

            emitAll(medicineFlow.map { medicines -> medicines.map { it.toMedicineListItem() } })
        }

    override fun observeMedicineDetails(medicineId: Uuid): Flow<MedicineDetailsItem?> =
        medicineDao.observeById(medicineId)
            .combine(approvedLeafletDao.observeByMedicineId(medicineId)) { medicine, leaflet ->
                medicine?.toMedicineDetailsItem(leaflet)
            }

    override fun observeLeafletDetails(leafletId: Uuid): Flow<LeafletDetailsItem?> =
        approvedLeafletDao.observeById(leafletId)
            .combine(medicineDao.observeAll()) { leaflet, medicines ->
                leaflet?.toLeafletDetailsItem(medicines)
            }

    override fun observeSubmissionsForUser(userId: Uuid): Flow<List<SubmissionListItem>> =
        flow {
            ensureSeedData()
            emitAll(
                submittedLeafletDao.observeBySubmittedByUserId(userId)
                    .combine(medicineDao.observeAll()) { submissions, medicines ->
                        val medicinesById = medicines.associateBy { it.medicineId }
                        submissions.mapNotNull { submission ->
                            val medicine = medicinesById[submission.medicineId] ?: return@mapNotNull null
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
                        val medicine = medicinesById[submission.medicineId] ?: return@mapNotNull null
                        val submitter = usersById[submission.submittedByUserId] ?: return@mapNotNull null
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
        val response = catalogApiService.searchMedicines(query.takeIf { it.isNotBlank() })
        val now = System.currentTimeMillis()
        val currentMedicines = medicineDao.observeAll().first().associateBy { it.medicineId }
        val remoteMedicines = response.items.map { dto ->
            dto.toMedicineEntity(currentMedicines[dto.id.toUuid()]?.createdAt ?: now, now)
        }
        medicineDao.upsertAll(remoteMedicines)
    }

    override suspend fun refreshMedicineDetails(medicineId: Uuid) {
        val response = catalogApiService.getMedicineDetails(medicineId.toString())
        val now = System.currentTimeMillis()
        val currentMedicine = medicineDao.getById(medicineId)
        medicineDao.upsert(
            response.medicine.toMedicineEntity(
                createdAt = currentMedicine?.createdAt ?: now,
                updatedAt = response.medicine.lastUpdatedAt ?: now,
            ),
        )

        val currentLeaflet = approvedLeafletDao.getByMedicineId(medicineId)
        val approvedLeaflet = response.approvedLeaflet
        if (approvedLeaflet == null) {
            if (currentLeaflet != null) {
                approvedLeafletDao.delete(currentLeaflet)
            }
        } else {
            upsertApprovedLeaflet(approvedLeaflet, now, currentLeaflet)
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

    private fun MedicineEntity.toMedicineListItem(): MedicineListItem =
        MedicineListItem(
            medicineId = medicineId.toString(),
            canonicalName = canonicalName,
            brandName = brandName,
            activeIngredient = activeIngredient,
            atcCode = atcCode,
            description = description,
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

    private fun ApprovedLeafletEntity.toApprovedLeafletItem(medicineId: String): ApprovedLeafletItem =
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

    private fun SubmissionStatus.toLabel(): String =
        when (this) {
            SubmissionStatus.DRAFT -> "Draft"
            SubmissionStatus.PENDING_REVIEW -> "Pending review"
            SubmissionStatus.APPROVED -> "Approved"
            SubmissionStatus.REJECTED -> "Rejected"
            SubmissionStatus.WITHDRAWN -> "Withdrawn"
        }

    private fun SubmittedLeafletEntity.isUrgent(): Boolean =
        status == SubmissionStatus.PENDING_REVIEW && System.currentTimeMillis() - createdAt <= 2 * HOUR

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

    private fun String.toUuid(): Uuid = Uuid.parse(this)
}
