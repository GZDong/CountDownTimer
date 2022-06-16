package com.gzdong.countdowntimer.utils

import android.os.CountDownTimer
import com.gzdong.countdowntimer.view.GCountDownTimer
import java.util.*

class ListCountDownTimerDispatcher(timeList: List<Long>) {

    private val listMap = mutableMapOf<GCountDownTimer, Long>()
    private var baseTime = 0L
    private var countDownTimer: CountDownTimer? = null

    init {
        baseTime = Collections.max(timeList)
        countDownTimer = object : CountDownTimer(baseTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                for ((timer, time) in listMap) {
                    if (time == -1L || time == 0L) {
                        continue
                    }
                    val realTimeUntilFinished = millisUntilFinished - (baseTime - time)
                    if (realTimeUntilFinished > 0) {
                        timer.staticSetTime(realTimeUntilFinished)
                    } else {
                        timer.finishCountDown()
                        listMap[timer] = -1L
                    }
                }
            }

            override fun onFinish() {
                listMap.clear()
            }
        }.start()
    }

    fun registerTimer(countDownTimer: GCountDownTimer, time: Long) {
        listMap[countDownTimer] = time
    }
}