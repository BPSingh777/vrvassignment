package com.solutions.inwork.client

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder

class GeofenceForegroundService : Service() {
    private lateinit var geofenceBroadcastReceiver: GeofenceBroadcastReceiver

    override fun onCreate() {
        super.onCreate()
        geofenceBroadcastReceiver = GeofenceBroadcastReceiver()
        registerGeofenceBroadcastReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterGeofenceBroadcastReceiver()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun registerGeofenceBroadcastReceiver() {
        val intentFilter = IntentFilter(GeofenceBroadcastReceiver.ACTION_GEOFENCE_EVENT)
        registerReceiver(geofenceBroadcastReceiver, intentFilter)
    }

    private fun unregisterGeofenceBroadcastReceiver() {
        unregisterReceiver(geofenceBroadcastReceiver)
    }
}
