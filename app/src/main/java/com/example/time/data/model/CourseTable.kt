package com.example.time.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 课表模型
 */
@Entity(tableName = "course_tables")
data class CourseTable(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String = "",          // 课表名称
    var semester: String = "",      // 学期
    var timeTableId: Long = 0,      // 使用的时间表ID
    var background: String = "",    // 背景图片路径
    var isDefault: Boolean = false  // 是否为默认课表
) 