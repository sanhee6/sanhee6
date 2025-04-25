package com.example.time.data.model

import java.util.Calendar
import java.util.Date

/**
 * 应用全局设置数据模型
 */
data class AppSettings(
    // 学期开始日期（第一周的第一天）
    var termStartDate: Calendar = Calendar.getInstance().apply {
        set(Calendar.MONTH, Calendar.SEPTEMBER)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    },
    
    // 总周数
    var totalWeeks: Int = 18,
    
    // 每天课程节数
    var sectionsPerDay: Int = 12,
    
    // 课程时间表
    var sectionTimes: List<SectionTime> = generateDefaultSectionTimes()
) {
    companion object {
        /**
         * 生成默认的课程时间表
         */
        fun generateDefaultSectionTimes(): List<SectionTime> {
            val defaultTimes = mutableListOf<SectionTime>()
            
            // 上午4节课
            defaultTimes.add(SectionTime(1, "08:00", "08:45"))
            defaultTimes.add(SectionTime(2, "08:55", "09:40"))
            defaultTimes.add(SectionTime(3, "10:00", "10:45"))
            defaultTimes.add(SectionTime(4, "10:55", "11:40"))
            
            // 下午4节课
            defaultTimes.add(SectionTime(5, "13:30", "14:15"))
            defaultTimes.add(SectionTime(6, "14:25", "15:10"))
            defaultTimes.add(SectionTime(7, "15:30", "16:15"))
            defaultTimes.add(SectionTime(8, "16:25", "17:10"))
            
            // 晚上4节课
            defaultTimes.add(SectionTime(9, "18:30", "19:15"))
            defaultTimes.add(SectionTime(10, "19:25", "20:10"))
            defaultTimes.add(SectionTime(11, "20:20", "21:05"))
            defaultTimes.add(SectionTime(12, "21:15", "22:00"))
            
            return defaultTimes
        }
    }
} 