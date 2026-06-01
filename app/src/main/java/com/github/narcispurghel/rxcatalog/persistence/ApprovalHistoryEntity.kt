@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.persistence

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.uuid.Uuid

@Entity(
    tableName = "approval_history",
    foreignKeys = [
        ForeignKey(
            entity = SubmittedLeafletEntity::class,
            parentColumns = ["submissionId"],
            childColumns = ["submissionId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["reviewerUserId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["submissionId"]),
        Index(value = ["reviewerUserId"]),
        Index(value = ["action"]),
        Index(value = ["syncStatus"]),
    ],
)
data class ApprovalHistoryEntity(
    @PrimaryKey
    val approvalHistoryId: Uuid,
    val submissionId: Uuid,
    val reviewerUserId: Uuid? = null,
    val action: ApprovalAction,
    val notes: String? = null,
    val actedAt: Long,
    val createdAt: Long,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)
