package com.example.time.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.widget.NestedScrollView

/**
 * 自定义可控制滚动范围的NestedScrollView
 */
class ScalableNestedScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {
    
    // 已经滚动到底部的标志
    private var isBottomReached = false
    
    // 已经滚动到顶部的标志
    private var isTopReached = true
    
    // 记录水平滑动状态，用于区分水平和垂直滑动
    private var isHorizontalScrolling = false
    
    // 最小滑动距离用于判断方向
    private val TOUCH_SLOP = 8
    
    // 记录触摸起始点
    private var startX = 0f
    private var startY = 0f
    private var lastY = 0f
    
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        
        // 判断是否滚动到了顶部或底部
        if (childCount > 0) {
            isBottomReached = t >= (getChildAt(0).measuredHeight - measuredHeight)
            isTopReached = t <= 0
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 记录起始触摸点
                startX = event.x
                startY = event.y
                lastY = event.y
                // 重置水平滑动状态
                isHorizontalScrolling = false
            }
            
            MotionEvent.ACTION_MOVE -> {
                val deltaX = Math.abs(event.x - startX)
                val deltaY = Math.abs(event.y - startY)
                
                // 如果尚未确定滑动方向，并且移动距离足够大，则确定方向
                if (!isHorizontalScrolling && (deltaX > TOUCH_SLOP || deltaY > TOUCH_SLOP)) {
                    isHorizontalScrolling = deltaX > deltaY
                }
                
                // 更新最后的Y位置
                lastY = event.y
            }
            
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 重置状态
                isHorizontalScrolling = false
            }
        }
        
        // 如果是水平滑动，让父视图或其他处理器处理
        if (isHorizontalScrolling) {
            return false
        }
        
        // 其他情况使用默认垂直滚动处理
        return super.onTouchEvent(event)
    }
    
    /**
     * 检查是否可以向特定方向滚动
     */
    override fun canScrollVertically(direction: Int): Boolean {
        // 如果是水平滑动，不要干扰垂直滚动
        if (isHorizontalScrolling) {
            return false
        }
        
        return super.canScrollVertically(direction)
    }
    
    /**
     * 设置滚动到内容的实际高度
     */
    fun setScrollToActualHeight() {
        if (childCount > 0) {
            val child = getChildAt(0)
            child.measure(
                MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
        }
    }
} 