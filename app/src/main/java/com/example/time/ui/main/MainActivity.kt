package com.example.time.ui.main

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.time.MainApplication
import com.example.time.R
import com.example.time.databinding.ActivityMainBinding
import com.example.time.ui.course.EditCourseActivity
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var navController: NavController
    
    // 当前选择的课表ID
    private var currentTableId: Long = 1
    
    override fun attachBaseContext(newBase: Context) {
        // 创建配置了中文的上下文
        val context = updateBaseContextLocale(newBase)
        super.attachBaseContext(context)
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        MainApplication.setAppLocale(this, Locale.CHINA)
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置应用使用中文
        MainApplication.setAppLocale(this, Locale.CHINA)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 设置状态栏
        window.statusBarColor = getColor(R.color.primary)
        
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        // 初始化应用数据
        viewModel.initAppData()
        
        setupAddCourseButton()
        loadDefaultCourseTable()
    }
    
    override fun onStart() {
        super.onStart()
        setupNavigation()
    }
    
    private fun setupNavigation() {
        // 使用NavHostFragment方式获取NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
    }
    
    private fun setupAddCourseButton() {
        // 设置添加课程按钮
        binding.fabAddCourse.setOnClickListener {
            // 打开添加课程页面
            EditCourseActivity.start(this, currentTableId)
        }
    }
    
    private fun loadDefaultCourseTable() {
        // 获取默认课表ID
        lifecycleScope.launch {
            viewModel.getDefaultCourseTable()?.let { courseTable ->
                currentTableId = courseTable.id
            }
        }
    }
} 