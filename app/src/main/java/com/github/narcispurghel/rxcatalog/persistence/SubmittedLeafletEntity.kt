@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.uuid.Uuid

@Entity(
    tableName = "submitted_leaflets",
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
            childColumns = ["submittedByUserId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["reviewedByUserId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["medicineId"]),
        Index(value = ["submittedByUserId"]),
        Index(value = ["reviewedByUserId"]),
        Index(value = ["status"]),
        Index(value = ["syncStatus"]),
    ],
)
data class SubmittedLeafletEntity(
    @PrimaryKey
    val submissionId: Uuid,
    val medicineId: Uuid,
    val submittedByUserId: Uuid,
    val reviewedByUserId: Uuid? = null,
    val title: String,
    val content: String,
    val status: SubmissionStatus,
    @ColumnInfo(defaultValue = "0")
    val isUrgent: Boolean = false,
    val rejectionReason: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val reviewedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)
