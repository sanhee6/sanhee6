package com.example.time.data.database

import androidx.room.*
import com.example.time.data.model.CourseTable
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseTableDao {
    @Query("SELECT * FROM course_tables")
    fun getAllCourseTables(): Flow<List<CourseTable>>
    
    @Query("SELECT * FROM course_tables")
    suspend fun getAllCourseTablesSync(): List<CourseTable>
    
    @Query("SELECT * FROM course_tables WHERE id = :id")
    suspend fun getCourseTableById(id: Long): CourseTable?
    
    @Query("SELECT * FROM course_tables WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultCourseTable(): CourseTable?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseTable(courseTable: CourseTable): Long
    
    @Update
    suspend fun updateCourseTable(courseTable: CourseTable)
    
    @Delete
    suspend fun deleteCourseTable(courseTable: CourseTable)
    
    @Query("UPDATE course_tables SET isDefault = 0")
    suspend fun clearDefaultCourseTable()
    
    @Query("UPDATE course_tables SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultCourseTable(id: Long)
} 