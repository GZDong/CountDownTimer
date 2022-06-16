package com.example.countdowntimer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.countdowntimer.R
import com.example.countdowntimer.entity.ListItemInfo
import com.gzdong.countdowntimer.utils.ListCountDownTimerDispatcher
import com.gzdong.countdowntimer.view.GCountDownTimer

class ListAdapter(
    private val context: Context,
    private val list: List<ListItemInfo>,
    private val listCountDownTimerDispatcher: ListCountDownTimerDispatcher
) :
    RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_view_list, parent, false)
        return ListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.name.text = list[position].name
        holder.timer.finishedListener = object : GCountDownTimer.OnCountDownFinishedListener {
            override fun onFinished() {
                Toast.makeText(
                    context,
                    "timer_${holder.name.text}" + "finished",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        listCountDownTimerDispatcher.registerTimer(holder.timer, list[position].time)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timer: GCountDownTimer = itemView.findViewById(R.id.timer)
        val name: TextView = itemView.findViewById(R.id.name)
    }
}