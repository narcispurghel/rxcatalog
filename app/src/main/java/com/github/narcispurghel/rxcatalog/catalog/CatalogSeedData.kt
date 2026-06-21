@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.catalog

import com.github.narcispurghel.rxcatalog.auth.PasswordHasher
import com.github.narcispurghel.rxcatalog.persistence.*
import kotlin.uuid.Uuid

internal data class CatalogSeedData(
	val users: List<UserEntity>,
	val medicines: List<MedicineEntity>,
	val submissions: List<SubmittedLeafletEntity>,
)

internal fun buildCatalogSeedData(
	passwordHasher: PasswordHasher,
	now: Long = System.currentTimeMillis(),
): CatalogSeedData {
	val doctorUserId = Uuid.parse(CatalogSeedIds.DOCTOR_USER_ID)
	val pharmacistUserId = Uuid.parse(CatalogSeedIds.PHARMACIST_USER_ID)
	val patientUserId = Uuid.parse(CatalogSeedIds.PATIENT_USER_ID)

	val aspirinMedicineId = Uuid.parse(CatalogSeedIds.ASPIRIN_MEDICINE_ID)
	val paracetamolMedicineId = Uuid.parse(CatalogSeedIds.PARACETAMOL_MEDICINE_ID)
	val ibuprofenMedicineId = Uuid.parse(CatalogSeedIds.IBUPROFEN_MEDICINE_ID)
	val amoxicillinMedicineId = Uuid.parse(CatalogSeedIds.AMOXICILLIN_MEDICINE_ID)

	val draftSubmissionId = Uuid.parse(CatalogSeedIds.DRAFT_SUBMISSION_ID)
	val pendingAspirinSubmissionId = Uuid.parse(CatalogSeedIds.PENDING_ASPIRIN_SUBMISSION_ID)
	val pendingParacetamolSubmissionId =
		Uuid.parse(CatalogSeedIds.PENDING_PARACETAMOL_SUBMISSION_ID)
	val approvedIbuprofenSubmissionId = Uuid.parse(CatalogSeedIds.APPROVED_IBUPROFEN_SUBMISSION_ID)
	val rejectedAmoxicillinSubmissionId =
		Uuid.parse(
			CatalogSeedIds.REJECTED_AMOXICILLIN_SUBMISSION_ID,
		)

	return CatalogSeedData(
		users =
			listOf(
				UserEntity(
					userId = doctorUserId,
					email = "doctor@example.com",
					displayName = "Dr. Elena Popescu",
					passwordHash = passwordHasher.hash("password123"),
					role = UserRole.DOCTOR,
					createdAt = now - DAY,
					updatedAt = now - DAY,
					lastLoginAt = now - HOUR,
					syncStatus = SyncStatus.SYNCED,
				),
				UserEntity(
					userId = pharmacistUserId,
					email = "pharmacist@example.com",
					displayName = "Pharmacist Andrei Ionescu",
					passwordHash = passwordHasher.hash("password123"),
					role = UserRole.PHARMACIST,
					createdAt = now - DAY,
					updatedAt = now - DAY,
					lastLoginAt = now - HOUR,
					syncStatus = SyncStatus.SYNCED,
				),
				UserEntity(
					userId = patientUserId,
					email = "patient@example.com",
					displayName = "Maria Ionescu",
					passwordHash = passwordHasher.hash("password123"),
					role = UserRole.USER,
					createdAt = now - DAY,
					updatedAt = now - DAY,
					lastLoginAt = now - HOUR,
					syncStatus = SyncStatus.SYNCED,
				),
			),
		medicines =
			listOf(
				MedicineEntity(
					medicineId = aspirinMedicineId,
					canonicalName = "Aspirin 100 mg",
					brandName = "Aspirin Protect",
					activeIngredient = "Acetylsalicylic acid",
					atcCode = "B01AC06",
					description = "Antiplatelet medicine used for cardiovascular prevention.",
					createdAt = now - DAY,
					updatedAt = now - HOUR,
					syncStatus = SyncStatus.SYNCED,
				),
				MedicineEntity(
					medicineId = paracetamolMedicineId,
					canonicalName = "Paracetamol 500 mg",
					brandName = "Panadol",
					activeIngredient = "Paracetamol",
					atcCode = "N02BE01",
					description = "Analgesic and antipyretic tablet.",
					createdAt = now - DAY,
					updatedAt = now - HOUR,
					syncStatus = SyncStatus.SYNCED,
				),
				MedicineEntity(
					medicineId = ibuprofenMedicineId,
					canonicalName = "Ibuprofen 200 mg",
					brandName = "Nurofen",
					activeIngredient = "Ibuprofen",
					atcCode = "M01AE01",
					description = "Nonsteroidal anti-inflammatory medicine.",
					createdAt = now - DAY,
					updatedAt = now - HOUR,
					syncStatus = SyncStatus.SYNCED,
				),
				MedicineEntity(
					medicineId = amoxicillinMedicineId,
					canonicalName = "Amoxicillin 500 mg",
					brandName = "Amoxil",
					activeIngredient = "Amoxicillin",
					atcCode = "J01CA04",
					description = "Broad-spectrum penicillin antibiotic.",
					createdAt = now - DAY,
					updatedAt = now - HOUR,
					syncStatus = SyncStatus.SYNCED,
				),
			),
		submissions =
			listOf(
				SubmittedLeafletEntity(
					submissionId = draftSubmissionId,
					medicineId = aspirinMedicineId,
					submittedByUserId = patientUserId,
					title = "Aspirin leaflet draft",
					content = "Draft safety notes and usage instructions for Aspirin 100 mg.",
					status = SubmissionStatus.DRAFT,
					createdAt = now - 3 * HOUR,
					updatedAt = now - 2 * HOUR,
					syncStatus = SyncStatus.SYNCED,
				),
				SubmittedLeafletEntity(
					submissionId = pendingAspirinSubmissionId,
					medicineId = aspirinMedicineId,
					submittedByUserId = patientUserId,
					title = "Aspirin 100 mg patient leaflet",
					content = "Updated leaflet content with dosing, contraindications, and warning signs.",
					status = SubmissionStatus.PENDING_REVIEW,
					createdAt = now - 35 * MINUTE,
					updatedAt = now - 35 * MINUTE,
					syncStatus = SyncStatus.SYNCED,
				),
				SubmittedLeafletEntity(
					submissionId = pendingParacetamolSubmissionId,
					medicineId = paracetamolMedicineId,
					submittedByUserId = pharmacistUserId,
					title = "Paracetamol oral suspension leaflet",
					content = "Paediatric dosing guidance and storage instructions.",
					status = SubmissionStatus.PENDING_REVIEW,
					createdAt = now - 95 * MINUTE,
					updatedAt = now - 95 * MINUTE,
					syncStatus = SyncStatus.SYNCED,
				),
				SubmittedLeafletEntity(
					submissionId = approvedIbuprofenSubmissionId,
					medicineId = ibuprofenMedicineId,
					submittedByUserId = doctorUserId,
					reviewedByUserId = pharmacistUserId,
					title = "Ibuprofen 200 mg leaflet",
					content = "Approved patient leaflet content for common pain and inflammation use.",
					status = SubmissionStatus.APPROVED,
					createdAt = now - 6 * HOUR,
					updatedAt = now - 2 * HOUR,
					reviewedAt = now - 90 * MINUTE,
					syncStatus = SyncStatus.SYNCED,
				),
				SubmittedLeafletEntity(
					submissionId = rejectedAmoxicillinSubmissionId,
					medicineId = amoxicillinMedicineId,
					submittedByUserId = patientUserId,
					reviewedByUserId = doctorUserId,
					title = "Amoxicillin 500 mg leaflet",
					content = "Draft antibiotic leaflet that still needs review for dosing precision.",
					status = SubmissionStatus.REJECTED,
					rejectionReason = "Needs updated dosing section for paediatric use.",
					createdAt = now - 8 * HOUR,
					updatedAt = now - 4 * HOUR,
					reviewedAt = now - 4 * HOUR,
					syncStatus = SyncStatus.SYNCED,
				),
			),
	)
}

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR
