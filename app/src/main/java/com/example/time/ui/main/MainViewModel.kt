package com.example.time.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.time.data.database.AppDatabase
import com.example.time.data.model.Course
import com.example.time.data.model.CourseTable
import com.example.time.data.model.TimeTable
import com.example.time.data.repository.CourseRepository
import com.example.time.data.repository.CourseTableRepository
import com.example.time.data.repository.TimeTableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val courseRepository = CourseRepository(database.courseDao())
    private val timeTableRepository = TimeTableRepository(database.timeTableDao())
    private val courseTableRepository = CourseTableRepository(database.courseTableDao())
    
    // 获取所有课程
    fun getAllCourses(tableId: Long): Flow<List<Course>> {
        return courseRepository.getAllCourses(tableId)
    }
    
    // 获取特定日期的课程
    fun getCoursesByDay(tableId: Long, day: Int): Flow<List<Course>> {
        return courseRepository.getCoursesByDay(tableId, day)
    }
    
    // 获取特定日期和周次的课程
    fun getCoursesByDayAndWeek(tableId: Long, day: Int, week: Int): Flow<List<Course>> {
        return courseRepository.getCoursesByDayAndWeek(tableId, day, week)
    }
    
    // 添加课程
    suspend fun addCourse(course: Course): Long {
        return courseRepository.insertCourse(course)
    }
    
    // 更新课程
    suspend fun updateCourse(course: Course) {
        courseRepository.updateCourse(course)
    }
    
    // 删除课程
    suspend fun deleteCourse(course: Course) {
        courseRepository.deleteCourse(course)
    }
    
    // 获取默认时间表
    suspend fun getDefaultTimeTable(): TimeTable? {
        return timeTableRepository.getDefaultTimeTable()
    }
    
    // 获取默认课表
    suspend fun getDefaultCourseTable(): CourseTable? {
        return courseTableRepository.getDefaultCourseTable()
    }
    
    // 初始化应用数据（第一次使用应用时调用）
    fun initAppData() {
        viewModelScope.launch {
            // 检查是否已有数据
            val timeTables = timeTableRepository.getAllTimeTablesSync()
            if (timeTables.isEmpty()) {
                // 创建默认时间表
                val defaultTimeTable = TimeTable(
                    name = "默认时间表",
                    isDefault = true
                )
                val timeTableId = timeTableRepository.insertTimeTable(defaultTimeTable)
                
                // 创建默认课表
                val defaultCourseTable = CourseTable(
                    name = "默认课表",
                    semester = "2023-2024学年第一学期",
                    timeTableId = timeTableId,
                    isDefault = true
                )
                courseTableRepository.insertCourseTable(defaultCourseTable)
            }
        }
    }
} 