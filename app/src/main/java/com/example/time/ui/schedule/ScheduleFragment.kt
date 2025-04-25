package com.example.time.ui.schedule

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.time.R
import com.example.time.data.model.Course
import com.example.time.data.model.TimeTable
import com.example.time.databinding.FragmentScheduleBinding
import com.example.time.ui.course.EditCourseActivity
import com.example.time.ui.main.MainViewModel
import com.example.time.data.SettingsManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ScheduleFragment : Fragment(), GestureDetector.OnGestureListener {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MainViewModel
    private lateinit var gestureDetector: GestureDetectorCompat
    private lateinit var settingsManager: SettingsManager
    
    // 课程表格高度单位
    private val sectionHeight = 150
    
    // 当前周数
    private var currentWeek = 1
    
    // 当前选择的课表ID
    private var currentTableId: Long = 1
    
    // 开学日期 - 从设置中获取
    private lateinit var startDate: Calendar

    // 记录上次触摸位置
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        gestureDetector = GestureDetectorCompat(requireContext(), this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        settingsManager = SettingsManager(requireContext())
        
        // 加载设置
        loadSettings()
        
        setupUI()
        setupGestureDetection()
        observeData()
        
        // 初始化滚动视图
        binding.scheduleScrollView.post {
            binding.scheduleScrollView.setScrollToActualHeight()
        }
        
        // 获取并设置底部安全区域边距
        setupBottomPadding()
        
        // 设置课程列表按钮点击事件
        binding.btnCourseList.setOnClickListener {
            showCourseList()
        }
    }
    
    private fun setupGestureDetection() {
        // 创建更敏感的手势检测器
        gestureDetector = GestureDetectorCompat(requireContext(), this).apply {
            setIsLongpressEnabled(false) // 禁用长按，避免干扰滑动
        }
        
        // 为整个滚动视图添加触摸监听，扩大检测区域
        binding.scheduleScrollView.setOnTouchListener { _, event ->
            // 只处理水平方向的手势，垂直方向交给ScrollView处理
            val result = gestureDetector.onTouchEvent(event)
            
            // 判断是否是明显的水平滑动
            if (event.action == MotionEvent.ACTION_MOVE) {
                val deltaX = Math.abs(event.x - lastTouchX)
                val deltaY = Math.abs(event.y - lastTouchY)
                
                // 如果是明显的水平滑动，则消耗事件
                if (deltaX > deltaY * 1.5 && deltaX > 20) {
                    return@setOnTouchListener true
                }
            }
            
            if (event.action == MotionEvent.ACTION_DOWN) {
                lastTouchX = event.x
                lastTouchY = event.y
            }
            
            // 不消耗事件，允许滚动视图处理垂直滚动
            false
        }
    }
    
    private fun setupUI() {
        // 设置周数和日期显示
        updateWeekAndDateDisplay()
        
        // 渲染课表框架
        renderSchedule()
    }
    
    private fun updateWeekAndDateDisplay() {
        if (currentWeek > 0) {
            binding.tvCurrentWeek.text = "第${currentWeek}周"
        } else {
            binding.tvCurrentWeek.text = "假期"
        }

        val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
        
        // 克隆开学日期日历对象
        val weekStart = startDate.clone() as Calendar
        
        // 计算第n周的第一天（周一）
        // 先将日期设为开学的那一周的周一
        while (weekStart.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            weekStart.add(Calendar.DAY_OF_MONTH, -1)
        }
        
        // 然后加上(周数-1)*7天，移动到目标周
        weekStart.add(Calendar.DAY_OF_MONTH, (currentWeek - 1) * 7)
        
        // 计算该周的最后一天（周日）
        val weekEnd = weekStart.clone() as Calendar
        weekEnd.add(Calendar.DAY_OF_MONTH, 6)
        
        // 更新日期范围显示
        binding.tvDateRange.text = "${dateFormat.format(weekStart.time)} - ${dateFormat.format(weekEnd.time)}"
        
        // 记录日志帮助调试
        Log.d("ScheduleFragment", "当前周: $currentWeek, 日期范围: ${dateFormat.format(weekStart.time)} - ${dateFormat.format(weekEnd.time)}")
        Log.d("ScheduleFragment", "周一: ${weekStart.get(Calendar.DAY_OF_WEEK)}, 周日: ${weekEnd.get(Calendar.DAY_OF_WEEK)}")
    }
    
    private fun calculateWeekStartDate(week: Int): Calendar {
        val calendar = startDate.clone() as Calendar
        // 加上(周数-1)*7天
        calendar.add(Calendar.DAY_OF_YEAR, (week - 1) * 7)
        return calendar
    }
    
    private fun calculateWeekEndDate(weekStart: Calendar): Calendar {
        val calendar = weekStart.clone() as Calendar
        // 加上6天得到周末
        calendar.add(Calendar.DAY_OF_YEAR, 6)
        return calendar
    }
    
    private fun changeWeek(change: Int, isRightSwipe: Boolean = false) {
        val settings = settingsManager.loadSettings()
        val oldWeek = currentWeek
        currentWeek += change
        if (currentWeek < 1) currentWeek = 1
        if (currentWeek > settings.totalWeeks) currentWeek = settings.totalWeeks
        
        // 如果周数没有变化，则不执行动画和更新
        if (oldWeek == currentWeek) return
        
        // 添加翻页动画效果
        val container = binding.courseContainer
        
        // 设置初始状态
        container.alpha = 1f
        
        // 方向参数 - 修正动画方向
        val fromX = if (isRightSwipe) 1f else -1f
        
        // 执行动画
        container.animate()
            .alpha(0f)
            .translationX(container.width * fromX * 0.3f) // 向移动方向偏移30%宽度
            .setDuration(150)
            .withEndAction {
                // 重置位置，但保持透明
                container.translationX = -container.width * fromX * 0.3f
                
                // 更新UI
        updateWeekAndDateDisplay()
        
        // 重新加载课程
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllCourses(currentTableId).collectLatest { courses ->
                renderCourses(courses)
                        
                        // 显示新内容的动画
                        container.animate()
                            .alpha(1f)
                            .translationX(0f)
                            .setDuration(150)
                            .start()
                    }
            }
        }
            .start()
    }
    
    private fun observeData() {
        // 观察课程数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllCourses(currentTableId).collectLatest { courses ->
                renderCourses(courses)
            }
        }
        
        // 观察时间表数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getDefaultTimeTable()?.let { timeTable ->
                // 重新渲染课表框架
                renderSchedule()
            }
        }
        
        // 获取默认课表ID
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getDefaultCourseTable()?.let { courseTable ->
                currentTableId = courseTable.id
                // 重新加载课程数据
                viewModel.getAllCourses(currentTableId).collectLatest { courses ->
                    renderCourses(courses)
                }
            }
        }
    }
    
    private fun renderCourses(courses: List<Course>) {
        // 清除之前的课程视图，但保留课表框架
        // 找到并移除只有课程视图，保留网格和标题
        val viewsToRemove = ArrayList<View>()
        for (i in 0 until binding.courseContainer.childCount) {
            val view = binding.courseContainer.getChildAt(i)
            if (view is CardView) {
                viewsToRemove.add(view)
            }
        }
        
        for (view in viewsToRemove) {
            binding.courseContainer.removeView(view)
        }
        
        // 过滤当前周次的课程
        val currentWeekCourses = courses.filter { it.weekList.contains(currentWeek) }
        
        Log.d("ScheduleFragment", "当前周 $currentWeek 的课程数量: ${currentWeekCourses.size}")
        
        // 为每个课程创建卡片视图
        currentWeekCourses.forEach { course ->
            // 确保day在有效范围内(0-6)
            if (course.day < 0 || course.day > 6) {
                Log.e("ScheduleFragment", "课程 ${course.name} 的day值无效: ${course.day}")
                return@forEach
            }
            
            // 确保开始节次和结束节次有效(1-10)
            if (course.startSection < 1 || course.startSection > 10 || 
                course.endSection < 1 || course.endSection > 10 ||
                course.startSection > course.endSection) {
                Log.e("ScheduleFragment", "课程 ${course.name} 的节次无效: ${course.startSection}-${course.endSection}")
                return@forEach
            }
            
            Log.d("ScheduleFragment", "渲染课程: ${course.name}, 星期=${course.day}(${getDayName(course.day)}), 节次=${course.startSection}-${course.endSection}")
            
            val courseView = createCourseView(course)
            binding.courseContainer.addView(courseView)
        }
    }
    
    private fun getDayName(day: Int): String {
        return when(day) {
            0 -> "周一"
            1 -> "周二"
            2 -> "周三"
            3 -> "周四"
            4 -> "周五"
            5 -> "周六"
            6 -> "周日"
            else -> ""
        }
    }
    
    private fun createCourseView(course: Course): CardView {
        // 从布局文件加载课程卡片视图
        val cardView = layoutInflater.inflate(R.layout.item_course, null) as CardView
        
        // 计算屏幕宽度和每个单元格的宽度
        val screenWidth = getScreenWidth()
        val timeColumnWidth = screenWidth / 8 // 时间列宽度
        val dayColumnWidth = (screenWidth - timeColumnWidth) / 7 // 每天的宽度
        
        // 确保课程卡片宽度精确等于单元格宽度
        val width = dayColumnWidth
        
        // 计算高度 - 节次 * 每节课高度
        val height = sectionHeight * (course.endSection - course.startSection + 1)
        
        // 创建布局参数 - 使用绝对定位确保正确放置
        val layoutParams = FrameLayout.LayoutParams(width, height, Gravity.NO_GRAVITY)
        
        // 计算卡片位置（基于星期几和节次）
        val dayColumn = course.day  // 0表示周一, 1表示周二...
        val startRow = course.startSection - 1  // 节次从1开始，所以减1
        
        // 精确计算左边距：时间列宽度 + 星期几列宽度 * 当前星期偏移
        val left = timeColumnWidth + dayColumnWidth * dayColumn
        val top = dpToPx(30) + sectionHeight * startRow
        
        Log.d("ScheduleFragment", "课程位置计算: " +
              "课程=${course.name}, " +
              "星期=${course.day}(${getDayName(course.day)}), " +
              "开始节次=${course.startSection}, " +
              "结束节次=${course.endSection}, " +
              "屏幕宽度=$screenWidth, " +
              "时间列宽度=$timeColumnWidth, " +
              "星期列宽度=$dayColumnWidth, " +
              "卡片宽度=$width, " +
              "卡片高度=$height, " +
              "左边距=$left, " +
              "顶部边距=$top")
              
        layoutParams.leftMargin = left
        layoutParams.topMargin = top
        
        cardView.layoutParams = layoutParams
        
        // 设置卡片内容
        val nameTextView = cardView.findViewById<TextView>(R.id.tv_course_name)
        val locationTextView = cardView.findViewById<TextView>(R.id.tv_course_location)
        val teacherTextView = cardView.findViewById<TextView>(R.id.tv_course_teacher)
        
        nameTextView.text = course.name
        locationTextView.text = course.classroom
        teacherTextView.text = course.teacher
        
        // 设置卡片背景颜色
        cardView.setCardBackgroundColor(course.color)
        
        // 设置点击事件
        cardView.setOnClickListener {
            // 打开课程详情/编辑界面
            EditCourseActivity.start(requireContext(), currentTableId, course.id)
        }
        
        return cardView
    }
    
    private fun getScreenWidth(): Int {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }
    
    private fun dpToPx(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
    
    // GestureDetector.OnGestureListener 接口实现
    override fun onDown(e: MotionEvent): Boolean = true
    
    override fun onShowPress(e: MotionEvent) {}
    
    override fun onSingleTapUp(e: MotionEvent): Boolean = false
    
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = false
    
    override fun onLongPress(e: MotionEvent) {}
    
    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        // 大幅降低阈值，提高灵敏度
        val SWIPE_THRESHOLD = 20
        val SWIPE_VELOCITY_THRESHOLD = 20
        
        try {
        if (e1 != null) {
            val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                
                // 确保水平滑动比垂直滑动更明显，避免斜向滑动被误判
                if (Math.abs(diffX) > Math.abs(diffY) &&
                    Math.abs(diffX) > SWIPE_THRESHOLD && 
                    Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    
                if (diffX > 0) {
                    // 右滑，上一周
                        changeWeek(-1, true)
                } else {
                    // 左滑，下一周
                        changeWeek(1, false)
                    }
                    return true
                }
            }
        } catch (exception: Exception) {
            // 处理任何可能的异常
            Log.e("ScheduleFragment", "滑动手势处理出错: ${exception.message}")
        }
        
        return false
    }
    
    private fun renderSchedule() {
        // 清除之前的视图
        binding.courseContainer.removeAllViews()
        
        // 加载设置
        val settings = settingsManager.loadSettings()
        
        // 计算屏幕宽度和每个单元格的宽度
        val screenWidth = getScreenWidth()
        val timeColumnWidth = screenWidth / 8 // 时间列宽度
        val dayColumnWidth = (screenWidth - timeColumnWidth) / 7 // 每一天的宽度平均分配
        val dayRowHeight = dpToPx(30) // 星期标题行高度
        
        // 计算课表总高度 - 确保足够容纳所有节次
        val totalHeight = dayRowHeight + settings.sectionsPerDay * sectionHeight
        
        // 添加整个课表的背景
        val scheduleBackground = View(requireContext())
        val backgroundParams = FrameLayout.LayoutParams(screenWidth, totalHeight)
        scheduleBackground.layoutParams = backgroundParams
        scheduleBackground.setBackgroundColor(Color.parseColor("#F5F5F5"))
        binding.courseContainer.addView(scheduleBackground)
        
        // 设置FrameLayout的最小高度，确保可以滚动
        binding.courseContainer.minimumHeight = totalHeight
        
        // 添加左上角空白区域
        val cornerView = View(requireContext())
        val cornerParams = FrameLayout.LayoutParams(timeColumnWidth, dayRowHeight)
        cornerView.layoutParams = cornerParams
        cornerView.setBackgroundColor(Color.parseColor("#EEEEEE"))
        binding.courseContainer.addView(cornerView)
        
        // 添加星期标题
        for (day in 0..6) {
            val dayView = TextView(requireContext())
            val dayParams = FrameLayout.LayoutParams(dayColumnWidth, dayRowHeight)
            dayParams.leftMargin = timeColumnWidth + day * dayColumnWidth
            dayView.layoutParams = dayParams
            dayView.text = getDayName(day)
            dayView.gravity = Gravity.CENTER
            dayView.setTypeface(null, Typeface.BOLD)
            dayView.setBackgroundColor(Color.parseColor("#EEEEEE"))
            binding.courseContainer.addView(dayView)
        }
        
        // 添加时间标签（节次）
        val sections = settings.sectionsPerDay // 使用设置中的节数
        for (section in 0 until sections) {
            val sectionView = TextView(requireContext())
            val sectionParams = FrameLayout.LayoutParams(timeColumnWidth, sectionHeight)
            sectionParams.topMargin = dayRowHeight + section * sectionHeight
            sectionView.layoutParams = sectionParams
            
            // 如果有对应的时间设置，显示时间信息
            if (section < settings.sectionTimes.size) {
                val sectionTime = settings.sectionTimes[section]
                sectionView.text = "${sectionTime.section}\n${sectionTime.startTime}"
            } else {
                sectionView.text = (section + 1).toString()
            }
            
            sectionView.gravity = Gravity.CENTER
            sectionView.textSize = 10f
            sectionView.setBackgroundColor(Color.parseColor("#EEEEEE"))
            binding.courseContainer.addView(sectionView)
        }
        
        // 添加背景网格
        for (day in 0..6) {
            for (section in 0 until sections) {
                val gridCell = View(requireContext())
                val cellParams = FrameLayout.LayoutParams(dayColumnWidth, sectionHeight)
                cellParams.leftMargin = timeColumnWidth + day * dayColumnWidth
                cellParams.topMargin = dayRowHeight + section * sectionHeight
                gridCell.layoutParams = cellParams
                gridCell.setBackgroundColor(Color.parseColor("#10000000")) // 浅灰色背景
                // 为每个格子添加边框
                gridCell.setBackgroundResource(R.drawable.bg_grid_cell)
                binding.courseContainer.addView(gridCell)
            }
        }
        
        // 加载课程数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllCourses(currentTableId).collectLatest { courses ->
                renderCourses(courses)
            }
        }
    }
    
    private fun showCourseList() {
        // 创建一个底部弹出的列表对话框
        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_course_list)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        
        // 获取课程列表视图
        val courseListView = dialog.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.course_list)
        courseListView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        
        // 加载所有课程数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllCourses(currentTableId).collectLatest { courses ->
                // 创建一个简单的适配器来显示课程
                val adapter = CourseListAdapter(courses) { course ->
                    // 点击课程项时，打开编辑界面
                    EditCourseActivity.start(requireContext(), currentTableId, course.id)
                    dialog.dismiss()
                }
                courseListView.adapter = adapter
            }
        }
        
        // 设置添加新课程按钮点击事件
        dialog.findViewById<Button>(R.id.btn_add_course).setOnClickListener {
            // 创建新课程
            EditCourseActivity.start(requireContext(), currentTableId, 0L) // 0L表示新建课程
            dialog.dismiss()
        }
        
        // 显示对话框
        dialog.show()
    }
    
    // 简单的课程列表适配器
    private inner class CourseListAdapter(
        private val courses: List<Course>,
        private val onCourseClick: (Course) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<CourseListAdapter.ViewHolder>() {
        
        inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            val courseNameText: TextView = itemView.findViewById(R.id.tv_course_name)
            val courseInfoText: TextView = itemView.findViewById(R.id.tv_course_info)
            val editButton: View = itemView.findViewById(R.id.btn_edit)
            val deleteButton: View = itemView.findViewById(R.id.btn_delete)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course_list, parent, false)
            return ViewHolder(view)
        }
        
        override fun getItemCount(): Int = courses.size
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val course = courses[position]
            holder.courseNameText.text = course.name
            holder.courseInfoText.text = "${getDayName(course.day)} 第${course.startSection}-${course.endSection}节 | ${course.classroom} | ${course.teacher}"
            
            // 设置背景颜色条
            val colorView = holder.itemView.findViewById<View>(R.id.view_color)
            colorView.setBackgroundColor(course.color)
            
            // 设置点击事件 - 编辑
            holder.editButton.setOnClickListener {
                onCourseClick(course)
            }
            
            // 设置点击事件 - 删除
            holder.deleteButton.setOnClickListener {
                // 显示确认删除对话框
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("确认删除")
                    .setMessage("确定要删除课程 '${course.name}' 吗？")
                    .setPositiveButton("删除") { _, _ ->
                        // 删除课程
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.deleteCourse(course)
                            // 刷新课表
                            renderSchedule()
                        }
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        }
    }
    
    private fun loadSettings() {
        val settings = settingsManager.loadSettings()
        startDate = settings.termStartDate
        
        // 添加详细日志记录开学日期
        val dateFormat = SimpleDateFormat("yyyy-MM-dd (EEE)", Locale.CHINA)
        Log.d("ScheduleFragment", "开学日期: ${dateFormat.format(startDate.time)}")
        
        // 计算当前日期是第几周
        val today = Calendar.getInstance()
        
        // 先找到开学日期那周的周一
        val termStartWeekMonday = startDate.clone() as Calendar
        while (termStartWeekMonday.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            termStartWeekMonday.add(Calendar.DAY_OF_MONTH, -1)
        }
        
        // 计算今天与第一周周一的差距（天数）
        val diffMillis = today.timeInMillis - termStartWeekMonday.timeInMillis
        val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
        val calculatedWeek = (diffDays / 7) + 1
        
        Log.d("ScheduleFragment", "第一周周一: ${dateFormat.format(termStartWeekMonday.time)}")
        Log.d("ScheduleFragment", "今天: ${dateFormat.format(today.time)}")
        Log.d("ScheduleFragment", "相差天数: $diffDays, 计算得到当前周数: $calculatedWeek")
        
        // 设置当前周数，确保在合理范围内
        currentWeek = when {
            calculatedWeek < 1 -> 1
            calculatedWeek > settings.totalWeeks -> settings.totalWeeks
            else -> calculatedWeek
        }
        
        Log.d("ScheduleFragment", "最终设置的当前周数: $currentWeek")
        
        // 更新UI以反映修改后的设置
        updateWeekAndDateDisplay()
        // 重新渲染课表框架以适应新的设置
        renderSchedule()
    }
    
    private fun setupBottomPadding() {
        // 获取底部导航栏的高度并设置内边距
        val activity = requireActivity()
        activity.findViewById<View>(R.id.bottom_navigation)?.let { bottomNav ->
            bottomNav.post {
                val navHeight = bottomNav.height
                
                // 增加额外的边距确保最后两节课完全可见
                val extraPadding = dpToPx(16)
                val totalPadding = navHeight + extraPadding
                
                // 设置滚动视图的底部内边距
                binding.scheduleScrollView.setPadding(
                    binding.scheduleScrollView.paddingLeft,
                    binding.scheduleScrollView.paddingTop,
                    binding.scheduleScrollView.paddingRight,
                    totalPadding
                )
                
                // 保持内边距但允许内容滚动到边距区域
                binding.scheduleScrollView.clipToPadding = false
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 