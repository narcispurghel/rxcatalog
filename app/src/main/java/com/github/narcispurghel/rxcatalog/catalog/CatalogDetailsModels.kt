package com.github.narcispurghel.rxcatalog.catalog

data class MedicineDetailsItem(
    val medicineId: String,
    val canonicalName: String,
    val brandName: String?,
    val activeIngredient: String?,
    val atcCode: String?,
    val description: String?,
    val approvedLeaflet: ApprovedLeafletItem?,
)

data class ApprovedLeafletItem(
    val leafletId: String,
    val medicineId: String,
    val title: String,
    val content: String,
    val version: Int,
    val approvedAtLabel: String,
)

data class LeafletDetailsItem(
    val leafletId: String,
    val medicineName: String,
    val title: String,
    val content: String,
    val version: Int,
    val approvedAtLabel: String,
)

data class SubmissionDetailsItem(
    val submissionId: String,
    val medicineId: String,
    val medicineName: String,
    val submittedBy: String,
    val title: String,
    val content: String,
    val statusLabel: String,
    val isUrgent: Boolean,
    val createdAtLabel: String,
    val updatedAtLabel: String,
    val reviewedAtLabel: String?,
    val reviewedBy: String?,
    val rejectionReason: String?,
    val latestReviewNote: String?,
)
