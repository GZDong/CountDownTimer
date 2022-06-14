package com.gzdong.countdowntimer.utils

import android.content.Context
import android.widget.Toast

class TestUtils {

    fun showTips(context: Context) {
        Toast.makeText(context, "Here is tiger", Toast.LENGTH_LONG).show()
    }
}