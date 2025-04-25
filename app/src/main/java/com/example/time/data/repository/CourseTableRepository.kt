package com.example.time.data.repository

import com.example.time.data.database.CourseTableDao
import com.example.time.data.model.CourseTable
import kotlinx.coroutines.flow.Flow

class CourseTableRepository(private val courseTableDao: CourseTableDao) {
    
    fun getAllCourseTables(): Flow<List<CourseTable>> {
        return courseTableDao.getAllCourseTables()
    }
    
    suspend fun getAllCourseTablesSync(): List<CourseTable> {
        return courseTableDao.getAllCourseTablesSync()
    }
    
    suspend fun getCourseTableById(id: Long): CourseTable? {
        return courseTableDao.getCourseTableById(id)
    }
    
    suspend fun getDefaultCourseTable(): CourseTable? {
        return courseTableDao.getDefaultCourseTable()
    }
    
    suspend fun insertCourseTable(courseTable: CourseTable): Long {
        return courseTableDao.insertCourseTable(courseTable)
    }
    
    suspend fun updateCourseTable(courseTable: CourseTable) {
        courseTableDao.updateCourseTable(courseTable)
    }
    
    suspend fun deleteCourseTable(courseTable: CourseTable) {
        courseTableDao.deleteCourseTable(courseTable)
    }
    
    suspend fun setDefaultCourseTable(id: Long) {
        courseTableDao.clearDefaultCourseTable()
        courseTableDao.setDefaultCourseTable(id)
    }
} 