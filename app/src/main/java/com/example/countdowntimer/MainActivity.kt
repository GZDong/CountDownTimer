package com.example.countdowntimer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.gzdong.countdowntimer.view.Stopwatch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn_start).setOnClickListener {
            findViewById<Stopwatch>(R.id.stopWatch).start()
        }
        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            findViewById<Stopwatch>(R.id.stopWatch).stop { time, subTime ->
                Toast.makeText(this, "$time.$subTime", Toast.LENGTH_LONG).show()
            }
        }
        findViewById<Button>(R.id.btn_reset).setOnClickListener {
            findViewById<Stopwatch>(R.id.stopWatch).reset()
        }
    }
}