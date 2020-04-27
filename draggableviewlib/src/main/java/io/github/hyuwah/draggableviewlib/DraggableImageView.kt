package io.github.hyuwah.draggableviewlib

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import io.github.hyuwah.draggableviewlib.Draggable.DRAG_TOLERANCE
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 29/01/2019
 * muhammad.whydn@gmail.com
 */
class DraggableImageView(context: Context, attrs: AttributeSet) : ImageView(context, attrs) {

    companion object {
        const val NON_STICKY = 0
        const val STICKY_AXIS_X = 1
        const val STICKY_AXIS_Y = 2
        const val STICKY_AXIS_XY = 3
    }

    // Attributes
    private var stickyAxis: Int
    private var mAnimate: Boolean

    private var draggableListener: DraggableListener? = null

    // Coordinates
    private var widgetInitialX: Float = 0F
    private var widgetDX: Float = 0F
    private var widgetInitialY: Float = 0F
    private var widgetDY: Float = 0F
    private var marginStart = 0F
    private var marginTop = 0F
    private var marginEnd = 0F
    private var marginBottom = 0F

    init {
        val attributes = intArrayOf(android.R.attr.layout_marginStart, android.R.attr.layout_marginTop, android.R.attr.layout_marginEnd, android.R.attr.layout_marginBottom)
        val typedArray = context.obtainStyledAttributes(attrs, attributes)
        try {
            marginStart = typedArray.getDimensionPixelOffset(0, 0).toFloat()
            @SuppressWarnings("ResourceType")
            marginTop = typedArray.getDimensionPixelOffset(1, 0).toFloat()
            @SuppressWarnings("ResourceType")
            marginEnd = typedArray.getDimensionPixelOffset(2, 0).toFloat()
            @SuppressWarnings("ResourceType")
            marginBottom = typedArray.getDimensionPixelOffset(3, 0).toFloat()
        } finally {
            typedArray.recycle()
        }

        context.theme.obtainStyledAttributes(attrs, R.styleable.DraggableImageView, 0, 0).apply {
            try {
                stickyAxis = getInteger(R.styleable.DraggableImageView_sticky, 0)
                mAnimate = getBoolean(R.styleable.DraggableImageView_animate, false)
            } finally {
                recycle()
            }
        }

        draggableSetup()
    }

    /**
     * Draggable Touch Setup
     */
    private fun draggableSetup() {
        this.setOnTouchListener { v, event ->
            val viewParent = v.parent as View
            val parentHeight = viewParent.height
            val parentWidth = viewParent.width
            val xMax = parentWidth - v.width - marginEnd
            val xMiddle = parentWidth / 2
            val yMax = parentHeight - v.height - marginBottom
            val yMiddle = parentHeight / 2

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    widgetDX = v.x - event.rawX
                    widgetDY = v.y - event.rawY
                    widgetInitialX = v.x
                    widgetInitialY = v.y
                }
                MotionEvent.ACTION_MOVE -> {
                    var newX = event.rawX + widgetDX
                    newX = max(marginStart, newX)
                    newX = min(xMax, newX)
                    v.x = newX

                    var newY = event.rawY + widgetDY
                    newY = max(marginTop, newY)
                    newY = min(yMax, newY)
                    v.y = newY

                    draggableListener?.onPositionChanged(v)
                }
                MotionEvent.ACTION_UP -> {
                    when (stickyAxis) {
                        STICKY_AXIS_X -> {
                            if (event.rawX >= xMiddle) {
                                if (mAnimate)
                                    v.animate().x(xMax)
                                        .setDuration(Draggable.DURATION_MILLIS)
                                        .setUpdateListener { draggableListener?.onPositionChanged(v) }
                                        .start()
                                else
                                    v.x = xMax
                            } else {
                                if (mAnimate)
                                    v.animate().x(marginStart).setDuration(Draggable.DURATION_MILLIS)
                                        .setUpdateListener { draggableListener?.onPositionChanged(v) }
                                        .start()
                                else
                                    v.x = marginStart
                            }
                        }
                        STICKY_AXIS_Y -> {
                            if (event.rawY >= yMiddle) {
                                if (mAnimate)
                                    v.animate().y(yMax)
                                        .setDuration(Draggable.DURATION_MILLIS)
                                        .setUpdateListener { draggableListener?.onPositionChanged(v) }
                                        .start()
                                else
                                    v.y = yMax
                            } else {
                                if (mAnimate)
                                    v.animate().y(marginTop)
                                        .setDuration(Draggable.DURATION_MILLIS)
                                        .setUpdateListener { draggableListener?.onPositionChanged(v) }
                                        .start()
                                else {
                                    if (mAnimate)
                                        v.animate().y(marginTop).setDuration(Draggable.DURATION_MILLIS)
                                            .setUpdateListener {
                                                draggableListener?.onPositionChanged(
                                                    v
                                                )
                                            }
                                            .start()
                                    else
                                        v.y = marginTop
                                }
                            }
                        }
                        STICKY_AXIS_XY -> {
                            if (event.rawX >= xMiddle) {
                                if (mAnimate)
                                    v.animate().x(xMax)
                                        .setDuration(Draggable.DURATION_MILLIS)
                                        .setUpdateListener { draggableListener?.onPositionChanged(v) }
                                        .start()
                                else
                                    v.x = xMax
                            } else {
                                if (mAnimate)
                                    v.animate().x(0F).setDuration(Draggable.DURATION_MILLIS)
                                        .setUpdateListener { draggableListener?.onPositionChanged(v) }
                                        .start()
                                v.x = 0F
                            }

                            if (event.rawY >= yMiddle) {
                                if (mAnimate)
                                    v.animate().y(yMax)
                                        .setDuration(Draggable.DURATION_MILLIS)
                                        .setUpdateListener { draggableListener?.onPositionChanged(v) }
                                        .start()
                                else
                                    v.y = yMax
                            } else {
                                if (mAnimate)
                                    v.animate().y(0F).setDuration(Draggable.DURATION_MILLIS)
                                        .setUpdateListener { draggableListener?.onPositionChanged(v) }
                                        .start()
                                else
                                    v.y = 0F
                            }
                        }
                    }

                    if (abs(v.x - widgetInitialX) <= DRAG_TOLERANCE && abs(v.y - widgetInitialY) <= DRAG_TOLERANCE) {
                        performClick()
                    }
                }
                else -> return@setOnTouchListener false
            }
            true
        }
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    /***
     * ATTRIBUTES setter / getter
     */

    fun setStickyAxis(axis: Int) {
        when (axis) {
            NON_STICKY, STICKY_AXIS_X, STICKY_AXIS_Y, STICKY_AXIS_XY -> {
                this.stickyAxis = axis
                invalidate()
                requestLayout()
            }
        }
    }

    fun isAnimate(): Boolean {
        return mAnimate
    }

    fun setAnimate(animate: Boolean) {
        mAnimate = animate
        invalidate()
        requestLayout()
    }

    fun setListener(draggableListener: DraggableListener?) {
        this.draggableListener = draggableListener
    }
}