package com.solutions.inwork

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.solutions.inwork.client.GeofenceBroadcastReceiver
import com.solutions.inwork.client.LocationHelper
import com.solutions.inwork.client.MidnightResetReceiver
import com.solutions.inwork.retrofitPost.EmployeeConnectivityStatusPost
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class GpsNotificationService : Service() {

    private lateinit var gpsStatusReceiver: GpsStatusReceiver
    private lateinit var notificationManager: NotificationManager
    private val NOTIFICATION_ID = 123
    private var isGpsOn : Boolean = false
    private lateinit var connectivityReceiver: ConnectivityReceiver
    private lateinit var batteryReceiver : BatteryReceiver
    private lateinit var midnightResetReceiver: MidnightResetReceiver
    private val INTERVAL = 60 * 1000 // 1 minute interval
    private lateinit var timer: Timer

    override fun onCreate() {
        super.onCreate()

        // Register the GPS status receiver
        gpsStatusReceiver = GpsStatusReceiver()
        val intentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(gpsStatusReceiver, intentFilter)

//        connectivityReceiver = ConnectivityReceiver()
//        val intentFilter2 = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
//        registerReceiver(connectivityReceiver, intentFilter2)

        batteryReceiver = BatteryReceiver()
        val intentFilter3 = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver,intentFilter3)

        midnightResetReceiver = MidnightResetReceiver()
        val intent4 = IntentFilter(Intent.ACTION_TIME_TICK)
        registerReceiver(midnightResetReceiver,intent4)

        //for online timestamp
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val timestamp = getCurrentTimestamp()
            //    setMidnightResetAlarm(this@GpsNotificationService,timestamp)
                Log.d("timestamp1",timestamp)
                location(timestamp)
                // Do something with the timestamp (e.g., save it, display it, etc.)
            }
        }, Date(), INTERVAL.toLong())



        // Start the service as a foreground service
       val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }


   private fun PostTimestamp(timestamp: String,latitude: String,longitude: String){
        val sharedPreferences = getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val employeeID = sharedPreferences.getString("employee_id", null) ?: return
        val companyID = sharedPreferences.getString("company_id", null) ?: return


        val jsonObject = JSONObject().apply {
            put("company_id", companyID)
            put("employee_id", employeeID)
            put("location_lat", latitude)
            put("location_long",longitude)
            put("updated_time_stamp",timestamp)

        }

        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())

        val apiService = EmployeeConnectivityStatusPost.apiService
        val call = apiService.postData(requestBody)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Request successful

                 //   sentNoticeNotification()
                    //  binding.companyIDeditext.text!!.clear()
                    Log.d("connectivitystatus","Status Updated successfully")

                  //  Toast.makeText(requireContext(),"Notice Sent Successfully",Toast.LENGTH_SHORT).show()
                } else {
                    //dismissprogressbar()
                    // Request failed
                    Log.d("connectivitystatus",response.message().toString())
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Request failed due to network error or other issues
                Log.d("connectivitystatus",t.toString())
            }
        })
    }

   private fun location(timestamp: String){

        val locationHelper = LocationHelper(this)
        locationHelper.getCurrentLocation(object : LocationHelper.LocationListener {
            override fun onLocationReceived(location: Location) {
                val latitude = location.latitude
                val longitude = location.longitude
                // Do something with latitude and longitude

                Log.d("timestamp2","$latitude $longitude")

                PostTimestamp(timestamp,latitude.toString(),longitude.toString())

               // dismissprogressbar()

              //  getCompleteAddressString(latitude,longitude)
                //binding.location.text = "Latitude: $latitude Longitude: $longitude"
             //   val map = view!!.findViewById<MapView>(R.id.locMap)

                var isselected = true


                //   Toast.makeText(requireContext(),"$latitude $longitude",Toast.LENGTH_SHORT).show()
            }

            override fun onLocationFailed() {
//                dismissprogressbar()
                Log.d("LocationBg","failed to fetch")
                // Handle location retrieval failure
            }

            override fun onProviderDisabled() {

            }
        })



    }

    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = Calendar.getInstance().time
        return dateFormat.format(currentTime)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Return START_STICKY to ensure the service is restarted if it's killed by the system

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(midnightResetReceiver)
        unregisterReceiver(batteryReceiver)
     //   unregisterReceiver(connectivityReceiver)
        // Unregister the GPS status receiver
        unregisterReceiver(gpsStatusReceiver)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }



    private fun createNotification(): Notification {

        val channelId = "gps_channel"
        val channelName = "GPS Channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val contentText = if (isGpsOn) "GPS is ON" else "GPS is OFF"

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Inwork")
            .setSmallIcon(R.drawable.ic_logo)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .build()
    }


    private var isNotificationScheduled = false
    private var gpsTurnedOffTime: Long = 0
    private val delayDuration: Long = 1 * 60 * 1000 // 2 minutes in milliseconds
    private val notificationHandler = Handler()

    private inner class GpsStatusReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            if (isGpsEnabled) {
                cancelNotification()
                Toast.makeText(context, "GPS is On", Toast.LENGTH_SHORT).show()
            } else {
                if (!isNotificationScheduled) {
                    gpsTurnedOffTime = System.currentTimeMillis()
                    scheduleNotification(context!!)
                    isNotificationScheduled = true
                }
            }

            if (isGpsEnabled != isGpsOn) {
                isGpsOn = isGpsEnabled
            }
        }
    }

    private fun scheduleNotification(context: Context) {
        notificationHandler.postDelayed({
            if (System.currentTimeMillis() - gpsTurnedOffTime >= delayDuration) {
                getAdminFCM("GPS ON/OFF", "GPS is OFF", context)
                isNotificationScheduled = false
            }
        }, delayDuration)
    }

    private fun cancelNotification() {
        notificationHandler.removeCallbacksAndMessages(null)
        isNotificationScheduled = false
    }



    private var isnotified = true
    private inner class ConnectivityReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo

            if (networkInfo != null && networkInfo.isConnected) {
                // Device is online
                // Perform necessary actions here
                if (isnotified){
                    isnotified = false
                //    getAdminFCM("Connectivity","Device is online", context!!)
                    Log.d("Connectivity", "Device is online")
                }

            } else {
                // Device is offline
                // Perform necessary actions here
                isnotified = true
               // GeofenceBroadcastReceiver().postStatus(context!!,"offline")
               // getAdminFCM("Connectivity","Device is online", context!!)
                Log.d("Connectivity", "Device is offline")
            }
        }
    }

//    private fun updateNotification() {
//        val notification = createNotification()
//        notificationManager.notify(NOTIFICATION_ID, notification)
//    }




     fun GpsNotification(
        title: String,
        message: String,
        adminFcmList: List<String>,
        context: Context
    ) {
        val url = "https://fcm.googleapis.com/fcm/send"
        val notificationObject = JSONObject()
        notificationObject.put("title", title)
        notificationObject.put("message", message)


        Log.d("adminfcmtokelist",adminFcmList.toString())

        for (adminFcm in adminFcmList) {
            val jsonObject = JSONObject()
            jsonObject.put("data", notificationObject)
            jsonObject.put("to", adminFcm)

            Log.d("fcmgps", adminFcm)
            val request: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, jsonObject,
                { response ->
                    // Handle the response here
                    Log.d("gpsstatus1", "success")

                    //  Toast.makeText(context, "GPS Successful", Toast.LENGTH_SHORT).show()
                },
                { error ->
                    // Handle the error here
                    Log.d("gpsstatus1", "fail")
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


    fun getAdminFCM(title: String, message: String, context: Context) {

        val sharedPreferences = context.getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val employeeName = sharedPreferences.getString("name", null)
        val companyID = sharedPreferences.getString("company_id", null)


        val firestore = FirebaseFirestore.getInstance()
        val adminCollection = firestore.collection("admin")


        val notification = GeofenceBroadcastReceiver()
        notification.postNotification(context, "${employeeName}'s $message", title)

        adminCollection.whereEqualTo("company_id", companyID)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    if (querySnapshot != null && !querySnapshot.isEmpty) {


                        val fcmList = mutableListOf<String>()
                        for (documentSnapshot in querySnapshot.documents) {
                            val fcm = documentSnapshot.get("fcm") as? List<String>
                            if (fcm != null) {

                                fcmList.addAll(fcm)
                            }
                        }
                        if (fcmList.isNotEmpty()) {

                            Log.d("adminfcmtokelist1",fcmList.toString())
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




//  private var isNotificationSent = false
//    private inner class GpsStatusReceiver : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//
//
//
//            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
////            val sharedPreferences = getSharedPreferences("GPS_BOOL",Context.MODE_PRIVATE)
////            val editor = sharedPreferences.edit()
////
////            editor.putString("StatusGps",isGpsEnabled.toString())
////            editor.putBoolean("isGpsOn",isGpsEnabled)
////            editor.apply()
////            Log.d("isGpsOn1",isGpsEnabled.toString())
//
//
//            if(isGpsEnabled){
//                isNotificationSent = false
//                Toast.makeText(context,"GPS is On",Toast.LENGTH_SHORT).show()
//            }else {
//                if(!isNotificationSent){
//                    Log.d("adminfcmtokelist2","size")
//                    getAdminFCM("GPS ON/OFF", "GPS is OFF", context!!)
//                    isNotificationSent = true
//                }
//
////                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
////
////                    val userId = FirebaseAuth.getInstance().currentUser?.uid
////
////                    val fcmList = mutableListOf<String>()
////                    if (userId != null) {
////                        Log.d("Gpstoken", token.toString())
////                        fcmList.addAll(listOf(token.toString()))
////
////                        Log.d("adminfcmtokelist2","size")
////
//////                    val clientHome = ClientHomeFragment()
////               //         GpsNotification("GPS ON/OFF", "GPS is OFF", fcmList, context!!)
//////
////                        getAdminFCM("GPS ON/OFF", "GPS is OFF", context!!)
//////                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//////                        cellinfo = simUtils.detectSimNetworkTechnology(requireContext()).toString()
//////                        // info.text = cellinfo
//////                        Log.d("cell", cellinfo)
//////                    }
////                    }
////
////                }
//            }
//
//            if (isGpsEnabled != isGpsOn) {
//                isGpsOn = isGpsEnabled
//              //  updateNotification()
//            }
//        }
//    }


//
//class GpsNotificationService : Service() {
//
//    private lateinit var gpsStatusReceiver: GpsStatusReceiver
//    private lateinit var notificationManager: NotificationManager
//    private val NOTIFICATION_ID = 123
//
//    override fun onCreate() {
//        super.onCreate()
//
//        // Register the GPS status receiver
//        gpsStatusReceiver = GpsStatusReceiver()
//        val intentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
//        registerReceiver(gpsStatusReceiver, intentFilter)
//
//        // Start the service as a foreground service
//        val notification = createNotification()
//        startForeground(NOTIFICATION_ID, notification)
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        // Return START_STICKY to ensure the service is restarted if it's killed by the system
//        return START_STICKY
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//
//        // Unregister the GPS status receiver
//        unregisterReceiver(gpsStatusReceiver)
//    }
//
//    override fun onBind(intent: Intent): IBinder? {
//        return null
//    }
//
//    private fun createNotification(): Notification {
//        // Build and return a notification for the foreground service
//        // Customize the notification as per your requirements
//        val channelId = "gps_channel"
//        val channelName = "GPS Channel"
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel =
//                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
//            notificationManager = getSystemService(NotificationManager::class.java)
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        return NotificationCompat.Builder(this, channelId)
//            .setContentTitle("GPS Notification")
//            .setContentText("GPS is OFF")
//            .setSmallIcon(R.drawable.ic_notification)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//    }
//
//    private inner class GpsStatusReceiver : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                // GPS is ON
//                dismissNotification()
//            } else {
//                // GPS is OFF
//                showNotification()
//            }
//        }
//    }
//
//    private fun showNotification() {
//        val notification = createNotification()
//        notificationManager.notify(NOTIFICATION_ID, notification)
//    }
//
//    private fun dismissNotification() {
//        notificationManager.cancel(NOTIFICATION_ID)
//    }
//}

//
//class GpsNotificationService : Service() {
//
//    private lateinit var gpsStatusReceiver: GpsStatusReceiver
//
//    override fun onCreate() {
//        super.onCreate()
//
//        // Register the GPS status receiver
//        gpsStatusReceiver = GpsStatusReceiver()
//        val intentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
//        registerReceiver(gpsStatusReceiver, intentFilter)
//
//        // Start the service as a foreground service
//        val notification = createNotification()
//        startForeground(NOTIFICATION_ID, notification)
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        // Return START_STICKY to ensure the service is restarted if it's killed by the system
//        return START_STICKY
//    }
//
////    override fun onDestroy() {
////        super.onDestroy()
////
////        // Unregister the GPS status receiver
////        unregisterReceiver(gpsStatusReceiver)
////    }
//
//    override fun onBind(intent: Intent): IBinder? {
//        return null
//    }
//
//     fun dismissNotification() {
//        val notificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.cancel(NOTIFICATION_ID)
//    }
//
//    private fun createNotification(): Notification {
//        // Build and return a notification for the foreground service
//        // Customize the notification as per your requirements
//        val channelId = "gps_channel"
//        val channelName = "GPS Channel"
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel =
//                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
//            val notificationManager = getSystemService(NotificationManager::class.java)
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        // detectGpsAndSendNotification(this)
//
//        return NotificationCompat.Builder(this, channelId)
//            .setContentTitle("GPS Notification")
//            .setContentText("GPS is OFF")
//            .setSmallIcon(R.drawable.ic_notification)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//    }
//
////            fun dismissNotification(context: Context) {
////            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
////            notificationManager.cancel(NOTIFICATION_ID)
////        }
//
//    companion object {
//        private const val NOTIFICATION_ID = 123
//    }
//
//
//    ////
////class GpsNotificationService : Service() {
////
////    private lateinit var gpsStatusReceiver: GpsStatusReceiver
////
////    override fun onCreate() {
////        super.onCreate()
////
////        // Register the GPS status receiver
////        gpsStatusReceiver = GpsStatusReceiver()
////        val intentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
////        registerReceiver(gpsStatusReceiver, intentFilter)
////    }
////
////    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
////        // Return START_STICKY to ensure the service is restarted if it's killed by the system
////        return START_STICKY
////    }
////
//////    override fun onDestroy() {
//////        super.onDestroy()
//////
//////        // Unregister the GPS status receiver
//////        unregisterReceiver(gpsStatusReceiver)
//////    }
////
////    override fun onBind(intent: Intent): IBinder? {
////        return null
////    }
////
////

////    companion object {
////        const val NOTIFICATION_ID = 1
////        const val CHANNEL_ID = "geofence_channel"}
//
//        fun showNotification(context: Context) {
//            val notificationBuilder = NotificationCompat.Builder(context, "geofence_channel")
//                .setContentTitle("GPS is OFF")
//                .setContentText("Turn on GPS to enable location services")
//                .setSmallIcon(R.drawable.ic_notification)
//            // Set other notification settings as needed
//
//            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.notify(1, notificationBuilder.build())
//        }
//
//        fun dismissNotification(context: Context) {
//            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.cancel(1)
//        }
//
//}
