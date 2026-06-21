package com.github.narcispurghel.rxcatalog.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import com.github.narcispurghel.rxcatalog.persistence.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import androidx.sqlite.db.SupportSQLiteDatabase

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
	@Provides
	@Singleton
	fun provideRxCatalogDatabase(
		@ApplicationContext context: Context,
	): RxCatalogDatabase =
		Room
			.databaseBuilder(
				context,
				RxCatalogDatabase::class.java,
				"rxcatalog.db",
			).addMigrations(MIGRATION_1_2)
			.build()

	private val MIGRATION_1_2 =
		object : Migration(1, 2) {
			override fun migrate(db: SupportSQLiteDatabase) {
				db.execSQL(
					"alter table submitted_leaflets add column isUrgent INTEGER not null default 0",
				)
			}
		}

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
