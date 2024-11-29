package com.solutions.inwork.client.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.solutions.inwork.R
import com.solutions.inwork.client.dataclasses.DataModel

class DataAdapter(private val dataList: List<DataModel>) : RecyclerView.Adapter<DataAdapter.DataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notice_item_data, parent, false)
        return DataViewHolder(view)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        val data = dataList[position]

        holder.companyIdTextView.text = data.company_id
        holder.companyNameTextView.text = data.company_name
        holder.noticeTextView.text = data.notice
        holder.timeStampTextView.text = data.time_stamp
        holder.GreetText.text = data.title
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val companyIdTextView: TextView = itemView.findViewById(R.id.companyIdTextView)
        val companyNameTextView: TextView = itemView.findViewById(R.id.companyNameTextView)
        val noticeTextView: TextView = itemView.findViewById(R.id.noticeTextView)
        val timeStampTextView: TextView = itemView.findViewById(R.id.timeStampTextView)
        val GreetText : TextView = itemView.findViewById(R.id.GreetText)
    }
}
