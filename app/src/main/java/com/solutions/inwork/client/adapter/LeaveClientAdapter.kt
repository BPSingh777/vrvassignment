package com.solutions.inwork.client.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.solutions.inwork.Admin.adapters.LeaveAdapter
import com.solutions.inwork.Admin.dataclasses.LeaveData
import com.solutions.inwork.R

class LeaveClientAdapter(var LeaveList : List<LeaveData>): RecyclerView.Adapter<LeaveClientAdapter.LeaveViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LeaveViewHolder {
       val itemView = LayoutInflater.from(parent.context).inflate(R.layout.leave_data_item,parent,false)
        return LeaveViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LeaveViewHolder, position: Int) {
        val leavedata = LeaveList[position]

        holder.EmployeeIdTextView.text = "Leave Date : ${leavedata.leave_date}"
        holder.leavetilldataTextView.text = "Leave till date : ${leavedata.till_date}"
        holder.EmployeeNameTextView.text = "Applied On : ${leavedata.time_stamp}"
        holder.designationTextView.visibility = View.GONE
      if (leavedata.approved == 1){
          holder.leavestatus.text = "Leave Status : Approved"
      } else if(leavedata.approved == 0){
          holder.leavestatus.text = "Leave Status : Declined"
      } else{
          holder.leavestatus.text = "Leave Status : Pending"
      }
        holder.leavedataTextView.text = "Approval/Declined timestamp : ${leavedata.approval_time_stamp}"
        holder.leaveReasonTextView.text = "Leave Reason: ${leavedata.leave_reason}"

        holder.btnlayout.visibility = View.GONE
    }



    override fun getItemCount(): Int {
        return LeaveList.size
    }


    class LeaveViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val EmployeeIdTextView: TextView = itemView.findViewById(R.id.textViewEmployeeId)
        val EmployeeNameTextView: TextView = itemView.findViewById(R.id.textViewEmployeeName)
        val designationTextView: TextView = itemView.findViewById(R.id.textViewDesignation)
        val leavedataTextView: TextView = itemView.findViewById(R.id.textViewLeaveDate)
        val leavetilldataTextView: TextView = itemView.findViewById(R.id.textViewLeavetillDate)
        val leaveReasonTextView: TextView = itemView.findViewById(R.id.textViewLeaveReason)
        val btnlayout : LinearLayout = itemView.findViewById(R.id.btnLayout)
        val leavestatus : TextView =itemView.findViewById(R.id.textViewLeavestatus)




    }
}