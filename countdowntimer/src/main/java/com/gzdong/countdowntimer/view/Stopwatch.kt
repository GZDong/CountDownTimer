package com.gzdong.countdowntimer.view

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.gzdong.countdowntimer.R
import com.gzdong.countdowntimer.utils.DelayExecutor
import com.gzdong.countdowntimer.utils.OnDelayExecutorListener
import java.util.*

private const val TAG = "timerActivity"

class Stopwatch : SurfaceView, SurfaceHolder.Callback {
    private var surfaceHolder: SurfaceHolder? = null
    private var canvas: Canvas? = null
    private var paint: Paint? = null
    private var timeSecondNum = 0
    private var timeSubSecondNum = 0
    private var isAlive = false
    private var timer: Timer? = null
    private var moveYpx = 0f
    private var isFinished = true
    private var unit = "s"
    private var txtSize = 12f
    private var textColor = 0
    private var textWidth = 0f
    private var isShowInit = false
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
            contentView.tag = TAG
            context.application.registerActivityLifecycleCallbacks(onContextLifecycleListener)
        }
        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.Stopwatch, defStyleAttr, 0)
        txtSize = typedArray.getInt(R.styleable.Stopwatch_txtSize, 12).toFloat()
        textColor = typedArray.getColor(R.styleable.Stopwatch_txtColor, Color.WHITE)
        val isShowUnit = typedArray.getBoolean(R.styleable.Stopwatch_isShowUnit, true)
        isShowInit = typedArray.getBoolean(R.styleable.Stopwatch_isShowInit, false)
        if (!isShowUnit) {
            unit = ""
        }
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
        textWidth = if (timeSecondNum > 99) {
            paint?.measureText("$timeSecondNum.00$unit") ?: 0f
        } else {
            paint?.measureText("00.00$unit") ?: 0f
        }
    }

    private fun drawInitStatus() {
        if (!isShowInit) {
            return
        }
        DelayExecutor.doDelay(500, object : OnDelayExecutorListener {
            override fun onFinish() {
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

    fun start(second: Int = 0, subSecond: Int = 0) {
        timeSecondNum = second
        timeSubSecondNum = subSecond
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runLogic()
            }
        }, 0, 30)
        isFinished = false
    }

    fun stop(listener: (Int, Int) -> Unit) {
        timer?.cancel()
        timeSubSecondNum += Random().nextInt(3)
        runLogic()
        listener(timeSecondNum, timeSubSecondNum)
        isFinished = true
    }

    fun reset() {
        timer?.cancel()
        timeSecondNum = 0
        timeSubSecondNum = 0
        setWidth()
        requestLayout()
        canvas = holder.lockCanvas()
        try {
            canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            canvas?.drawText("", 0f, 0f, Paint())
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            canvas?.run {
                surfaceHolder?.unlockCanvasAndPost(canvas)
            }
        }
        isFinished = true
        drawInitStatus()
    }

    fun getTime(listener: (Int, Int) -> Unit) {
        listener(timeSecondNum, timeSubSecondNum)
    }

    fun onResume() {
        if (isFinished) {
            DelayExecutor.doDelay(500, object : OnDelayExecutorListener {
                override fun onFinish() {
                    try {
                        canvas = holder.lockCanvas()
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
        timer?.cancel()
        isAlive = false
    }

    fun runLogic() {
        if (!isAlive) {
            return
        }
        try {
            if (timeSubSecondNum < 99) {
                timeSubSecondNum += 3
            } else {
                val tmp = timeSecondNum
                timeSecondNum += 1
                timeSubSecondNum = 0
                if (timeSecondNum.toString().length > tmp.toString().length
                    && timeSecondNum.toString().length > 2
                ) {
                    setWidth()
                    post { requestLayout() }
                }
            }
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
            if (timeSecondNum < 10) {
                if (timeSubSecondNum < 10) {
                    canvas?.drawText(
                        "0$timeSecondNum.0$timeSubSecondNum$unit", txtSize,
                        moveYpx, it
                    )
                } else {
                    canvas?.drawText(
                        "0$timeSecondNum.$timeSubSecondNum$unit", txtSize,
                        moveYpx, it
                    )
                }
            } else {
                if (timeSubSecondNum < 10) {
                    canvas?.drawText(
                        "$timeSecondNum.0$timeSubSecondNum$unit", txtSize,
                        moveYpx, it
                    )
                } else {
                    canvas?.drawText(
                        "$timeSecondNum.$timeSubSecondNum$unit", txtSize,
                        moveYpx, it
                    )
                }
            }
        }
    }

    private fun sp2px(context: Context, sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        )
    }
}