package com.example.time.data.model

import java.io.Serializable

/**
 * 表示一节课程的时间信息
 */
data class SectionTime(
    // 节次编号（第几节课）
    val section: Int,
    
    // 开始时间（格式：HH:mm）
    var startTime: String,
    
    // 结束时间（格式：HH:mm）
    var endTime: String
) : Serializable 