package com.solutions.inwork.Admin.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.solutions.inwork.Admin.dataclasses.CheckInAndOut
import com.solutions.inwork.R

class AttendanceDetailsAdapter(private val attendancealertslist : List<CheckInAndOut>): RecyclerView.Adapter<AttendanceDetailsAdapter.AttendanceDetailsViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AttendanceDetailsViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_inout_data, parent, false)
        return AttendanceDetailsViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: AttendanceDetailsViewHolder,
        position: Int
    ) {
        val currentAlertDetails = attendancealertslist[position]
        holder.alertTimeSpentTextView.text = currentAlertDetails.timeSpent
        holder.alertCheckOutTextView.text = currentAlertDetails.checkOutTime
        holder.alertserialnumberTextView.text = currentAlertDetails.sno
        holder.alertCheckInTextView.text = currentAlertDetails.checkInTime
        holder.alertdateTextview.text = currentAlertDetails.date
        holder.alertRemarks.text = currentAlertDetails.remarks
    }

    override fun getItemCount(): Int {
        return attendancealertslist.size
    }



    class AttendanceDetailsViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView){
        val alertCheckOutTextView: TextView = itemView.findViewById(R.id.AttendanceCheckOuttxt)
        val alertCheckInTextView: TextView = itemView.findViewById(R.id.AttendanceCheckIntxt)
        val alertserialnumberTextView : TextView = itemView.findViewById(R.id.AttendanceserialNumber)
        val alertdateTextview : TextView = itemView.findViewById(R.id.Attendancedatetxt)
        val alertTimeSpentTextView : TextView = itemView.findViewById(R.id.AttendanceTimeSpenttxt)
        val alertRemarks : TextView = itemView.findViewById(R.id.AttendanceRemarks)
    }
}