package com.gzdong.countdowntimer.utils

import android.os.CountDownTimer

class DelayExecutor {
    companion object {

        fun doDelay(time: Long, listener: OnDelayExecutorListener) {
            DelayCountDownTimer(time, 1000, listener).start()
        }

    }
}
interface OnDelayExecutorListener {
    fun onFinish()
}

class DelayCountDownTimer(
    millisInFuture: Long,
    countDownInterval: Long,
    private val listener: OnDelayExecutorListener
) : CountDownTimer(millisInFuture, countDownInterval) {

    override fun onTick(millisUntilFinished: Long) {

    }

    override fun onFinish() {
        listener.onFinish()
    }
}