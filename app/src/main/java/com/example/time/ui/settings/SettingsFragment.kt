package com.example.time.ui.settings

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.time.R
import com.example.time.data.SettingsManager
import com.example.time.data.model.AppSettings
import com.example.time.databinding.FragmentSettingsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.content.DialogInterface

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var settingsManager: SettingsManager
    private lateinit var appSettings: AppSettings
    
    private val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        settingsManager = SettingsManager(requireContext())
        appSettings = settingsManager.loadSettings()
        
        // 初始化UI
        updateUI()
        
        // 设置点击事件
        setupClickListeners()
    }
    
    private fun updateUI() {
        // 更新学期开始日期
        binding.tvTermStartDate.text = dateFormat.format(appSettings.termStartDate.time)
        
        // 更新总周数
        binding.tvTotalWeeks.text = "${appSettings.totalWeeks}周"
        
        // 更新每天课程节数
        binding.tvSectionsPerDay.text = "${appSettings.sectionsPerDay}节"
    }
    
    private fun setupClickListeners() {
        // 学期开始日期
        binding.layoutTermStart.setOnClickListener {
            showDatePickerDialog(appSettings.termStartDate) { calendar ->
                appSettings.termStartDate = calendar
                binding.tvTermStartDate.text = dateFormat.format(calendar.time)
            }
        }
        
        // 总周数
        binding.layoutTotalWeeks.setOnClickListener {
            showNumberPickerDialog(
                "设置总周数",
                appSettings.totalWeeks,
                1,
                30
            ) { value ->
                appSettings.totalWeeks = value
                binding.tvTotalWeeks.text = "${value}周"
            }
        }
        
        // 每天课程节数
        binding.layoutSectionsPerDay.setOnClickListener {
            showNumberPickerDialog(
                "设置每天课程节数",
                appSettings.sectionsPerDay,
                1,
                16
            ) { value ->
                appSettings.sectionsPerDay = value
                binding.tvSectionsPerDay.text = "${value}节"
            }
        }
        
        // 编辑课程时间
        binding.layoutSectionTimes.setOnClickListener {
            val intent = Intent(requireContext(), SectionTimeEditorActivity::class.java)
            startActivity(intent)
        }
        
        // 保存设置
        binding.btnSaveSettings.setOnClickListener {
            // 保存设置
            settingsManager.saveSettings(appSettings)
            
            Toast.makeText(requireContext(), "设置已保存", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showDatePickerDialog(
        initialDate: Calendar,
        onDateSelected: (Calendar) -> Unit
    ) {
        val year = initialDate.get(Calendar.YEAR)
        val month = initialDate.get(Calendar.MONTH)
        val day = initialDate.get(Calendar.DAY_OF_MONTH)
        
        // 直接使用Activity作为Context，确保对话框能正确显示
        val activity = requireActivity()
        
        // 设置全局语言环境为中文，影响新创建的对话框
        Locale.setDefault(Locale.CHINA)
        val config = Resources.getSystem().configuration
        config.setLocale(Locale.CHINA)
        
        // 创建对话框
        val dialog = DatePickerDialog(
            activity,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                onDateSelected(selectedDate)
            },
            year, month, day
        )
        
        // 设置日期选择器的一些额外属性
        dialog.datePicker.firstDayOfWeek = Calendar.MONDAY
        
        // 手动设置对话框标题和按钮为中文
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", dialog)
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", dialog)
        
        dialog.show()
    }
    
    private fun showNumberPickerDialog(
        title: String,
        initialValue: Int,
        minValue: Int,
        maxValue: Int,
        onValueSelected: (Int) -> Unit
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_number_picker, null)
        
        val numberPicker = dialogView.findViewById<NumberPicker>(R.id.number_picker)
        numberPicker.minValue = minValue
        numberPicker.maxValue = maxValue
        numberPicker.value = initialValue
        
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("确定") { _, _ ->
                onValueSelected(numberPicker.value)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 