@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.persistence

import androidx.room.TypeConverter
import kotlin.uuid.Uuid

class PersistenceConverters {
    @TypeConverter
    fun fromUuid(value: Uuid?): ByteArray? = value?.toByteArray()

    @TypeConverter
    fun toUuid(value: ByteArray?): Uuid? = value?.let(Uuid::fromByteArray)

    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.storageValue

    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.fromStorageValue(value)

    @TypeConverter
    fun fromSubmissionStatus(value: SubmissionStatus): String = value.storageValue

    @TypeConverter
    fun toSubmissionStatus(value: String): SubmissionStatus = SubmissionStatus.fromStorageValue(value)

    @TypeConverter
    fun fromApprovalAction(value: ApprovalAction): String = value.storageValue

    @TypeConverter
    fun toApprovalAction(value: String): ApprovalAction = ApprovalAction.fromStorageValue(value)

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.storageValue

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.fromStorageValue(value)
}
