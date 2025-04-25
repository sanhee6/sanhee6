package com.example.time.ui.course

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.time.data.database.AppDatabase
import com.example.time.data.model.Course
import com.example.time.data.repository.CourseRepository

class CourseViewModel(application: Application) : AndroidViewModel(application) {
    
    private val courseRepository: CourseRepository
    
    init {
        val courseDao = AppDatabase.getDatabase(application).courseDao()
        courseRepository = CourseRepository(courseDao)
    }
    
    suspend fun getCourseById(id: Long): Course? {
        return courseRepository.getCourseById(id)
    }
    
    suspend fun addCourse(course: Course): Long {
        return courseRepository.insertCourse(course)
    }
    
    suspend fun updateCourse(course: Course) {
        courseRepository.updateCourse(course)
    }
    
    suspend fun deleteCourse(course: Course) {
        courseRepository.deleteCourse(course)
    }
} 