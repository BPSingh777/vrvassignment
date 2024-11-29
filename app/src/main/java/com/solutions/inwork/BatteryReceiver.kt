package com.solutions.inwork

import android.Manifest
import android.app.ActivityManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.BatteryManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.solutions.inwork.client.GeofenceBroadcastReceiver
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject


class BatteryReceiver : BroadcastReceiver() {
    private var bool = true
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BatteryP","enter" )

        if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
            val batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPercentage = batteryLevel * 100 / batteryScale
            if (batteryPercentage < 22) {
                // Display notification here
                val sf = context.getSharedPreferences("BatteryNotification",Context.MODE_PRIVATE)
                val isSent = sf.getBoolean("BatterySent",true)
                Log.d("Battery212",isSent.toString())
                if (isSent){
                    lastKnownLocation(context)
                    with(sf.edit()){
                        putBoolean("BatterySent",false)
                        apply()
                    }
                }
                Log.d("BatteryP","Low")
            }else{

                if(!isForegroundServiceRunning(context)){
                    ContextCompat.startForegroundService(context,Intent(context, GpsNotificationService::class.java))
                }

                val sf = context.getSharedPreferences("BatteryNotification",Context.MODE_PRIVATE)
                sf.edit().putBoolean("BatterySent",true).apply()
                Log.d("Battery213",sf.getBoolean("Work_end_sent",false).toString())
              //  bool = true
            }

            Log.d("BatteryP","enter ${isForegroundServiceRunning(context)}" )
        }
    }


    private fun isForegroundServiceRunning(context: Context): Boolean {
        val className = GpsNotificationService::class.java.name
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

        if (activityManager != null) {
            val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)
            for (serviceInfo in runningServices) {
                if (className == serviceInfo.service.className) {
                    return true
                }
            }
        }
        return false
    }


    fun lastKnownLocation(context: Context){
        // Request last known location
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(
               context ,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Check if location is available and recent enough
                if (location != null && isLocationRecent(location)) {
                    // Save the last known location to persistent storage

                    saveLastKnownLocation(context,location)

                }
                else{
                    getAdminFCM("Battery Situation","Battery is low",context)
                }
            }
            .addOnFailureListener {
                getAdminFCM("Battery Situation","Battery is low",context)
            }
    }

    // Check if the location is recent and accurate enough for your requirements
    fun isLocationRecent(location: Location?): Boolean {
        if (location == null) {
          //  getAdminFCM("Battery Situation","Battery is low",context)
            return false
        }

        val currentTimeMillis = System.currentTimeMillis()
        val locationTimeMillis = location.time

        // Define a time threshold (e.g., 5 minutes) for considering the location as recent
        val timeThreshold = 5 * 60 * 1000 // 5 minutes in milliseconds

        return (currentTimeMillis - locationTimeMillis) <= timeThreshold
    }

    // Save the last known location in a persistent storage mechanism
    fun saveLastKnownLocation(context: Context, location: Location?) {
        val sharedPreferences = context.getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        if (location != null) {
            // Save the location coordinates and other relevant data
            editor.putFloat("lastKnownLatitude", location.latitude.toFloat())
            editor.putFloat("lastKnownLongitude", location.longitude.toFloat())
            editor.putLong("lastKnownTime", location.time)
          //  batteryBroadcast()
            getAdminFCM("Battery Situation","Battery is low Last Known Coordinates ${location.latitude}  ${location.longitude} ",context)
            Log.d("LocationBattery","${location.latitude}  ${location.longitude}")


        } else {
            getAdminFCM("Battery Situation","Battery is low",context)

            // Clear the saved location data
            editor.remove("lastKnownLatitude")
            editor.remove("lastKnownLongitude")
            editor.remove("lastKnownTime")
            Log.d("LocationBattery","null")
        }

        editor.apply()
    }

   private fun GpsNotification(
        title: String,
        message: String,
        adminFcmList: List<String>,
        context: Context
    ) {
        val url = "https://fcm.googleapis.com/fcm/send"
        val notificationObject = JSONObject()
        notificationObject.put("title", title)
        notificationObject.put("message", message)



        for (adminFcm in adminFcmList) {
            val jsonObject = JSONObject()
            jsonObject.put("data", notificationObject)
            jsonObject.put("to", adminFcm)

            Log.d("fcmgps", adminFcm)
            val request: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, jsonObject,
                { response ->
                    // Handle the response here
                    Log.d("gpsstatus", "success")

                    //  Toast.makeText(context, "GPS Successful", Toast.LENGTH_SHORT).show()
                },
                { error ->
                    // Handle the error here
                    Log.d("gpsstatus", "fail")
                    // Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] =
                        "key=AAAAa8gk1zc:APA91bEXlVMS52U7wSCnoNjRwmS0SsZWC__LZIh3cazVJDB7U_nN3x8VvvLR8T2YOckUMuXD8FB66v5pNTLqILQUFTecetIE-RMpzGyJCM-NlqjVOv6n3jmG_xdjxTzphB82Yf41kP-k"
                    headers["Content-Type"] = "application/json"
                    return headers
                }
            }
            Volley.newRequestQueue(context).add(request)
        }
    }


  private  fun getAdminFCM(title: String, message: String, context: Context) {

        val sharedPreferences = context.getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val employeeName = sharedPreferences.getString("name", null)
        val companyID = sharedPreferences.getString("company_id", null)
        Log.d("credentials","$employeeName $companyID")


        val firestore = FirebaseFirestore.getInstance()
        val adminCollection = firestore.collection("admin")

        adminCollection.whereEqualTo("company_id", companyID)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    if (querySnapshot != null && !querySnapshot.isEmpty) {

                        val notification = GeofenceBroadcastReceiver()
                        notification.postNotification(context, "${employeeName}'s $message", title)

                        val fcmList = mutableListOf<String>()
                        for (documentSnapshot in querySnapshot.documents) {
                            val fcm = documentSnapshot.get("fcm") as? List<String>
                            if (fcm != null) {

                                fcmList.addAll(fcm)
                            }
                        }
                        if (fcmList.isNotEmpty()) {
                            GpsNotification(title,"$employeeName's $message",fcmList,context)
                        }
                    } else {
                        // Admin login failed
                        Log.d("fcm", "${task.result}  ${task.exception}")
                        // Toast.makeText(context, "Admin login failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Error occurred while querying admin section
                    val exception = task.exception
                    Log.d("Adminerror", exception.toString())
                }
            }
    }
}