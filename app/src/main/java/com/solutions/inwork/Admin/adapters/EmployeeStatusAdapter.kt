package com.solutions.inwork.Admin.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.solutions.inwork.Admin.dataclasses.EmployeeStatus
import com.solutions.inwork.R

class EmployeeStatusAdapter(private var EmpStatusList: List<EmployeeStatus>):RecyclerView.Adapter<EmployeeStatusAdapter.StatusViewHolder>() {

    fun submitList(newList: List<EmployeeStatus>) {
        EmpStatusList = newList
        notifyDataSetChanged()
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int
    ): StatusViewHolder {
      val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_status_employee,parent,false)
        return StatusViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val data = EmpStatusList[position]
        holder.EmployeeId.text = data.current_status
        holder.EmployeeName.text = data.employee_id
        holder.status.text = data.time_stamp
        if(data.current_status == "CHECKED IN"){
            holder.EmployeeId.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.GREEN))
        }else{
            holder.EmployeeId.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.RED))
        }
    }

    override fun getItemCount(): Int {
        return EmpStatusList.size
    }

    class StatusViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val EmployeeName : TextView = itemView.findViewById(R.id.EmployeeNameStatus)
        val EmployeeId : TextView = itemView.findViewById(R.id.EmployeeIdStatus)
        val status : TextView = itemView.findViewById(R.id.EmployeeStatus)
    }
}