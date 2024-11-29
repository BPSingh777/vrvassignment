package com.solutions.inwork.Admin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.solutions.inwork.Admin.dataclasses.AlertDetails
import com.solutions.inwork.PdfClass
import com.solutions.inwork.R

class AlertDetailsAdapter(private val alertDetailsList: List<AlertDetails>) :
    RecyclerView.Adapter<AlertDetailsAdapter.AlertDetailsViewHolder>() {

    class AlertDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val alertTitleTextView: TextView = itemView.findViewById(R.id.titletxt)
        val alertDescriptionTextView: TextView = itemView.findViewById(R.id.descriptiontxt)
        val alertserialnumberTextView : TextView = itemView.findViewById(R.id.serialNumbertxt)
        val alertdateTextview : TextView = itemView.findViewById(R.id.datetxt)
        val alerttimeTextView : TextView = itemView.findViewById(R.id.timetxt)
        val alertRemarksTextView : TextView = itemView.findViewById(R.id.remarkstxt)
        // Add any other views you need to bind data to
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertDetailsViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_alert_details, parent, false)
        return AlertDetailsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AlertDetailsViewHolder, position: Int) {
        val currentAlertDetails = alertDetailsList[position]
        holder.alertTitleTextView.text = currentAlertDetails.alertTitle
        holder.alertDescriptionTextView.text = currentAlertDetails.alertDescription
        holder.alertserialnumberTextView.text = currentAlertDetails.sno
        holder.alerttimeTextView.text = currentAlertDetails.alertTime
        holder.alertRemarksTextView.text = currentAlertDetails.alertRemarks
        holder.alertdateTextview.text = currentAlertDetails.alertDate
        // Bind other data to the views in the ViewHolder
    }

    override fun getItemCount(): Int {
        return alertDetailsList.size
    }
}
