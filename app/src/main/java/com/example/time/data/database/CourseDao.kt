package com.example.time.data.database

import androidx.room.*
import com.example.time.data.model.Course
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses WHERE tableId = :tableId")
    fun getAllCourses(tableId: Long): Flow<List<Course>>
    
    @Query("SELECT * FROM courses WHERE tableId = :tableId")
    suspend fun getAllCoursesSync(tableId: Long): List<Course>
    
    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: Long): Course?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course): Long
    
    @Update
    suspend fun updateCourse(course: Course)
    
    @Delete
    suspend fun deleteCourse(course: Course)
    
    @Query("SELECT * FROM courses WHERE tableId = :tableId AND day = :day")
    fun getCoursesByDay(tableId: Long, day: Int): Flow<List<Course>>
    
    fun getCoursesByDayAndWeek(tableId: Long, day: Int, week: Int): Flow<List<Course>> {
        return getCoursesByDay(tableId, day).map { courses ->
            courses.filter { course -> course.weekList.contains(week) }
        }
    }
} 