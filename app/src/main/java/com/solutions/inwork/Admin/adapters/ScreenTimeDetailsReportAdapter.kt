package com.solutions.inwork.Admin.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.solutions.inwork.Admin.dataclasses.Screentimedata
import com.solutions.inwork.R
import java.text.SimpleDateFormat
import java.util.Locale

class ScreenTimeDetailsReportAdapter(private var screenList: List<Screentimedata>):RecyclerView.Adapter<ScreenTimeDetailsReportAdapter.ScreenViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScreenViewHolder {
       val itemView = LayoutInflater.from(parent.context).inflate(R.layout.report_screen_item,parent,false)
        return ScreenViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ScreenViewHolder, position: Int) {
        val alertdetails = screenList[position]
      //  Log.d("screeninfo",alertdetails.toString())
        val timestamp = alertdetails.time_stamp
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = dateFormat.parse(timestamp)
        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        holder.DateScreen.text = formattedDate
        holder.whatsAppScreen.text = alertdetails.whatsapp_duration
        holder.FacebookScreen.text = alertdetails.facebook_duration
        holder.GameScreen.text = alertdetails.games_duration
        holder.InstagramScreen.text = alertdetails.instagram_duration
        holder.TwitterScreen.text = alertdetails.twitter_duration
        holder.callsScreen.text = alertdetails.calls_duration
        holder.othersScreen.text = alertdetails.others_duration
        holder.NewsScreen.text = alertdetails.news_duration
        holder.SnScreen.text = alertdetails.sno

        // Calculate the total duration
        val totalDuration = calculateTotalDuration(
            alertdetails.whatsapp_duration,
            alertdetails.facebook_duration,
            alertdetails.games_duration,
            alertdetails.instagram_duration,
            alertdetails.twitter_duration,
            alertdetails.calls_duration,
            alertdetails.others_duration
        )

        holder.Total.text = formatDuration(totalDuration)
    }


 //   Log.d("avgmonthlyduration",averageMonthlyDuration.toString())


    override fun getItemCount(): Int {
        Log.d("screeninfo",screenList.toString())
        return screenList.size
    }


    class ScreenViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val whatsAppScreen : TextView = itemView.findViewById(R.id.whatsAppScreenTimetxt)
        val InstagramScreen : TextView = itemView.findViewById(R.id.InstagramScreenTimetxt)
        val FacebookScreen : TextView = itemView.findViewById(R.id.FacebookScreenTimetxt)
        val TwitterScreen : TextView = itemView.findViewById(R.id.twitterScreenTimetxt)
        val callsScreen : TextView = itemView.findViewById(R.id.CallsScreenTimetxt)
        val GameScreen : TextView = itemView.findViewById(R.id.GameScreenTimetxt)
        val NewsScreen : TextView = itemView.findViewById(R.id.NewsScreenTimetxt)
        val Total : TextView = itemView.findViewById(R.id.TotalScreenTimetxt)
        val othersScreen : TextView = itemView.findViewById(R.id.OthersScreenTimetxt)
        val DateScreen : TextView = itemView.findViewById(R.id.dateScreenTimetxt)
        val SnScreen : TextView = itemView.findViewById(R.id.seriallNumberScreenTimetxt)
    }

    fun calculateTotalDuration(vararg durations: String?): Long {
        var totalDuration = 0L

        for (duration in durations) {
            duration?.let {
                val parts = it.split(":")
                if (parts.size == 3) {
                    val hours = parts[0].toLongOrNull() ?: 0
                    val minutes = parts[1].toLongOrNull() ?: 0
                    val seconds = parts[2].toLongOrNull() ?: 0

                    val durationInSeconds = hours * 3600 + minutes * 60 + seconds
                    totalDuration += durationInSeconds
                }
            }
        }

        return totalDuration
    }

    fun formatDuration(durationInSeconds: Long): String {
        val hours = durationInSeconds / 3600
        val minutes = (durationInSeconds % 3600) / 60
        val seconds = durationInSeconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }


}

