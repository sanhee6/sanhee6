package com.example.timetable.model

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

/**
 * 时间表模型，用于定义上课时间
 */
@Entity(tableName = "timetables")
data class TimeTable(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String = "",                      // 时间表名称
    var sectionTimes: List<SectionTime> = defaultSectionTimes,  // 节次对应的时间
    var isDefault: Boolean = false              // 是否为默认时间表
) {
    companion object {
        // 默认的节次时间安排
        val defaultSectionTimes = listOf(
            SectionTime(1, "8:00", "8:45"),
            SectionTime(2, "8:55", "9:40"),
            SectionTime(3, "10:00", "10:45"),
            SectionTime(4, "10:55", "11:40"),
            SectionTime(5, "13:30", "14:15"),
            SectionTime(6, "14:25", "15:10"),
            SectionTime(7, "15:30", "16:15"),
            SectionTime(8, "16:25", "17:10"),
            SectionTime(9, "18:30", "19:15"),
            SectionTime(10, "19:25", "20:10")
        )
    }
}

/**
 * 节次时间模型
 */
data class SectionTime(
    val section: Int,     // 第几节课
    val startTime: String, // 开始时间
    val endTime: String   // 结束时间
) : Serializable

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

/**
 * 类型转换器，用于Room数据库
 */
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromSectionTimeList(value: List<SectionTime>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toSectionTimeList(value: String): List<SectionTime> {
        val listType = object : TypeToken<List<SectionTime>>() {}.type
        return gson.fromJson(value, listType)
    }
}

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