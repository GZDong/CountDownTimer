@file:Suppress("PrivatePropertyName")

package com.gzdong.countdowntimer.view

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.TypedValue
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import com.gzdong.countdowntimer.R
import com.gzdong.countdowntimer.utils.DelayExecutor
import com.gzdong.countdowntimer.utils.OnDelayExecutorListener
import java.util.concurrent.Executors

private const val TAG = "countDownTimerActivity"
const val FORMAT_1 = "d:h:m:s"
const val FORMAT_2 = "h:m:s"
const val FORMAT_3 = "m:s"
const val FORMAT_4 = "d:h:m"
const val FORMAT_5 = "h:m"
const val FORMAT_6 = "d:h"


class GCountDownTimer : SurfaceView, SurfaceHolder.Callback {
    private var surfaceHolder: SurfaceHolder? = null
    private var canvas: Canvas? = null
    private var paint: Paint? = null
    private var time = 0L
    private var delimiter_day_hour = ":"
    private var delimiter_hour_minute = ":"
    private var delimiter_minute_second = ":"
    private var unit_day = ""
    private var unit_hour = ""
    private var unit_minute = ""
    private var unit_second = ""
    private var day: Int = 0
    private var hour: Int = 0
    private var minute: Int = 0
    private var second: Int = 0
    private var format = FORMAT_1
    private val tmpStr = StringBuilder()
    private var result = ""
    private var countDownTimer: CountDownTimer? = null
    private var isAlive = false
    private var moveYpx = 0f
    private var isFinished = true
    private var txtSize = 12f
    private var textColor = 0
    private var textWidth = 0f
    var finishedListener: OnCountDownFinishedListener? = null
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()
    private val onContextLifecycleListener = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {
            if (activity.findViewById<View>(android.R.id.content).tag != null &&
                activity.findViewById<View>(android.R.id.content).tag.toString().contains(TAG)
            ) {
                onResume()
            }
        }

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {}
    }

    constructor(context: Context) : super(context) {
        initView(context, null, 0)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initView(context, attributeSet, 0)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        initView(context, attributeSet, defStyleAttr)
    }

    private fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        if (context is Activity) {
            val contentView = context.findViewById<View>(android.R.id.content)
            if (contentView.tag == null) {
                contentView.tag = " "
            }
            contentView.tag = contentView.tag.toString() + TAG
            context.application.registerActivityLifecycleCallbacks(onContextLifecycleListener)
        }
        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.CountDownTimer, defStyleAttr, 0)
        txtSize = typedArray.getInt(R.styleable.CountDownTimer_t_size, 12).toFloat()
        textColor = typedArray.getColor(R.styleable.CountDownTimer_t_color, Color.WHITE)
        delimiter_day_hour =
            typedArray.getString(R.styleable.CountDownTimer_delimiter_day_hour) ?: ":"
        delimiter_hour_minute =
            typedArray.getString(R.styleable.CountDownTimer_delimiter_hour_minute) ?: ":"
        delimiter_minute_second =
            typedArray.getString(R.styleable.CountDownTimer_delimiter_minute_second) ?: ":"
        unit_day = typedArray.getString(R.styleable.CountDownTimer_unit_str_day) ?: ""
        unit_hour = typedArray.getString(R.styleable.CountDownTimer_unit_str_hour) ?: ""
        unit_minute = typedArray.getString(R.styleable.CountDownTimer_unit_str_minute) ?: ""
        unit_second = typedArray.getString(R.styleable.CountDownTimer_unit_str_second) ?: ""
        format = typedArray.getString(R.styleable.CountDownTimer_format) ?: FORMAT_1
        surfaceHolder = holder
        surfaceHolder?.addCallback(this)
        setBackgroundTransparent()
        isFocusable = true
        moveYpx = sp2px(context, txtSize)
        initPaint()
        setWidth()
        drawInitStatus()
    }

    private fun setWidth() {
        timeFormat(time.toInt())
        textWidth = paint?.measureText(result) ?: 0f
    }

    private fun drawInitStatus() {
        DelayExecutor.doDelay(500, object : OnDelayExecutorListener {
            override fun onFinish() {
                try {
                    canvas = holder.lockCanvas()
                    timeFormat(time.toInt())
                    drawText(canvas)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    canvas?.run {
                        surfaceHolder?.unlockCanvasAndPost(canvas)
                    }
                }
            }
        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var finalWidth = 0
        var finalHeight = 0
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        when (widthMode) {
            MeasureSpec.EXACTLY -> {
                finalWidth = widthSize
            }
            MeasureSpec.AT_MOST -> {
                finalWidth = (textWidth + 2 * txtSize).toInt()
            }
            MeasureSpec.UNSPECIFIED -> {
                finalWidth = (textWidth + 2 * txtSize).toInt()
            }
        }
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        when (heightMode) {
            MeasureSpec.EXACTLY -> {
                finalHeight = heightSize
            }
            MeasureSpec.AT_MOST -> {
                finalHeight = sp2px(context, txtSize + 2).toInt()
            }
            MeasureSpec.UNSPECIFIED -> {
                finalHeight = sp2px(context, txtSize + 2).toInt()
            }
        }
        setMeasuredDimension(finalWidth, finalHeight)
    }

    private fun setBackgroundTransparent() {
        surfaceHolder?.setFormat(PixelFormat.TRANSLUCENT)
        setZOrderOnTop(true)
    }

    private fun initPaint() {
        if (paint == null) {
            paint = Paint()
            paint?.run {
                color = textColor
                style = Paint.Style.FILL
                strokeWidth = txtSize + 1
                textSize = sp2px(context, txtSize)
                isAntiAlias = true
                isFakeBoldText = true
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isAlive = true
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isAlive = false
    }

    fun start(milliSecond: Long, interval: Long, listener: () -> Unit) {
        if (interval < 1000) {
            Toast.makeText(context, "interval must more than 1000", Toast.LENGTH_SHORT).show()
            return
        }
        countDownTimer?.cancel()
        countDownTimer = null
        countDownTimer = object : CountDownTimer(milliSecond, interval) {
            override fun onTick(milliTime: Long) {
                singleThreadExecutor.execute {
                    time = milliTime / 1000
                    timeFormat(time.toInt())
                    runLogic()
                }
            }

            override fun onFinish() {
                listener()
                cancel()
            }
        }.start()
        isFinished = interval != 1000L
    }

    fun staticSetTime(milliSecond: Long) {
        singleThreadExecutor.execute {
            isAlive = true
            time = milliSecond / 1000
            timeFormat(time.toInt())
            runLogic()
        }
    }

    private fun timeFormat(seconds: Int) {
        when (format) {
            FORMAT_1 -> {
                day = seconds / (60 * 60 * 24)
                hour = seconds % (60 * 60 * 24) / (60 * 60)
                minute = seconds % (60 * 60) / 60
                second = seconds % 60
            }
            FORMAT_2 -> {
                day = -1
                hour = (seconds / 60) / 60
                minute = seconds % (60 * 60) / 60
                second = seconds % 60
            }
            FORMAT_3 -> {
                day = -1
                hour = -1
                minute = seconds / 60
                second = seconds % 60
            }
            FORMAT_4 -> {
                day = seconds / (60 * 60 * 24)
                hour = seconds % (60 * 60 * 24) / (60 * 60)
                minute = seconds % (60 * 60) / 60
                delimiter_minute_second = ""
                second = -1
            }
            FORMAT_5 -> {
                day = -1
                hour = (seconds / 60) / 60
                minute = seconds % (60 * 60) / 60
                delimiter_minute_second = ""
                second = -1
            }
            FORMAT_6 -> {
                day = seconds / (60 * 60 * 24)
                hour = seconds % (60 * 60 * 24) / (60 * 60)
                delimiter_hour_minute = ""
                delimiter_minute_second = ""
                minute = -1
                second = -1
            }
        }
        result = setResult(day, hour, minute, second)
    }

    private fun setResult(day: Int, hour: Int, minute: Int, seconds: Int): String {
        tmpStr.clear()
        if (day != -1) {
            tmpStr.append("$day$unit_day$delimiter_day_hour")
        }
        if (hour != -1) {
            if (hour < 10) {
                tmpStr.append("0$hour")
            } else {
                tmpStr.append(hour)
            }
            tmpStr.append("$unit_hour$delimiter_hour_minute")
        }
        if (minute != -1) {
            if (minute < 10) {
                tmpStr.append("0$minute")
            } else {
                tmpStr.append(minute)
            }
            tmpStr.append("$unit_minute$delimiter_minute_second")
        }
        if (seconds != -1) {
            if (seconds < 10) {
                tmpStr.append("0$seconds")
            } else {
                tmpStr.append(seconds)
            }
            tmpStr.append(unit_second)
        }
        return tmpStr.toString()
    }

    fun onResume() {
        if (isFinished) {
            DelayExecutor.doDelay(500, object : OnDelayExecutorListener {
                override fun onFinish() {
                    try {
                        canvas = holder.lockCanvas()
                        timeFormat(time.toInt())
                        drawText(canvas)
                    } catch (e: Exception) {

                    } finally {
                        canvas?.run {
                            surfaceHolder?.unlockCanvasAndPost(canvas)
                        }
                    }
                }
            })
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        closeView()
        if (context is Activity) {
            (context as Activity).application.unregisterActivityLifecycleCallbacks(
                onContextLifecycleListener
            )
        }
    }

    private fun closeView() {
        try {
            surfaceHolder?.removeCallback(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        countDownTimer?.cancel()
        isAlive = false
    }

    fun runLogic() {
        if (!isAlive) {
            return
        }
        try {
            canvas = holder.lockCanvas()
            drawText(canvas)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            canvas?.run {
                surfaceHolder?.unlockCanvasAndPost(canvas)
            }
        }
    }

    private fun drawText(canvas: Canvas?) {
        //执行具体的绘制操作
        canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        paint?.let {
            canvas?.drawText(result, txtSize, moveYpx, it)
        }
    }

    private fun sp2px(context: Context, sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        )
    }

    interface OnCountDownFinishedListener {
        fun onFinished()
    }

    fun finishCountDown() {
        finishedListener?.onFinished()
    }
}