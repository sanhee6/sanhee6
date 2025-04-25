package com.example.time.data.database

import androidx.room.*
import com.example.time.data.model.TimeTable
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeTableDao {
    @Query("SELECT * FROM timetables")
    fun getAllTimeTables(): Flow<List<TimeTable>>
    
    @Query("SELECT * FROM timetables")
    suspend fun getAllTimeTablesSync(): List<TimeTable>
    
    @Query("SELECT * FROM timetables WHERE id = :id")
    suspend fun getTimeTableById(id: Long): TimeTable?
    
    @Query("SELECT * FROM timetables WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultTimeTable(): TimeTable?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeTable(timeTable: TimeTable): Long
    
    @Update
    suspend fun updateTimeTable(timeTable: TimeTable)
    
    @Delete
    suspend fun deleteTimeTable(timeTable: TimeTable)
    
    @Query("UPDATE timetables SET isDefault = 0")
    suspend fun clearDefaultTimeTable()
    
    @Query("UPDATE timetables SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultTimeTable(id: Long)
} 