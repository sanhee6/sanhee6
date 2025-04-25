package com.example.time.ui.daily

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.time.R
import com.example.time.data.model.Course
import com.example.time.ui.course.EditCourseActivity

class DailyCourseAdapter : ListAdapter<Course, DailyCourseAdapter.CourseViewHolder>(CourseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daily_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = getItem(position)
        holder.bind(course)
    }

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.card_course)
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_course_name)
        private val timeTextView: TextView = itemView.findViewById(R.id.tv_course_time)
        private val locationTextView: TextView = itemView.findViewById(R.id.tv_course_location)
        private val teacherTextView: TextView = itemView.findViewById(R.id.tv_course_teacher)

        fun bind(course: Course) {
            // 设置课程卡片颜色
            cardView.setCardBackgroundColor(course.color)
            
            // 设置课程内容
            nameTextView.text = course.name
            
            // 设置课程时间（第几节课）
            val timeText = "第${course.startSection}-${course.endSection}节"
            timeTextView.text = timeText
            
            // 设置教室位置
            locationTextView.text = course.classroom
            
            // 设置教师名称
            teacherTextView.text = course.teacher
            
            // 设置点击事件
            itemView.setOnClickListener {
                // 打开课程编辑页面
                EditCourseActivity.start(itemView.context, course.tableId, course.id)
            }
        }
    }

    class CourseDiffCallback : DiffUtil.ItemCallback<Course>() {
        override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem == newItem
        }
    }
} 