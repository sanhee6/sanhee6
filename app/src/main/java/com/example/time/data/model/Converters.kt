package com.example.time.data.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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