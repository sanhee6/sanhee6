package com.example.time.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

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