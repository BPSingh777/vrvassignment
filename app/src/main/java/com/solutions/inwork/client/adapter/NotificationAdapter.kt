package com.solutions.inwork.client.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.solutions.inwork.Admin.dataclasses.AdminGetNotifications
import com.solutions.inwork.R
import com.solutions.inwork.client.dataclasses.ClientNotification

class NotificationAdapter<T>(var notificationList: List<T>) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val data = notificationList[position]
        // Customize the binding logic based on the data class type
        if (data is ClientNotification) {
            // Handle client notification data binding
            val clientNotification = data as ClientNotification
            holder.notification.text = clientNotification.notification
            holder.title.text = clientNotification.title
            holder.notificationTimestamp.text = clientNotification.notification_date
        } else if (data is AdminGetNotifications) {
            // Handle admin notification data binding
            val adminNotification = data as AdminGetNotifications
            holder.notification.text = adminNotification.notification
            holder.title.text = adminNotification.title
            holder.notificationTimestamp.text = adminNotification.notification_date
        }
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.TitleTextview)
        val notification: TextView = itemView.findViewById(R.id.NotificationTextview)
        val notificationTimestamp : TextView = itemView.findViewById(R.id.notificationTimeStamp)
    }
}
