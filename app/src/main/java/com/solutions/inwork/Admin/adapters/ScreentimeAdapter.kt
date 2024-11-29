package com.solutions.inwork.Admin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.solutions.inwork.Admin.dataclasses.Screentimedata
import com.solutions.inwork.R

class ScreentimeAdapter(private var ScreenList : List<Screentimedata>): RecyclerView.Adapter<ScreentimeAdapter.ScreenViewHolder>() {

    fun submitList(newList: List<Screentimedata>) {
        ScreenList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ScreenViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.screentime_details_item,parent,false)
        return ScreenViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScreenViewHolder, position: Int) {
       val ScreenData = ScreenList[position]
        holder.bind(ScreenData)
    }

    override fun getItemCount(): Int {
      return ScreenList.size
    }
   class ScreenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
       private val companyIdTextView: TextView = itemView.findViewById(R.id.companyIdText)
       private val companyNameTextView: TextView = itemView.findViewById(R.id.companyNameText)
       private val employeeIdTextView: TextView = itemView.findViewById(R.id.employeeIdText)
       private val employeeNameTextView: TextView = itemView.findViewById(R.id.employeeNameText)
       private val designationTextView: TextView = itemView.findViewById(R.id.designationText)
       private val workStartTimeTextView: TextView = itemView.findViewById(R.id.workStartTimeText)
       private val viewMoreButton: TextView = itemView.findViewById(R.id.viewMoreButton)
       private val expandedDetailsLayout: LinearLayout = itemView.findViewById(R.id.expandedDetailsLayout)
       private val workEndTimeTextView: TextView = itemView.findViewById(R.id.workEndTimeText)
       private val whatsappDurationTextView: TextView = itemView.findViewById(R.id.whatsappDurationText)
       private val facebookDurationTextView: TextView = itemView.findViewById(R.id.facebookDurationText)
       private val instagramDurationTextView: TextView = itemView.findViewById(R.id.instagramDurationText)
       private val twitterDurationTextView: TextView = itemView.findViewById(R.id.twitterDurationText)
       private val newsDurationTextView: TextView = itemView.findViewById(R.id.newsDurationText)
       private val gamesDurationTextView: TextView = itemView.findViewById(R.id.gamesDurationText)
       private val callsDurationTextView: TextView = itemView.findViewById(R.id.callsDurationText)
       private val othersDurationTextView: TextView = itemView.findViewById(R.id.othersDurationText)
       private val dateTextView: TextView = itemView.findViewById(R.id.Datetext)
       private val viewLessButton: TextView = itemView.findViewById(R.id.viewLessButton)

       fun bind(ScreenData: Screentimedata) {
           companyIdTextView.text = "Company ID: ${ScreenData.company_id}"
           companyNameTextView.text = "Company Name: ${ScreenData.company_name}"
           employeeIdTextView.text = "Employee ID: ${ScreenData.employee_id}"
           employeeNameTextView.text = "Employee Name: ${ScreenData.employee_name}"
           designationTextView.text = "Designation: ${ScreenData.designation}"


           viewMoreButton.setOnClickListener {
               expandedDetailsLayout.visibility = View.VISIBLE
               viewMoreButton.visibility = View.GONE
           }

           viewLessButton.setOnClickListener {
               expandedDetailsLayout.visibility = View.GONE
               viewMoreButton.visibility = View.VISIBLE
           }
           workStartTimeTextView.text = "Work Start Time: ${ScreenData.work_start_time}"
           workEndTimeTextView.text = "Work End Time: ${ScreenData.work_end_time}"
           whatsappDurationTextView.text = "WhatsApp Duration: ${ScreenData.whatsapp_duration}"
           facebookDurationTextView.text = "Facebook Duration: ${ScreenData.facebook_duration}"
           instagramDurationTextView.text = "Instagram Duration: ${ScreenData.instagram_duration}"
           twitterDurationTextView.text = "Twitter Duration: ${ScreenData.twitter_duration}"
           newsDurationTextView.text = "News Duration: ${ScreenData.news_duration}"
           gamesDurationTextView.text = "Games Duration: ${ScreenData.games_duration}"
           callsDurationTextView.text = "Calls Duration: ${ScreenData.calls_duration}"
           othersDurationTextView.text = "Others Duration: ${ScreenData.others_duration}"
           dateTextView.text = "Date: ${ScreenData.time_stamp}"
       }
   }
}