package com.github.narcispurghel.rxcatalog.persistence

enum class SyncStatus(val storageValue: String) {
    SYNCED("synced"),
    PENDING_CREATE("pending_create"),
    PENDING_UPDATE("pending_update"),
    PENDING_DELETE("pending_delete"),
    CONFLICT("conflict");

    companion object {
        fun fromStorageValue(value: String): SyncStatus =
            entries.firstOrNull { it.storageValue == value } ?: SYNCED
    }
}
