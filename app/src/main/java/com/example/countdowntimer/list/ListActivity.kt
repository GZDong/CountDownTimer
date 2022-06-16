package com.example.countdowntimer.list

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.countdowntimer.R
import com.example.countdowntimer.adapter.ListAdapter
import com.example.countdowntimer.entity.ListItemInfo
import com.gzdong.countdowntimer.utils.ListCountDownTimerDispatcher

class ListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        val data = mutableListOf<ListItemInfo>()
        for (i in 0..50) {
            data.add(ListItemInfo("name$i", (i + 1) * 6000L))
        }
        val listCountDownTimerDispatcher = ListCountDownTimerDispatcher(data.map { it.time })
        val adapter = ListAdapter(this, data, listCountDownTimerDispatcher)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        val list = findViewById<RecyclerView>(R.id.list)
        list.layoutManager = layoutManager
        list.adapter = adapter
    }
}