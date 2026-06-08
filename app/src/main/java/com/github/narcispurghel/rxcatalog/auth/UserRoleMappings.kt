package com.github.narcispurghel.rxcatalog.auth

fun com.github.narcispurghel.rxcatalog.common.UserRole.toPersistenceRole():
    com.github.narcispurghel.rxcatalog.persistence.UserRole =
    when (this) {
        com.github.narcispurghel.rxcatalog.common.UserRole.USER ->
            com.github.narcispurghel.rxcatalog.persistence.UserRole.USER
        com.github.narcispurghel.rxcatalog.common.UserRole.DOCTOR ->
            com.github.narcispurghel.rxcatalog.persistence.UserRole.DOCTOR
        com.github.narcispurghel.rxcatalog.common.UserRole.PHARMACIST ->
            com.github.narcispurghel.rxcatalog.persistence.UserRole.PHARMACIST
    }

fun com.github.narcispurghel.rxcatalog.persistence.UserRole.toCommonUserRole():
    com.github.narcispurghel.rxcatalog.common.UserRole =
    when (this) {
        com.github.narcispurghel.rxcatalog.persistence.UserRole.USER ->
            com.github.narcispurghel.rxcatalog.common.UserRole.USER
        com.github.narcispurghel.rxcatalog.persistence.UserRole.DOCTOR ->
            com.github.narcispurghel.rxcatalog.common.UserRole.DOCTOR
        com.github.narcispurghel.rxcatalog.persistence.UserRole.PHARMACIST ->
            com.github.narcispurghel.rxcatalog.common.UserRole.PHARMACIST
    }
