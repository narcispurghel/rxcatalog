@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
interface MedicineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicine: MedicineEntity)

    @Upsert
    suspend fun upsert(medicine: MedicineEntity)

    @Upsert
    suspend fun upsertAll(medicines: List<MedicineEntity>)

    @Query("select * from medicines where medicineId = :medicineId limit 1")
    suspend fun getById(medicineId: Uuid): MedicineEntity?

    @Query("select * from medicines where medicineId = :medicineId limit 1")
    fun observeById(medicineId: Uuid): Flow<MedicineEntity?>

    @Query("select * from medicines order by canonicalName collate nocase asc")
    fun observeAll(): Flow<List<MedicineEntity>>

    @Query(
        """
        select * from medicines
        where lower(canonicalName) like '%' || lower(:query) || '%'
           or lower(coalesce(brandName, '')) like '%' || lower(:query) || '%'
           or lower(coalesce(activeIngredient, '')) like '%' || lower(:query) || '%'
           or lower(coalesce(atcCode, '')) like '%' || lower(:query) || '%'
        order by canonicalName collate nocase asc
        """
    )
    fun search(query: String): Flow<List<MedicineEntity>>

    @Query(
        """
        select * from medicines
        where syncStatus = :syncStatus
        order by updatedAt desc
        """
    )
    fun observeBySyncStatus(syncStatus: SyncStatus): Flow<List<MedicineEntity>>

    @Query(
        """
        update medicines
        set canonicalName = :canonicalName,
            brandName = :brandName,
            activeIngredient = :activeIngredient,
            atcCode = :atcCode,
            description = :description,
            updatedAt = :updatedAt,
            syncStatus = :syncStatus
        where medicineId = :medicineId
        """
    )
    suspend fun updateDetails(
        medicineId: Uuid,
        canonicalName: String,
        brandName: String?,
        activeIngredient: String?,
        atcCode: String?,
        description: String?,
        updatedAt: Long,
        syncStatus: SyncStatus,
    )

    @Delete
    suspend fun delete(medicine: MedicineEntity)
}
