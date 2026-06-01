package com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals

data class ApprovalQueueItem(
    val submissionId: String,
    val medicineName: String,
    val submittedBy: String,
    val createdAtLabel: String,
    val isUrgent: Boolean,
)
