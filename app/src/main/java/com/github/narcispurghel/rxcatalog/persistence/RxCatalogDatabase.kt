@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserEntity::class,
        MedicineEntity::class,
        SubmittedLeafletEntity::class,
        ApprovedLeafletEntity::class,
        ApprovalHistoryEntity::class,
        FavoriteEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(PersistenceConverters::class)
abstract class RxCatalogDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun medicineDao(): MedicineDao

    abstract fun submittedLeafletDao(): SubmittedLeafletDao

    abstract fun approvedLeafletDao(): ApprovedLeafletDao

    abstract fun approvalHistoryDao(): ApprovalHistoryDao

    abstract fun favoriteDao(): FavoriteDao
}
