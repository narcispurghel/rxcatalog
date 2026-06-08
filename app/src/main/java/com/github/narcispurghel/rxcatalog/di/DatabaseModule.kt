package com.github.narcispurghel.rxcatalog.di

import android.content.Context
import androidx.room.Room
import com.github.narcispurghel.rxcatalog.persistence.ApprovalHistoryDao
import com.github.narcispurghel.rxcatalog.persistence.ApprovedLeafletDao
import com.github.narcispurghel.rxcatalog.persistence.FavoriteDao
import com.github.narcispurghel.rxcatalog.persistence.MedicineDao
import com.github.narcispurghel.rxcatalog.persistence.RxCatalogDatabase
import com.github.narcispurghel.rxcatalog.persistence.SubmittedLeafletDao
import com.github.narcispurghel.rxcatalog.persistence.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideRxCatalogDatabase(
        @ApplicationContext context: Context,
    ): RxCatalogDatabase =
        Room.databaseBuilder(
            context,
            RxCatalogDatabase::class.java,
            "rxcatalog.db",
        ).build()

    @Provides
    fun provideUserDao(database: RxCatalogDatabase): UserDao = database.userDao()

    @Provides
    fun provideMedicineDao(database: RxCatalogDatabase): MedicineDao = database.medicineDao()

    @Provides
    fun provideSubmittedLeafletDao(database: RxCatalogDatabase): SubmittedLeafletDao =
        database.submittedLeafletDao()

    @Provides
    fun provideApprovedLeafletDao(database: RxCatalogDatabase): ApprovedLeafletDao =
        database.approvedLeafletDao()

    @Provides
    fun provideApprovalHistoryDao(database: RxCatalogDatabase): ApprovalHistoryDao =
        database.approvalHistoryDao()

    @Provides
    fun provideFavoriteDao(database: RxCatalogDatabase): FavoriteDao = database.favoriteDao()
}
