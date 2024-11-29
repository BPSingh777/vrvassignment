package com.solutions.inwork.client.adapter

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.recyclerview.widget.RecyclerView
import com.solutions.inwork.R
import com.solutions.inwork.client.dataclasses.PermissionItem

class PermissionAdapter(private val permissions: List<PermissionItem>,
                        private val dialog: Dialog
) :
    RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.permission_card_layout, parent, false)
        return PermissionViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
        val permission = permissions[position]
        holder.permissionName.text = permission.name
        holder.permissionDescription.text = permission.description

        holder.grantButton.setOnClickListener {
            requestPermission(holder.itemView.context, permission.name)
            dialog.dismiss() // Dismiss the dialog when the "Grant" button is clicked
        }
    }

    override fun getItemCount(): Int {
        return permissions.size
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission(context: Context, permissionName: String) {
        // Handle the permission request using ActivityCompat.requestPermissions()
        when (permissionName) {
            "Location" -> ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            "Background Location" -> ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                1003
            )
            "ScreenTime" -> {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                context.startActivity(intent)
            }
            "Phone" -> ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.CALL_PHONE),
                1007
            )
            "Storage Permission" -> ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE ,
                    Manifest.permission.READ_EXTERNAL_STORAGE),
                1001
            )
            "Notifications" -> ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1009
            )


            // Add other permissions as needed
        }
    }

    class PermissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val permissionName: TextView = itemView.findViewById(R.id.permissionName)
        val permissionDescription: TextView = itemView.findViewById(R.id.permissionDescription)
        val grantButton: Button = itemView.findViewById(R.id.grantButton)
    }

    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        const val LOCATION_PERMISSION_REQUEST_CODE = 1002
        // Add other permission request codes as needed
    }
}
