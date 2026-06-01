package com.github.narcispurghel.rxcatalog.persistence

enum class SubmissionStatus(val storageValue: String) {
    DRAFT("draft"),
    PENDING_REVIEW("pending_review"),
    APPROVED("approved"),
    REJECTED("rejected"),
    WITHDRAWN("withdrawn");

    companion object {
        fun fromStorageValue(value: String): SubmissionStatus =
            entries.firstOrNull { it.storageValue == value } ?: DRAFT
    }
}
