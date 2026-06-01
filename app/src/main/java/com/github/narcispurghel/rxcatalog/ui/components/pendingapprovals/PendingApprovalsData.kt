package com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals

fun sampleApprovalQueue(): List<ApprovalQueueItem> =
    listOf(
        ApprovalQueueItem(
            submissionId = "review-001",
            medicineName = "Aspirin 100 mg leaflet",
            submittedBy = "Dr. Popescu",
            createdAtLabel = "10 min ago",
            isUrgent = true,
        ),
        ApprovalQueueItem(
            submissionId = "review-002",
            medicineName = "Paracetamol oral suspension",
            submittedBy = "Pharmacist Ionescu",
            createdAtLabel = "1 hour ago",
            isUrgent = false,
        ),
        ApprovalQueueItem(
            submissionId = "review-003",
            medicineName = "Ibuprofen 200 mg leaflet",
            submittedBy = "Dr. Marin",
            createdAtLabel = "Today",
            isUrgent = false,
        ),
    )
