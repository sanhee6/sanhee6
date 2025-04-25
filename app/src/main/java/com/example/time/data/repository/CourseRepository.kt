package com.example.time.data.repository

import android.util.Log
import com.example.time.data.database.CourseDao
import com.example.time.data.model.Course
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CourseRepository(private val courseDao: CourseDao) {
    
    fun getAllCourses(tableId: Long): Flow<List<Course>> {
        return courseDao.getAllCourses(tableId)
    }
    
    suspend fun getAllCoursesSync(tableId: Long): List<Course> {
        return courseDao.getAllCoursesSync(tableId)
    }
    
    suspend fun getCourseById(id: Long): Course? {
        return courseDao.getCourseById(id)
    }
    
    suspend fun insertCourse(course: Course): Long {
        return courseDao.insertCourse(course)
    }
    
    suspend fun updateCourse(course: Course) {
        courseDao.updateCourse(course)
    }
    
    suspend fun deleteCourse(course: Course) {
        courseDao.deleteCourse(course)
    }
    
    fun getCoursesByDayAndWeek(tableId: Long, day: Int, week: Int): Flow<List<Course>> {
        Log.d("CourseRepository", "过滤课程 - 表ID: $tableId, 星期: $day, 周次: $week")
        return courseDao.getCoursesByDay(tableId, day).map { courses ->
            Log.d("CourseRepository", "获取到星期${day+1}的课程: ${courses.size}个")
            
            // 为每个课程添加详细信息记录
            courses.forEach { course ->
                Log.d("CourseRepository", "课程: ${course.name}, 周次列表: ${course.weekList}, 包含本周(${week})?: ${course.weekList.contains(week)}")
            }
            
            val filtered = courses.filter { course -> course.weekList.contains(week) }
            Log.d("CourseRepository", "过滤后第${week}周的课程: ${filtered.size}个")
            
            // 输出过滤后的课程名称列表
            if (filtered.isNotEmpty()) {
                Log.d("CourseRepository", "过滤后的课程: ${filtered.joinToString { it.name }}")
            }
            
            filtered
        }
    }
    
    fun getCoursesByDay(tableId: Long, day: Int): Flow<List<Course>> {
        return courseDao.getCoursesByDay(tableId, day)
    }
} 