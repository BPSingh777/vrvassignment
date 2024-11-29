package com.solutions.inwork.client

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.solutions.inwork.MyFirebaseMessagingService
import com.solutions.inwork.R
import com.solutions.inwork.retrofitPost.EmployeeStatusPost
import com.solutions.inwork.retrofitPost.RetofitCheckOutPost
import com.solutions.inwork.retrofitPost.RetrofitCheckInPost
import com.solutions.inwork.retrofitPost.RetrofitPostNotifications
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.firestore.FirebaseFirestore
import com.solutions.inwork.client.fragments.NotificationClientFragment
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashMap
import java.util.Locale

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_GEOFENCE_EVENT = "com.example.geofence.ACTION_GEOFENCE_EVENT"
        const val PREFS_NAME = "GeofencePrefs"
        const val KEY_NOTIFICATION_SENT = "NotificationSent"
    }

   // private var isNotificationSent = false


    override fun onReceive(context: Context, intent: Intent) {


//        val sharedPreferencesGeo = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        isNotificationSent = sharedPreferencesGeo.getBoolean(KEY_NOTIFICATION_SENT, false)

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent?.hasError() == true) {
            val errorMessage = "GeofencingEvent error: ${geofencingEvent.errorCode}"
            showToast(context, errorMessage)
            Log.d("shivam",errorMessage)
            return
        }

        val sharedPreferences = context.getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val employeeName = sharedPreferences.getString("name", null)
        val companyID = sharedPreferences.getString("company_id", null)

        Log.d("company","$companyID $employeeName")
        if (companyID == null) {
            Log.d("companyid","null")
        } else {
            val geofenceTransition = geofencingEvent?.geofenceTransition
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ) {
                val transitionMessage = "$employeeName Checked In "
                val notification = "You Checked In"
                val sharedPreferencesGeo = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val isNotificationSent = sharedPreferencesGeo.getBoolean(KEY_NOTIFICATION_SENT, false)
                if(!isNotificationSent){
                    postStatus(context,"CHECKED IN")
                    Log.d("Geofence",notification)
                    showNotification(context, transitionMessage)
                    //    Notification(context, transitionMessage)
                    PostCheckIn(context,"auto")
                    getAdminFCM(context, companyID,transitionMessage,"yes",transitionMessage)
                    showToast(context, transitionMessage)

                    with(sharedPreferencesGeo.edit()){
                       putBoolean(KEY_NOTIFICATION_SENT, true)
                       apply()
                    }
//                    val editor = sharedPreferencesGeo.edit()
//                    editor.putBoolean(KEY_NOTIFICATION_SENT, isNotificationSent)
                    Log.d(KEY_NOTIFICATION_SENT, isNotificationSent.toString())
//                    editor.apply()
                }


            }
            else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                val transitionMessage = "$employeeName Checked Out "
             //   val notification = "You Checked In"

                val sharedPreferencesGeo = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val isNotificationSent = sharedPreferencesGeo.getBoolean(KEY_NOTIFICATION_SENT, true)

                if (isNotificationSent){
                    showNotification(context, transitionMessage)
                    postStatus(context,"CHECKED OUT")
                    // Log.d("shivam",notification)
                    //  Notification(context, transitionMessage)
                    PostChechOut(context,"auto")
                    getAdminFCM(context, companyID,transitionMessage,"no",transitionMessage)
                    showToast(context, transitionMessage)

                    with(sharedPreferencesGeo.edit()){
                        putBoolean(KEY_NOTIFICATION_SENT, false)
                        apply()
                    }
//                    val editor = sharedPreferencesGeo.edit()
//                    editor.putBoolean(KEY_NOTIFICATION_SENT, isNotificationSent)
                    Log.d(KEY_NOTIFICATION_SENT, isNotificationSent.toString())
                  //  editor.apply()
                }



            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                val message = "$employeeName Dwell Geofence"
                showNotification(context, message)
                postStatus(context,"CHECKED IN")
              //  Notification(context, message)
                Log.d("shivam",message)

                getAdminFCM(context,companyID,message,"yes","You Checked In")
                showToast(context, message)
            } else {
                val errorMessage = "Invalid geofence transition"
                //    showToast(context, errorMessage)
                // Notification(errorMessage)
                // showNotification(context, errorMessage)
            }
        }
    }

//    private fun detectGpsAndSendNotification(context: Context) {
//
//        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            //     binding.permission.text =    "GPS is ON"
//            // GPS is ON
//            // Perform your logic here
//            // Send notification or execute any other necessary code
//            Log.d("gpsstatus","GPS is ON")
//        } else {
//            // GPS is OFF
//            // Perform your logic here
//            // Send notification or execute any other necessary code
//            Log.d("gpsstatus","GPS is OFF")
//            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
//
//                val userId = FirebaseAuth.getInstance().currentUser?.uid
//
//                val fcmList = mutableListOf<String>()
//                if (userId != null) {
//                    Log.d("Gpstoken",token.toString())
//                    fcmList.addAll(listOf(token.toString()))
//                    showNotification(context,"GPS is OFF")
//
////                    val clientHome = ClientHomeFragment()
//               //     GpsNotification("GPS ON/OFF","GPS is OFF",fcmList)
////
//              //      getAdminFCM("GPS ON/OFF","GPS is OFF")
////                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
////                        cellinfo = simUtils.detectSimNetworkTechnology(requireContext()).toString()
////                        // info.text = cellinfo
////                        Log.d("cell", cellinfo)
////                    }
//                }
//
//            }
//
//        }
//        //   stopForeground(true) // Stop the foreground service once the task is completed
//        //    stopSelf() // Stop the service
//    }
    fun PostCheckIn(context: Context,remarks: String){

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val currentDate = Date()
        val timeStamp = dateFormat.format(currentDate)
        val sharedPreferences = context.getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val employeeId = sharedPreferences.getString("employee_id",null)
        val work_start_time = sharedPreferences.getString("work_start_time",null)
        val work_end_time = sharedPreferences.getString("work_end_time",null)

        val jsonObject = JSONObject().apply {
            put("employee_id",employeeId)
            put("work_start_time",work_start_time)
            put("work_end_time",work_end_time)
            put("checkin_time",timeStamp)
            put("remarks",remarks)
        }
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(),jsonObject.toString())
        val call = RetrofitCheckInPost.apiService.postData(requestBody)
        call.enqueue(object: Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful){
                    Log.d("CheckIn","CheckIn Details Sent Successfully")
                }else{
                    Log.d("CheckIn","CheckIn Details Didn't sent")
                }


            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("CheckIn","Something went wrong")
            }

        })

    }

    fun PostChechOut(context: Context,remarks: String){

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val currentDate = Date()
        val timeStamp = dateFormat.format(currentDate)
        val sharedPreferences = context.getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val employeeId = sharedPreferences.getString("employee_id",null)
        val work_start_time = sharedPreferences.getString("work_start_time",null)
        val work_end_time = sharedPreferences.getString("work_end_time",null)


        val jsonObject = JSONObject().apply {
            put("employee_id",employeeId)
            put("work_start_time",work_start_time)
            put("work_end_time",work_end_time)
            put("checkout_time",timeStamp)
            put("remarks",remarks)
        }
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(),jsonObject.toString())
        val call = RetofitCheckOutPost.apiService.postData(requestBody)
        call.enqueue(object: Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful){
                    Log.d("CheckOut","CheckOut Details Sent Successfully")
                }else{
                    Log.d("CheckOut","CheckOut Details Didn't sent")
                }


            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("CheckOut","Something went wrong")
            }

        })
    }

    fun postStatus(context: Context,Status: String){

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val currentDate = Date()
        val timeStamp = dateFormat.format(currentDate)

        val sharedPreferences = context.getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val companyID = sharedPreferences.getString("company_id", null)
        val employeeId = sharedPreferences.getString("employee_id",null)
        val jsonObject = JSONObject().apply {
            put("company_id", companyID)
            put("employee_id", employeeId)
            put("current_status", Status)
            put("time_stamp", timeStamp)
        }
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(),jsonObject.toString())
        val call = EmployeeStatusPost.apiService.postData(requestBody)

        call.enqueue(object :Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful){
                    Log.d("StatusPost","Notification Sent Successfully")
                 //   Toast.makeText(context,"Notification Sent Successfully",Toast.LENGTH_SHORT).show()
                }else{
                    Log.d("StatusPost","Notification not Sent")
                }

            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("StatusPost","Something went wrong")
            }

        })
    }

    private fun showNotification(context: Context, message: String) {
        // Create a notification channel for Android Oreo and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "geofence_channel"
            val channelName = "Geofence Channel"
            val channelDescription = "Channel for Geofence Notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, ClientActivity::class.java)
        intent.putExtra("fragment", "NotificationClientFragment")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val fragmentPendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        // Build and show the notification
        val notificationBuilder = NotificationCompat.Builder(context, "geofence_channel")
            .setContentTitle("Inwork")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_logo)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(fragmentPendingIntent)


        //  Add an action to the notification for sending an FCM notification to the admin
        val adminIntent = Intent(context, MyFirebaseMessagingService::class.java).apply {
            putExtra("client_name", "Kanishk")
            putExtra("geofence_event", message)
        }
        val pendingIntent = PendingIntent.getService(context, 0, adminIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        val action = NotificationCompat.Action.Builder(
            R.drawable.ic_notification,
            "Notify Admin",
            pendingIntent
        ).build()
       // notificationBuilder.addAction(action)


        with(NotificationManagerCompat.from(context)) {
            notify(1, notificationBuilder.build())
        }
    }

    fun postNotification(context: Context,notification : String,title: String){

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val currentDate = Date()
        val timeStamp = dateFormat.format(currentDate)

        val sharedPreferences = context.getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val companyID = sharedPreferences.getString("company_id", null)
        val employeeId = sharedPreferences.getString("employee_id",null)
        val jsonObject = JSONObject().apply {
            put("company_id", companyID)
            put("employee_id", employeeId)
            put("title", title)
            put("notification",notification)
            put("notification_date", timeStamp)
        }
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(),jsonObject.toString())
        val call = RetrofitPostNotifications.apiService.postData(requestBody)

        call.enqueue(object :Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
             //   Toast.makeText(context,"Notification Sent Successfully",Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context,"Something went wrong",Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun getAdminFCM(context: Context, companyID: String, message: String,Status: String,notification: String) {
        val firestore = FirebaseFirestore.getInstance()
        val adminCollection = firestore.collection("admin")

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
                            Notification(context, message, fcmList,Status,notification)
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



    fun Notification(context: Context, message: String, adminFcmList: List<String>,status: String,notification: String) {
        val url = "https://fcm.googleapis.com/fcm/send"
        val notificationObject = JSONObject()
        notificationObject.put("title", "INWORK")
        notificationObject.put("message", message)

        val sharedPreferences = context.getSharedPreferences("CHECK_STATUS",Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        Log.d("checkStatus",status)
        postNotification(context,notification,"Attendance")

        for (adminFcm in adminFcmList) {
            val jsonObject = JSONObject()
            jsonObject.put("data", notificationObject)
            jsonObject.put("to", adminFcm)

            Log.d("fcmtoken",adminFcm)
            val request: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, jsonObject,
                { response ->
                    // Handle the response here
                    Log.d("geovolley", "success")
                    editor.putString("status",status)
                    //editor.putString("bool",status)
                    editor.apply()
                  //  Toast.makeText(context, "Successful", Toast.LENGTH_SHORT).show()
                },
                { error ->
                    // Handle the error here
                    Log.d("geovolley", "fail")
                    Toast.makeText(context, "Notification sending Failed", Toast.LENGTH_SHORT).show()
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "key=AAAAa8gk1zc:APA91bEXlVMS52U7wSCnoNjRwmS0SsZWC__LZIh3cazVJDB7U_nN3x8VvvLR8T2YOckUMuXD8FB66v5pNTLqILQUFTecetIE-RMpzGyJCM-NlqjVOv6n3jmG_xdjxTzphB82Yf41kP-k"
                    headers["Content-Type"] = "application/json"
                    return headers
                }
            }
            Volley.newRequestQueue(context).add(request)
        }
    }





    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    data class FcmNotification(
        val to: String,
        val notification: FcmNotificationData
    )

    data class FcmNotificationData(
        val title: String,
        val body: String
    )

}



