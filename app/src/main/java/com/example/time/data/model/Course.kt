package com.example.time.data.model

import android.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

/**
 * 课程数据模型
 */
@Entity(tableName = "courses")
@TypeConverters(Converters::class)
data class Course(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String = "",                   // 课程名称
    var classroom: String = "",              // 教室位置
    var teacher: String = "",                // 教师姓名
    var weekList: List<Int> = listOf(),      // 课程周次列表
    var day: Int = 0,                        // 星期几，0表示周一
    var startSection: Int = 0,               // 开始节次
    var endSection: Int = 0,                 // 结束节次
    var color: Int = defaultColors[0],       // 课程颜色
    var note: String = "",                   // 备注信息
    var reminder: Boolean = true,            // 是否开启课前提醒
    var reminderMinutes: Int = 10,           // 提前提醒时间(分钟)
    var autoSilent: Boolean = false,         // 是否自动静音
    var tableId: Long = 0                    // 所属课表ID
) : Serializable {
    companion object {
        // 默认课程颜色列表
        val defaultColors = listOf(
            Color.parseColor("#90CAF9"),  // 蓝色
            Color.parseColor("#AED581"),  // 绿色
            Color.parseColor("#FFCC80"),  // 橙色
            Color.parseColor("#EF9A9A"),  // 红色
            Color.parseColor("#CE93D8"),  // 紫色
            Color.parseColor("#80DEEA")   // 青色
        )
    }
} 