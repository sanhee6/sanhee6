package com.example.time.data.database

import androidx.room.TypeConverter
import com.example.time.data.model.SectionTime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room数据库的类型转换器
 */
class Converters {
    private val gson = Gson()

    /**
     * 将List<SectionTime>转换为JSON字符串存储到数据库
     */
    @TypeConverter
    fun fromSectionTimeList(sectionTimes: List<SectionTime>): String {
        return gson.toJson(sectionTimes)
    }

    /**
     * 将JSON字符串从数据库转换回List<SectionTime>对象
     */
    @TypeConverter
    fun toSectionTimeList(sectionTimesString: String): List<SectionTime> {
        val listType = object : TypeToken<List<SectionTime>>() {}.type
        return gson.fromJson(sectionTimesString, listType)
    }
} 