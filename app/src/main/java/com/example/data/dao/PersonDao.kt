package com.example.data.dao

import androidx.room.*
import com.example.data.model.PersonEntity
import com.example.data.model.PersonStandardPackageItemEntity
import com.example.data.model.PersonWithPackage
import com.example.data.model.StandardPackageItemWithProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM persons WHERE isActive = 1 ORDER BY name ASC")
    fun getAllPersons(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM persons WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getAllPersonsList(): List<PersonEntity>

    @Query("SELECT * FROM persons WHERE isActive = 1 AND name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchPersons(query: String): Flow<List<PersonEntity>>

    @Query("SELECT * FROM persons WHERE id = :id")
    suspend fun getPersonById(id: Long): PersonEntity?

    @Query("SELECT * FROM persons WHERE id = :id")
    fun observePersonById(id: Long): Flow<PersonEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity): Long

    @Update
    suspend fun updatePerson(person: PersonEntity)

    @Delete
    suspend fun deletePerson(person: PersonEntity)

    @Query("DELETE FROM person_standard_packages WHERE personId = :personId")
    suspend fun clearStandardPackageForPerson(personId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStandardPackageItems(items: List<PersonStandardPackageItemEntity>)

    @Transaction
    @Query("SELECT * FROM person_standard_packages WHERE personId = :personId")
    fun getStandardPackageWithProducts(personId: Long): Flow<List<StandardPackageItemWithProduct>>

    @Transaction
    @Query("SELECT * FROM person_standard_packages WHERE personId = :personId")
    suspend fun getStandardPackageListWithProducts(personId: Long): List<StandardPackageItemWithProduct>

    @Query("SELECT COUNT(*) FROM persons WHERE isActive = 1")
    fun getPersonCount(): Flow<Int>
}
