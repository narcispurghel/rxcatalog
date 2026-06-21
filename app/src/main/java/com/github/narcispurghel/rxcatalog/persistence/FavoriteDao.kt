@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.persistence

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
interface FavoriteDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(favorite: FavoriteEntity)

	@Upsert
	suspend fun upsert(favorite: FavoriteEntity)

	@Upsert
	suspend fun upsertAll(favorites: List<FavoriteEntity>)

	@Query(
		"""
        select * from favorites
        where userId = :userId and medicineId = :medicineId
        limit 1
        """,
	)
	suspend fun getByUserAndMedicine(
		userId: Uuid,
		medicineId: Uuid,
	): FavoriteEntity?

	@Query(
		"""
        select * from favorites
        where userId = :userId and medicineId = :medicineId
        limit 1
        """,
	)
	fun observeByUserAndMedicine(
		userId: Uuid,
		medicineId: Uuid,
	): Flow<FavoriteEntity?>

	@Query(
		"""
        select * from favorites
        where userId = :userId
        order by createdAt desc
        """,
	)
	fun observeByUserId(userId: Uuid): Flow<List<FavoriteEntity>>

	@Query(
		"""
        select medicines.* from medicines
        inner join favorites on favorites.medicineId = medicines.medicineId
        where favorites.userId = :userId
        order by favorites.createdAt desc, medicines.canonicalName collate nocase asc
        """,
	)
	fun observeFavoriteMedicinesByUserId(userId: Uuid): Flow<List<MedicineEntity>>

	@Query(
		"""
        select * from favorites
        where medicineId = :medicineId
        order by createdAt desc
        """,
	)
	fun observeByMedicineId(medicineId: Uuid): Flow<List<FavoriteEntity>>

	@Query(
		"""
        select count(*) from favorites
        where userId = :userId
        """,
	)
	fun observeCountForUser(userId: Uuid): Flow<Int>

	@Query(
		"""
        select exists(
            select 1 from favorites
            where userId = :userId and medicineId = :medicineId
        )
        """,
	)
	fun observeIsFavorite(
		userId: Uuid,
		medicineId: Uuid,
	): Flow<Boolean>

	@Query(
		"""
        select * from favorites
        where syncStatus = :syncStatus
        order by createdAt desc
        """,
	)
	fun observeBySyncStatus(syncStatus: SyncStatus): Flow<List<FavoriteEntity>>

	@Query(
		"""
        update favorites
        set createdAt = :createdAt,
            syncStatus = :syncStatus
        where userId = :userId and medicineId = :medicineId
        """,
	)
	suspend fun update(
		userId: Uuid,
		medicineId: Uuid,
		createdAt: Long,
		syncStatus: SyncStatus,
	)

	@Delete
	suspend fun delete(favorite: FavoriteEntity)
}
