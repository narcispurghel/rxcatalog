package com.github.narcispurghel.rxcatalog.catalog

data class MedicineListItem(
    val medicineId: String,
    val canonicalName: String,
    val brandName: String?,
    val activeIngredient: String?,
    val atcCode: String?,
    val description: String?,
)

data class SubmissionListItem(
    val submissionId: String,
    val medicineName: String,
    val title: String,
    val statusLabel: String,
    val updatedLabel: String,
    val actionLabel: String,
)

data class PendingApprovalListItem(
    val submissionId: String,
    val medicineName: String,
    val submittedBy: String,
    val createdAtLabel: String,
    val isUrgent: Boolean,
)
