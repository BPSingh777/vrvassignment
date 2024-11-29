package com.solutions.inwork.client

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.solutions.inwork.R
import com.google.firebase.firestore.FirebaseFirestore
import com.solutions.inwork.client.fragments.ClientHomeFragment
import org.json.JSONObject

class LocationChangeDetector(private val context: Context) {

    private var locationManager: LocationManager
    private var notificationManager: NotificationManager
    private var previousLocation: Location? = null
    private var overspeed : Boolean = true

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
        private const val NOTIFICATION_CHANNEL_ID = "LocationChangeChannel"
        private const val NOTIFICATION_ID = 456
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (previousLocation != null) {
                val distance = previousLocation?.distanceTo(location) ?: 0.0f
                val timeElapsed = (location.time - previousLocation?.time!!) / 1000

                Log.d("kan","locchange")
                if (distance > 332 && timeElapsed < 10) {

                    val sf = context.getSharedPreferences("Overspeed",Context.MODE_PRIVATE)
                    val isSent = sf.getBoolean("overspeed_sent",true)
                    Log.d("Overspeed212",isSent.toString())
                    if (isSent){
                        getAdminFCM("Over Speeding")
                       // generateNotification(context)
                        Log.d("overspeed","notification")
                        with(sf.edit()){
                            putBoolean("overspeed_sent",false)
                            apply()
                        }
                    }



                   // overspeed = false
                }
                else{
                    val sf = context.getSharedPreferences("Overspeed",Context.MODE_PRIVATE)
                    sf.edit().putBoolean("overspeed_sent",true).apply()
                    Log.d("Overspeed213",sf.getBoolean("overspeed_sent",false).toString())


                    Log.d("overspeed","normal")
                }
            }

            previousLocation = location
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // Your existing code for handling status changes
        }

        override fun onProviderDisabled(provider: String) {
            // Handle the case when the location provider is disabled
            Toast.makeText(context,"Your Gps is disabled",Toast.LENGTH_SHORT).show()
            Log.d("providerDisabled", "Location provider is disabled: $provider")
        }

        override fun onProviderEnabled(provider: String) {
            // Handle the case when the location provider is disabled
            Toast.makeText(context,"Your Gps is enabled",Toast.LENGTH_SHORT).show()
            Log.d("providerDisabled", "Location provider is disabled: $provider")
        }


    }


    init {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun startLocationUpdates() {
        // Check if the location permission is granted
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val minTime = 5000L // Minimum time interval between location updates (in milliseconds)
            val minDistance = 0f // Minimum distance between location updates (in meters)

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                locationListener
            )
        } else {
            throw RuntimeException("Location permission not granted.")
        }
    }

    private fun generateNotification(context: Context) {
        // Create a notification channel for Android Oreo and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Location Change",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

//        val geofenceBroadcastReceiver = GeofenceBroadcastReceiver()
//        geofenceBroadcastReceiver.postNotification(context,"Over Speeding","Location Change")
        getAdminFCM("Over Speeding")
        // Create the notification
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle("Location Change Detected")
            .setContentText("You have moved more than 1km within 10 seconds.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Show the notification
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun getAdminFCM( message: String) {

        val sharedPreferences = context.getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val employeeName = sharedPreferences.getString("name", null)
        val companyID = sharedPreferences.getString("company_id", null)



        val firestore = FirebaseFirestore.getInstance()
        val adminCollection = firestore.collection("admin")

        adminCollection.whereEqualTo("company_id", companyID)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    if (querySnapshot != null && !querySnapshot.isEmpty) {
                        val geofenceBroadcastReceiver = GeofenceBroadcastReceiver()
          geofenceBroadcastReceiver.postNotification(context,"$employeeName's $message","Safety Alert!!")
                        val fcmList = mutableListOf<String>()
                        for (documentSnapshot in querySnapshot.documents) {
                            val fcm = documentSnapshot.get("fcm") as? List<String>
                            if (fcm != null) {

                                fcmList.addAll(fcm)
                            }
                        }
                        if (fcmList.isNotEmpty()) {
                            GpsNotification("$employeeName's $message",fcmList)
                        }
                    } else {
                        // Admin login failed
                        Log.d("fcm", "${task.result}  ${task.exception}")
                        // Toast.makeText(context, "Admin login failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Error occurred while querying admin section
                    val exception = task.exception
                    Log.d("overspeed", exception.toString())
                }
            }
    }


    fun GpsNotification(message: String, adminFcmList: List<String>) {
        val url = "https://fcm.googleapis.com/fcm/send"
        val notificationObject = JSONObject()
        notificationObject.put("title", "Safety Alert!!")
        notificationObject.put("message", message)
        notificationObject.put("type","overspeed")



        val sharedPreferences = context.getSharedPreferences("OVERSPEED",Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("overspeed","over")
        editor.apply()

        for (adminFcm in adminFcmList) {
            val jsonObject = JSONObject()
            jsonObject.put("data", notificationObject)
            jsonObject.put("to", adminFcm)

            Log.d("fcmover",adminFcm)
            val request: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, jsonObject,
                { response ->
                    // Handle the response here
                    Log.d("overspeed", "success")

                 //   Toast.makeText(context, "GPS Successful", Toast.LENGTH_SHORT).show()
                },
                { error ->
                    // Handle the error here
                    Log.d("overspeed", "fail")
               //     Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = java.util.HashMap<String, String>()
                    headers["Authorization"] = "key=AAAAa8gk1zc:APA91bEXlVMS52U7wSCnoNjRwmS0SsZWC__LZIh3cazVJDB7U_nN3x8VvvLR8T2YOckUMuXD8FB66v5pNTLqILQUFTecetIE-RMpzGyJCM-NlqjVOv6n3jmG_xdjxTzphB82Yf41kP-k"
                    headers["Content-Type"] = "application/json"
                    return headers
                }
            }
            Volley.newRequestQueue(context).add(request)
        }
    }

//    fun GpsNotification(fcm: String,message: String){
//
//
//
//        val url = "https://fcm.googleapis.com/fcm/send"
//        val notificationObject = JSONObject()
//        notificationObject.put("title", "Location Change")
//        notificationObject.put("message", message)
//        notificationObject.put("type","overspeed")
//
//        val sharedPreferences = context.getSharedPreferences("OVERSPEED",Context.MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//        editor.putString("overspeed","over")
//        editor.apply()
//
//        val jsonObject = JSONObject()
//        jsonObject.put("data", notificationObject)
//        jsonObject.put("to", fcm)
//
//        Log.d("fcmtokeng",fcm)
//        val request: JsonObjectRequest = object : JsonObjectRequest(
//            Method.POST, url, jsonObject,
//            { response ->
//                // Handle the response here
//                Log.d("overspeed", "success $fcm")
//                // editor.putString("status",status)
//                //editor.putString("bool",status)
//                //    editor.apply()
//
//            //    Toast.makeText(context, "OverSpeed Successful", Toast.LENGTH_SHORT).show()
//            },
//            { error ->
//                // Handle the error here
//                Log.d("overspeed", error.toString())
//            //    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
//            }) {
//            override fun getHeaders(): MutableMap<String, String> {
//                val headers = HashMap<String, String>()
//                headers["Authorization"] = "key=AAAAJcBjKiY:APA91bE8D8PeUeXcTjM3TRyx4rackDmoMZG2pbjgRQshEaoiFap46JqKQlvL2-4uZjLODmWAsxI10PGoI4JjVTfRqbEJSP4W24Bb4E2cuh2PRdAcvkT9CkOFibwdv4zxoPZOm-iEy-Dl"
//                headers["Content-Type"] = "application/json"
//                return headers
//            }
//        }
//        Volley.newRequestQueue(context).add(request)
//
//
//    }
}
