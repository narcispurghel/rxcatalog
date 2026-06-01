package com.github.narcispurghel.rxcatalog.persistence

enum class UserRole(val storageValue: String) {
    USER("user"),
    DOCTOR("doctor"),
    PHARMACIST("pharmacist");

    companion object {
        fun fromStorageValue(value: String): UserRole =
            entries.firstOrNull { it.storageValue == value } ?: USER
    }
}
