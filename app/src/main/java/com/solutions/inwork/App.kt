package com.solutions.inwork

import android.app.Application
import android.content.Context
import android.os.BatteryManager

class App : Application() {




    override fun onCreate() {
        super.onCreate()


//        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
//        batteryManager.setBatteryOptimizationPolicy(packageName, BatteryManager.OPTIMIZATION_POLICY_IGNORE)
////
//        val intent = Intent(this, GpsNotificationService::class.java)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            this.startForegroundService(intent)
//        } else {
//            this.startService(intent)
//        }


//        val serviceIntent2 = Intent(this, BatteryService::class.java)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            ContextCompat.startForegroundService(this,serviceIntent2)
//        } else {
//           startService(serviceIntent2)
//        }

    }

    override fun onTerminate() {
        super.onTerminate()

        // Unregister the GPS status receiver
      //  unregisterReceiver(gpsStatusReceiver)
    }

}