package com.example.time.ui.course

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.time.MainApplication
import com.example.time.R
import com.example.time.data.model.Course
import com.example.time.databinding.ActivityEditCourseBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import java.util.Locale

class EditCourseActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityEditCourseBinding
    private lateinit var viewModel: CourseViewModel
    
    private var courseId: Long = 0
    private var tableId: Long = 1
    private var isEditing = false
    
    // 星期名称数组，与Course类中day字段对应：0表示周一，1表示周二，以此类推
    private val dayNames = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    private val sectionNumbers = (1..10).map { it.toString() }.toTypedArray()
    
    private val weekNumbers = mutableListOf<Int>()
    private var selectedColor = Course.defaultColors[0]
    
    override fun attachBaseContext(newBase: Context) {
        // 创建配置了中文的上下文
        val locale = Locale.CHINA
        Locale.setDefault(locale)
        
        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        val context = newBase.createConfigurationContext(configuration)
        
        super.attachBaseContext(context)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置应用使用中文
        MainApplication.setAppLocale(this, Locale.CHINA)
        
        binding = ActivityEditCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[CourseViewModel::class.java]
        
        // 获取传递的参数
        courseId = intent.getLongExtra(EXTRA_COURSE_ID, 0)
        tableId = intent.getLongExtra(EXTRA_TABLE_ID, 1)
        isEditing = courseId > 0
        
        setupUI()
        
        if (isEditing) {
            // 加载课程数据
            loadCourseData()
            // 显示删除按钮
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            // 新增课程时设置默认值
            binding.spinnerDay.setText(dayNames[0], false)
            binding.spinnerStartSection.setText("1", false)
            binding.spinnerEndSection.setText("2", false)
        }
    }
    
    private fun setupUI() {
        // 设置天数下拉菜单
        val dayAdapter = ArrayAdapter(this, R.layout.dropdown_item, dayNames)
        binding.spinnerDay.setAdapter(dayAdapter)
        
        // 设置节次下拉菜单
        val sectionAdapter = ArrayAdapter(this, R.layout.dropdown_item, sectionNumbers)
        binding.spinnerStartSection.setAdapter(sectionAdapter)
        binding.spinnerEndSection.setAdapter(sectionAdapter)
        
        // 设置周次选择
        setupWeekChips()
        
        // 设置颜色选择
        setupColorChips()
        
        // 设置提醒时间拖动条
        binding.seekbarReminderTime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateReminderTimeText(progress)
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // 更新初始提醒时间文本
        updateReminderTimeText(binding.seekbarReminderTime.progress)
        
        // 设置周次选择按钮
        binding.btnSelectAllWeeks.setOnClickListener { selectWeeks(ALL) }
        binding.btnSelectOddWeeks.setOnClickListener { selectWeeks(ODD) }
        binding.btnSelectEvenWeeks.setOnClickListener { selectWeeks(EVEN) }
        
        // 设置保存按钮
        binding.btnSave.setOnClickListener { saveCourse() }
        
        // 设置取消按钮
        binding.btnCancel.setOnClickListener { finish() }
        
        // 设置删除按钮
        binding.btnDelete.setOnClickListener { confirmDelete() }
    }
    
    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("删除课程")
            .setMessage("确定要删除这个课程吗？")
            .setPositiveButton("删除") { _, _ -> deleteCourse() }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun deleteCourse() {
        lifecycleScope.launch {
            viewModel.getCourseById(courseId)?.let { course ->
                viewModel.deleteCourse(course)
                finish()
            }
        }
    }
    
    private fun setupWeekChips() {
        binding.chipGroupWeeks.removeAllViews()
        
        for (i in 1..20) {
            val chip = Chip(this)
            chip.text = i.toString()
            chip.isCheckable = true
            
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!weekNumbers.contains(i)) {
                        weekNumbers.add(i)
                    }
                } else {
                    weekNumbers.remove(i)
                }
            }
            
            binding.chipGroupWeeks.addView(chip)
        }
    }
    
    private fun setupColorChips() {
        binding.chipGroupColors.removeAllViews()
        
        Course.defaultColors.forEachIndexed { index, color ->
            val chip = Chip(this)
            chip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(color)
            chip.isCheckable = true
            
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedColor = color
                    
                    // 取消选中其他颜色
                    for (i in 0 until binding.chipGroupColors.childCount) {
                        val otherChip = binding.chipGroupColors.getChildAt(i) as Chip
                        if (otherChip != chip) {
                            otherChip.isChecked = false
                        }
                    }
                }
            }
            
            // 默认选中第一个颜色
            if (index == 0) {
                chip.isChecked = true
            }
            
            binding.chipGroupColors.addView(chip)
        }
    }
    
    private fun updateReminderTimeText(minutes: Int) {
        binding.tvReminderTime.text = getString(R.string.reminder_time, minutes)
    }
    
    private fun selectWeeks(type: Int) {
        for (i in 0 until binding.chipGroupWeeks.childCount) {
            val chip = binding.chipGroupWeeks.getChildAt(i) as Chip
            val week = chip.text.toString().toInt()
            
            when (type) {
                ALL -> chip.isChecked = true
                ODD -> chip.isChecked = week % 2 != 0
                EVEN -> chip.isChecked = week % 2 == 0
            }
        }
    }
    
    private fun loadCourseData() {
        lifecycleScope.launch {
            val course = viewModel.getCourseById(courseId)
            course?.let {
                // 填充表单数据
                binding.etCourseName.setText(it.name)
                binding.etClassroom.setText(it.classroom)
                binding.etTeacher.setText(it.teacher)
                
                try {
                    // 设置星期几
                    if (it.day >= 0 && it.day < dayNames.size) {
                        binding.spinnerDay.setText(dayNames[it.day], false)
                    }
                    
                    // 设置节次
                    binding.spinnerStartSection.setText(it.startSection.toString(), false)
                    binding.spinnerEndSection.setText(it.endSection.toString(), false)
                } catch (e: Exception) {
                    Log.e("EditCourseActivity", "Error setting dropdown values", e)
                }
                
                binding.etNote.setText(it.note)
                binding.switchReminder.isChecked = it.reminder
                binding.seekbarReminderTime.progress = it.reminderMinutes
                binding.switchAutoSilent.isChecked = it.autoSilent
                
                // 设置周次
                weekNumbers.clear()
                weekNumbers.addAll(it.weekList)
                
                for (i in 0 until binding.chipGroupWeeks.childCount) {
                    val chip = binding.chipGroupWeeks.getChildAt(i) as Chip
                    val week = chip.text.toString().toInt()
                    chip.isChecked = it.weekList.contains(week)
                }
                
                // 设置颜色
                for (i in 0 until binding.chipGroupColors.childCount) {
                    val chip = binding.chipGroupColors.getChildAt(i) as Chip
                    val color = chip.chipBackgroundColor?.defaultColor
                    if (color == it.color) {
                        chip.isChecked = true
                        selectedColor = it.color
                    }
                }
                
                updateReminderTimeText(it.reminderMinutes)
            }
        }
    }
    
    private fun saveCourse() {
        val name = binding.etCourseName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, R.string.error_course_name_empty, Toast.LENGTH_SHORT).show()
            return
        }
        
        if (weekNumbers.isEmpty()) {
            Toast.makeText(this, R.string.error_week_empty, Toast.LENGTH_SHORT).show()
            return
        }
        
        val dayText = binding.spinnerDay.text.toString()
        val dayIndex = dayNames.indexOf(dayText)
        if (dayIndex == -1) {
            Toast.makeText(this, "请选择星期", Toast.LENGTH_SHORT).show()
            return
        }
        
        val startSectionText = binding.spinnerStartSection.text.toString()
        val endSectionText = binding.spinnerEndSection.text.toString()
        
        val startSection = startSectionText.toIntOrNull() ?: 0
        val endSection = endSectionText.toIntOrNull() ?: 0
        
        if (startSection == 0 || endSection == 0 || startSection > endSection) {
            Toast.makeText(this, R.string.error_section_invalid, Toast.LENGTH_SHORT).show()
            return
        }
        
        // 输出调试信息
        Log.d("EditCourseActivity", "保存课程: 星期=$dayText(index=$dayIndex), 开始节次=$startSection, 结束节次=$endSection")
        
        val course = Course(
            id = if (isEditing) courseId else 0,
            name = name,
            classroom = binding.etClassroom.text.toString().trim(),
            teacher = binding.etTeacher.text.toString().trim(),
            weekList = weekNumbers.sorted(),
            day = dayIndex,
            startSection = startSection,
            endSection = endSection,
            color = selectedColor,
            note = binding.etNote.text.toString().trim(),
            reminder = binding.switchReminder.isChecked,
            reminderMinutes = binding.seekbarReminderTime.progress,
            autoSilent = binding.switchAutoSilent.isChecked,
            tableId = tableId
        )
        
        lifecycleScope.launch {
            if (isEditing) {
                viewModel.updateCourse(course)
            } else {
                viewModel.addCourse(course)
            }
            finish()
        }
    }
    
    companion object {
        private const val EXTRA_COURSE_ID = "extra_course_id"
        private const val EXTRA_TABLE_ID = "extra_table_id"
        
        private const val ALL = 0
        private const val ODD = 1
        private const val EVEN = 2
        
        fun start(context: Context, tableId: Long, courseId: Long = 0) {
            val intent = Intent(context, EditCourseActivity::class.java).apply {
                putExtra(EXTRA_TABLE_ID, tableId)
                putExtra(EXTRA_COURSE_ID, courseId)
            }
            context.startActivity(intent)
        }
    }
} 