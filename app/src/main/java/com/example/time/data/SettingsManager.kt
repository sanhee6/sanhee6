package com.example.time.data

import android.content.Context
import android.content.SharedPreferences
import com.example.time.data.model.AppSettings
import com.example.time.data.model.SectionTime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 应用设置管理器，负责保存和读取应用设置
 */
class SettingsManager(private val context: Context) {
    companion object {
        private const val PREF_NAME = "app_settings"
        private const val KEY_TERM_START_DATE = "term_start_date"
        private const val KEY_TOTAL_WEEKS = "total_weeks"
        private const val KEY_SECTIONS_PER_DAY = "sections_per_day"
        private const val KEY_SECTION_TIMES = "section_times"
        
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    }
    
    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    private val gson = Gson()
    
    /**
     * 保存设置
     */
    fun saveSettings(settings: AppSettings) {
        preferences.edit().apply {
            // 保存学期开始日期
            putString(KEY_TERM_START_DATE, DATE_FORMAT.format(settings.termStartDate.time))
            
            // 保存总周数
            putInt(KEY_TOTAL_WEEKS, settings.totalWeeks)
            
            // 保存每天课程节数
            putInt(KEY_SECTIONS_PER_DAY, settings.sectionsPerDay)
            
            // 保存课程时间表（转为JSON）
            putString(KEY_SECTION_TIMES, gson.toJson(settings.sectionTimes))
            
            apply()
        }
    }
    
    /**
     * 读取设置
     */
    fun loadSettings(): AppSettings {
        val settings = AppSettings()
        
        // 读取学期开始日期
        preferences.getString(KEY_TERM_START_DATE, null)?.let { dateString ->
            try {
                val date = DATE_FORMAT.parse(dateString)
                date?.let {
                    settings.termStartDate.time = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // 读取总周数
        settings.totalWeeks = preferences.getInt(KEY_TOTAL_WEEKS, settings.totalWeeks)
        
        // 读取每天课程节数
        settings.sectionsPerDay = preferences.getInt(KEY_SECTIONS_PER_DAY, settings.sectionsPerDay)
        
        // 读取课程时间表
        preferences.getString(KEY_SECTION_TIMES, null)?.let { json ->
            try {
                val type = object : TypeToken<List<SectionTime>>() {}.type
                val sectionTimes = gson.fromJson<List<SectionTime>>(json, type)
                settings.sectionTimes = sectionTimes
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return settings
    }
} 