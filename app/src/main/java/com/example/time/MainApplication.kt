package com.example.time

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setAppLocale(this, Locale.CHINA)
    }
    
    override fun attachBaseContext(base: Context) {
        // 用中文设置包装基础上下文
        val context = updateBaseContextLocale(base)
        super.attachBaseContext(context)
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setAppLocale(this, Locale.CHINA)
    }
    
    /**
     * 更新基础上下文的语言环境
     */
    private fun updateBaseContextLocale(context: Context): Context {
        val locale = Locale.CHINA
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }
    
    /**
     * 设置应用的语言环境
     */
    companion object {
        fun setAppLocale(context: Context, locale: Locale) {
            Locale.setDefault(locale)
            val resources = context.resources
            val config = Configuration(resources.configuration)
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }
} 