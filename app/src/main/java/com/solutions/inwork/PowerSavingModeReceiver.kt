package com.solutions.inwork

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService

class PowerSavingModeReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != null && intent.action == PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) {

            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

            if(powerManager != null){
                if(!powerManager.isPowerSaveMode || !powerManager.isLowPowerStandbyEnabled){
                    Log.d("BatterySaver","if")
                    startForegroundService(context)
                }
                else{
                    Log.d("BatterySaver","else")
                }
            }else{
                Log.d("BatterySaver","else2")
            }


        }
    }

    private fun startForegroundService(context: Context) {
        val serviceIntent = Intent(context, GpsNotificationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(context,serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
