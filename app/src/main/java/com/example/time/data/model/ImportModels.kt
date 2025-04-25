package com.example.time.data.model

/**
 * 课程响应类，用于从教务系统导入课表
 */
data class CourseImportResponse(
    val status: Int,
    val message: String,
    val data: List<ImportedCourse>?
)

/**
 * 导入的课程数据结构
 */
data class ImportedCourse(
    val name: String,
    val classroom: String,
    val teacher: String,
    val weekList: List<Int>,
    val day: Int,
    val startSection: Int,
    val endSection: Int
) 