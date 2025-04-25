package com.example.time.data.repository

import com.example.time.data.database.TimeTableDao
import com.example.time.data.model.TimeTable
import kotlinx.coroutines.flow.Flow

class TimeTableRepository(private val timeTableDao: TimeTableDao) {
    
    fun getAllTimeTables(): Flow<List<TimeTable>> {
        return timeTableDao.getAllTimeTables()
    }
    
    suspend fun getAllTimeTablesSync(): List<TimeTable> {
        return timeTableDao.getAllTimeTablesSync()
    }
    
    suspend fun getTimeTableById(id: Long): TimeTable? {
        return timeTableDao.getTimeTableById(id)
    }
    
    suspend fun getDefaultTimeTable(): TimeTable? {
        return timeTableDao.getDefaultTimeTable()
    }
    
    suspend fun insertTimeTable(timeTable: TimeTable): Long {
        return timeTableDao.insertTimeTable(timeTable)
    }
    
    suspend fun updateTimeTable(timeTable: TimeTable) {
        timeTableDao.updateTimeTable(timeTable)
    }
    
    suspend fun deleteTimeTable(timeTable: TimeTable) {
        timeTableDao.deleteTimeTable(timeTable)
    }
    
    suspend fun setDefaultTimeTable(id: Long) {
        timeTableDao.clearDefaultTimeTable()
        timeTableDao.setDefaultTimeTable(id)
    }
} 