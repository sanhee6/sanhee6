package com.example.time.ui.settings

import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.time.MainApplication
import com.example.time.R
import com.example.time.data.SettingsManager
import com.example.time.data.model.SectionTime
import com.example.time.databinding.ActivitySectionTimeEditorBinding
import java.util.Locale

class SectionTimeEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySectionTimeEditorBinding
    private lateinit var settingsManager: SettingsManager
    private val sectionTimeList = mutableListOf<SectionTime>()
    private lateinit var adapter: SectionTimeAdapter

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
        
        binding = ActivitySectionTimeEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 设置工具栏
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "课程时间设置"
        
        // 初始化设置管理器
        settingsManager = SettingsManager(this)
        
        // 获取课程时间列表
        sectionTimeList.addAll(settingsManager.loadSettings().sectionTimes)
        
        // 设置RecyclerView
        setupRecyclerView()
        
        // 设置按钮点击事件
        binding.btnAddSection.setOnClickListener {
            addNewSection()
        }
        
        binding.btnSave.setOnClickListener {
            saveSettings()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = SectionTimeAdapter()
        binding.recyclerSectionTimes.layoutManager = LinearLayoutManager(this)
        binding.recyclerSectionTimes.adapter = adapter
    }
    
    private fun addNewSection() {
        val nextSection = sectionTimeList.size + 1
        val newSection = SectionTime(nextSection, "08:00", "08:45")
        sectionTimeList.add(newSection)
        adapter.notifyItemInserted(sectionTimeList.size - 1)
    }
    
    private fun saveSettings() {
        if (sectionTimeList.isEmpty()) {
            Toast.makeText(this, "课程时间列表不能为空", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 重新设置节次编号，确保顺序正确
        for (i in sectionTimeList.indices) {
            sectionTimeList[i] = sectionTimeList[i].copy(section = i + 1)
        }
        
        // 保存设置
        val settings = settingsManager.loadSettings()
        settings.sectionTimes = sectionTimeList
        settingsManager.saveSettings(settings)
        
        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    
    inner class SectionTimeAdapter : RecyclerView.Adapter<SectionTimeAdapter.ViewHolder>() {
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvSection: TextView = itemView.findViewById(R.id.tv_section)
            val tvStartTime: TextView = itemView.findViewById(R.id.tv_start_time)
            val tvEndTime: TextView = itemView.findViewById(R.id.tv_end_time)
            val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_section_time, parent, false)
            return ViewHolder(view)
        }
        
        override fun getItemCount() = sectionTimeList.size
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val sectionTime = sectionTimeList[position]
            
            holder.tvSection.text = sectionTime.section.toString()
            holder.tvStartTime.text = sectionTime.startTime
            holder.tvEndTime.text = sectionTime.endTime
            
            // 设置时间选择点击事件
            holder.tvStartTime.setOnClickListener {
                showTimePickerDialog(sectionTime.startTime) { newTime ->
                    sectionTime.startTime = newTime
                    holder.tvStartTime.text = newTime
                }
            }
            
            holder.tvEndTime.setOnClickListener {
                showTimePickerDialog(sectionTime.endTime) { newTime ->
                    sectionTime.endTime = newTime
                    holder.tvEndTime.text = newTime
                }
            }
            
            // 设置删除按钮点击事件
            holder.btnDelete.setOnClickListener {
                if (sectionTimeList.size > 1) {
                    sectionTimeList.removeAt(position)
                    notifyDataSetChanged()  // 完全刷新，确保节次编号正确
                } else {
                    Toast.makeText(this@SectionTimeEditorActivity, "至少需要保留一节课", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun showTimePickerDialog(initialTime: String, onTimeSelected: (String) -> Unit) {
        val parts = initialTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        
        TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val timeString = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                onTimeSelected(timeString)
            },
            hour, minute, true
        ).show()
    }
} 