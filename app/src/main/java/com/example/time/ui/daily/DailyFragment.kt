package com.example.time.ui.daily

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.time.R
import com.example.time.data.model.Course
import com.example.time.databinding.FragmentDailyBinding
import com.example.time.ui.main.MainViewModel
import com.example.time.data.SettingsManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DailyFragment : Fragment() {

    private var _binding: FragmentDailyBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: DailyCourseAdapter
    private lateinit var settingsManager: SettingsManager
    
    // 当前选择的日期
    private var currentDate = Calendar.getInstance()
    
    // 当前周数
    private var currentWeek = 1
    
    // 当前选择的课表ID
    private var currentTableId: Long = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        settingsManager = SettingsManager(requireContext())
        
        // 初始化当前日期为今天
        currentDate = Calendar.getInstance()
        
        // 计算当前周数
        calculateCurrentWeek()
        
        Log.d("DailyFragment", "初始化: 当前日期: ${SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(currentDate.time)}, 当前周数: $currentWeek")
        
        setupUI()
        observeData()
    }
    
    private fun calculateCurrentWeek() {
        // 获取开学日期
        val settings = settingsManager.loadSettings()
        val startDate = settings.termStartDate

        // 添加日志记录开学日期
        val dateFormat = SimpleDateFormat("yyyy-MM-dd (EEE)", Locale.CHINA)
        Log.d("DailyFragment", "初始化 - 开学日期: ${dateFormat.format(startDate.time)}")
        
        // 计算当前日期
        val today = Calendar.getInstance()
        Log.d("DailyFragment", "今天: ${dateFormat.format(today.time)}")
        
        // 先找到开学日期那周的周一
        val termStartWeekMonday = startDate.clone() as Calendar
        while (termStartWeekMonday.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            termStartWeekMonday.add(Calendar.DAY_OF_MONTH, -1)
        }
        Log.d("DailyFragment", "第一周周一: ${dateFormat.format(termStartWeekMonday.time)}")
        
        // 计算学期结束日期（第一周周一 + 学期周数*7 - 1天）
        val termEndDate = termStartWeekMonday.clone() as Calendar
        termEndDate.add(Calendar.DAY_OF_YEAR, settings.totalWeeks * 7 - 1)
        Log.d("DailyFragment", "学期结束日期: ${dateFormat.format(termEndDate.time)}")
        
        // 判断今天是否在学期范围内
        if (today.before(termStartWeekMonday) || today.after(termEndDate)) {
            // 如果不在学期范围内，设置currentWeek为-1表示非学期时间
            currentWeek = -1
            Log.d("DailyFragment", "今天不在学期范围内")
            return
        }
        
        // 计算今天与第一周周一的差距（天数）
        val diffMillis = today.timeInMillis - termStartWeekMonday.timeInMillis
        val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
        
        // 计算当前周数（第几周）
        val calculatedWeek = (diffDays / 7) + 1
        Log.d("DailyFragment", "相差天数: $diffDays, 计算得到当前周数: $calculatedWeek")
        
        // 确保周数在有效范围内
        currentWeek = when {
            calculatedWeek < 1 -> 1
            calculatedWeek > settings.totalWeeks -> settings.totalWeeks
            else -> calculatedWeek
        }
        
        Log.d("DailyFragment", "初始化 - 当前周数设置为: $currentWeek")
    }
    
    private fun setupUI() {
        // 设置日期显示
        updateDateDisplay()
        
        // 设置RecyclerView
        adapter = DailyCourseAdapter()
        binding.recyclerCourses.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCourses.adapter = adapter
        
        // 设置CalendarView为中文
        setupChineseCalendarView()
    }
    
    /**
     * 设置日历为中文显示
     */
    private fun setupChineseCalendarView() {
        try {
            // 设置每周第一天为周一
            binding.calendarView.setFirstDayOfWeek(Calendar.MONDAY)
            
            // 设置全局区域为中文
            Locale.setDefault(Locale.CHINA)
            val configuration = resources.configuration
            configuration.setLocale(Locale.CHINA)
            resources.updateConfiguration(configuration, resources.displayMetrics)
            
            // 更新日历视图
            binding.calendarView.date = currentDate.timeInMillis
            
            // 添加日期变化监听器，确保日期以中文格式显示
            binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                // 更新当前选择的日期
                currentDate.set(year, month, dayOfMonth)
                
                // 计算所选日期对应的周数
                calculateWeekForSelectedDate(currentDate)
                
                // 更新界面显示
                updateUIForSelectedDay()
            }
        } catch (e: Exception) {
            Log.e("DailyFragment", "设置中文日历失败", e)
        }
    }
    
    /**
     * 计算所选日期对应的周数
     */
    private fun calculateWeekForSelectedDate(selectedDate: Calendar) {
        val settings = settingsManager.loadSettings()
        val startDate = settings.termStartDate

        // 找到开学日期所在周的周一
        val termStartWeekMonday = startDate.clone() as Calendar
        while (termStartWeekMonday.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            termStartWeekMonday.add(Calendar.DAY_OF_MONTH, -1)
        }
        
        // 计算学期结束日期
        val termEndDate = termStartWeekMonday.clone() as Calendar
        termEndDate.add(Calendar.DAY_OF_YEAR, settings.totalWeeks * 7 - 1)
        
        // 判断所选日期是否在学期范围内
        if (selectedDate.before(termStartWeekMonday) || selectedDate.after(termEndDate)) {
            // 如果不在学期范围内，设置currentWeek为-1表示非学期时间
            currentWeek = -1
            Log.d("DailyFragment", "所选日期不在学期范围内: ${SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(selectedDate.time)}")
            return
        }

        // 计算所选日期与第一周周一的差距（天数）
        val diffMillis = selectedDate.timeInMillis - termStartWeekMonday.timeInMillis
        val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

        // 计算所选日期的周数（第几周）
        val calculatedWeek = (diffDays / 7) + 1

        // 确保周数在有效范围内
        currentWeek = when {
            calculatedWeek < 1 -> 1
            calculatedWeek > settings.totalWeeks -> settings.totalWeeks
            else -> calculatedWeek
        }
        
        Log.d("DailyFragment", "所选日期: ${SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(selectedDate.time)}, 计算周数: $currentWeek")
    }
    
    private fun observeData() {
        // 获取默认课表ID
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getDefaultCourseTable()?.let { courseTable ->
                currentTableId = courseTable.id
                // 加载当天课程
                loadCoursesForCurrentDay()
            }
        }
    }
    
    private fun loadCoursesForCurrentDay() {
        // 如果当前不在学期时间内，显示空列表
        if (currentWeek == -1) {
            Log.d("DailyFragment", "当前日期不在学期范围内，不显示课程")
            updateCourseList(emptyList())
            binding.tvEmpty.text = "非学期时间"
            binding.tvEmpty.visibility = View.VISIBLE
            binding.recyclerCourses.visibility = View.GONE
            return
        }
        
        // 恢复默认的空提示文本
        binding.tvEmpty.text = "今日没有课程"
        
        // 获取选择日期是周几（0-6，周日是0）
        val dayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK)
        // 转换为应用中的表示（0-6，周一是0）
        val day = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
        
        Log.d("DailyFragment", "加载周${day+1}第${currentWeek}周的课程，日期：${SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(currentDate.time)}")
        
        // 使用getCoursesByDayAndWeek代替getCoursesByDay，同时过滤周次
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.getCoursesByDayAndWeek(currentTableId, day, currentWeek).collectLatest { courses ->
                    Log.d("DailyFragment", "获取到课程数量: ${courses.size}")
                    for (course in courses) {
                        Log.d("DailyFragment", "课程: ${course.name}, 周次: ${course.weekList}, 当前周: $currentWeek")
                    }
                updateCourseList(courses)
                }
            } catch (e: Exception) {
                Log.e("DailyFragment", "加载课程出错: ${e.message}", e)
            }
        }
    }
    
    private fun updateCourseList(courses: List<Course>) {
        if (courses.isEmpty()) {
            Log.d("DailyFragment", "没有课程，显示空提示")
            binding.tvEmpty.visibility = View.VISIBLE
            binding.recyclerCourses.visibility = View.GONE
        } else {
            Log.d("DailyFragment", "有${courses.size}个课程，显示课程列表")
            binding.tvEmpty.visibility = View.GONE
            binding.recyclerCourses.visibility = View.VISIBLE
            adapter.submitList(courses)
        }
    }
    
    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.CHINA)
        val weekText = if (currentWeek > 0) "第${currentWeek}周" else "非学期时间"
        
        // 更新标题栏显示
        binding.tvDate.text = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(currentDate.time)
        binding.tvWeekday.text = SimpleDateFormat("EEEE", Locale.CHINA).format(currentDate.time)
        binding.tvWeek.text = weekText
    }
    
    private fun updateUIForSelectedDay() {
        // 只有当Fragment已附加到Activity时才更新UI
        if (!isAdded) return
        
        // 更新UI显示
        updateDateDisplay()
        
        // 加载课程数据
        loadCoursesForCurrentDay()
        
        // 如果不在学期范围内，显示提示信息
        if (currentWeek == -1) {
            Toast.makeText(requireContext(), "当前日期不在学期范围内", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 