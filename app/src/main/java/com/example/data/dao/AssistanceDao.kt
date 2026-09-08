package com.example.data.dao

import androidx.room.*
import com.example.data.model.AssistanceItemEntity
import com.example.data.model.MonthlyAssistanceEntity
import com.example.data.model.MonthlyAssistanceWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface AssistanceDao {
    @Transaction
    @Query("SELECT * FROM monthly_assistances WHERE year = :year AND month = :month ORDER BY scheduledDate ASC")
    fun getAssistancesForMonth(year: Int, month: Int): Flow<List<MonthlyAssistanceWithDetails>>

    @Transaction
    @Query("SELECT * FROM monthly_assistances WHERE year = :year AND month = :month ORDER BY scheduledDate ASC")
    suspend fun getAssistancesListForMonth(year: Int, month: Int): List<MonthlyAssistanceWithDetails>

    @Transaction
    @Query("SELECT * FROM monthly_assistances WHERE personId = :personId ORDER BY year DESC, month DESC")
    fun getAssistancesForPerson(personId: Long): Flow<List<MonthlyAssistanceWithDetails>>

    @Transaction
    @Query("SELECT * FROM monthly_assistances WHERE personId = :personId AND year = :year AND month = :month LIMIT 1")
    suspend fun getAssistanceForPersonAndMonth(personId: Long, year: Int, month: Int): MonthlyAssistanceWithDetails?

    @Transaction
    @Query("SELECT * FROM monthly_assistances WHERE personId = :personId AND year = :year AND month = :month LIMIT 1")
    fun observeAssistanceForPersonAndMonth(personId: Long, year: Int, month: Int): Flow<MonthlyAssistanceWithDetails?>

    @Transaction
    @Query("SELECT * FROM monthly_assistances WHERE id = :assistanceId")
    suspend fun getAssistanceById(assistanceId: Long): MonthlyAssistanceWithDetails?

    @Transaction
    @Query("SELECT * FROM monthly_assistances WHERE id = :assistanceId")
    fun observeAssistanceById(assistanceId: Long): Flow<MonthlyAssistanceWithDetails?>

    @Transaction
    @Query("SELECT * FROM monthly_assistances WHERE scheduledDate = :dateIso")
    fun getAssistancesForDate(dateIso: String): Flow<List<MonthlyAssistanceWithDetails>>

    @Transaction
    @Query("SELECT * FROM monthly_assistances WHERE scheduledDate >= :startDateIso AND scheduledDate <= :endDateIso ORDER BY scheduledDate ASC")
    fun getAssistancesBetweenDates(startDateIso: String, endDateIso: String): Flow<List<MonthlyAssistanceWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyAssistance(assistance: MonthlyAssistanceEntity): Long

    @Update
    suspend fun updateMonthlyAssistance(assistance: MonthlyAssistanceEntity)

    @Query("DELETE FROM monthly_assistances WHERE id = :assistanceId")
    suspend fun deleteAssistance(assistanceId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssistanceItems(items: List<AssistanceItemEntity>)

    @Query("DELETE FROM assistance_items WHERE assistanceId = :assistanceId")
    suspend fun clearAssistanceItems(assistanceId: Long)

    @Query("SELECT COUNT(*) FROM monthly_assistances WHERE year = :year AND month = :month")
    fun getAssistanceCountForMonth(year: Int, month: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM monthly_assistances WHERE year = :year AND month = :month AND status = 'DELIVERED'")
    fun getDeliveredCountForMonth(year: Int, month: Int): Flow<Int>

    @Query("SELECT SUM(totalAmount) FROM monthly_assistances WHERE year = :year AND month = :month")
    fun getTotalAmountForMonth(year: Int, month: Int): Flow<Double?>

    @Transaction
    @Query("SELECT * FROM monthly_assistances ORDER BY year DESC, month DESC")
    fun getAllAssistances(): Flow<List<MonthlyAssistanceWithDetails>>

    @Transaction
    @Query("SELECT * FROM monthly_assistances ORDER BY year DESC, month DESC")
    suspend fun getAllAssistancesList(): List<MonthlyAssistanceWithDetails>
}
