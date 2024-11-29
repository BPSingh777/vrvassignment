package com.solutions.inwork

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat

class BootReciever : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start your foreground service here
            val serviceIntent2 = Intent(context, GpsNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                 ContextCompat.startForegroundService(context,serviceIntent2)
                GeofenceClass(context).fetchCoordinates()
            }else{
              //  startService(serviceIntent)
            }


        }
    }
}
