@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.persistence

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.uuid.Uuid

@Entity(
    tableName = "medicines",
    indices = [
        Index(value = ["canonicalName"]),
        Index(value = ["atcCode"]),
        Index(value = ["syncStatus"]),
    ],
)
data class MedicineEntity(
    @PrimaryKey
    val medicineId: Uuid,
    val canonicalName: String,
    val brandName: String? = null,
    val activeIngredient: String? = null,
    val atcCode: String? = null,
    val description: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)
