@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.persistence

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.uuid.Uuid

@Entity(
    tableName = "approved_leaflets",
    foreignKeys = [
        ForeignKey(
            entity = MedicineEntity::class,
            parentColumns = ["medicineId"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["approvedByUserId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SubmittedLeafletEntity::class,
            parentColumns = ["submissionId"],
            childColumns = ["sourceSubmissionId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["medicineId"], unique = true),
        Index(value = ["approvedByUserId"]),
        Index(value = ["sourceSubmissionId"]),
        Index(value = ["syncStatus"]),
    ],
)
data class ApprovedLeafletEntity(
    @PrimaryKey
    val leafletId: Uuid,
    val medicineId: Uuid,
    val sourceSubmissionId: Uuid? = null,
    val approvedByUserId: Uuid? = null,
    val title: String,
    val content: String,
    val version: Int,
    val approvedAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)
