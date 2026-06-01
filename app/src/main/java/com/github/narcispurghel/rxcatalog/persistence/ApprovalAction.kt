package com.github.narcispurghel.rxcatalog.persistence

enum class ApprovalAction(val storageValue: String) {
    APPROVE("approve"),
    REJECT("reject"),
    REQUEST_CHANGES("request_changes"),
    REPLACE("replace");

    companion object {
        fun fromStorageValue(value: String): ApprovalAction =
            entries.firstOrNull { it.storageValue == value } ?: APPROVE
    }
}
