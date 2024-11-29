package com.solutions.inwork

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.solutions.inwork.Admin.AdminActivity
import com.solutions.inwork.client.ClientActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("TAG", remoteMessage.toString())
        val title = remoteMessage.data["title"]
        val message = remoteMessage.data["message"]
        val type = remoteMessage.data["type"]
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager


        if(title == "Notice Board"){

            Log.d("Notice","true")

            val channel = NotificationChannel(
                "notice_channel",
                "Notice_Board",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)


            var pendingIntent: PendingIntent? = null

                val intent = Intent(this, ClientActivity::class.java)
                intent.putExtra("fragment", "NoticeFragment")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)


            var notificationId: Int = type.hashCode()
            val notification = NotificationCompat.Builder(this, "notice_channel")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_logo)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(notificationId, notification)

            }

        else{

            val channel = NotificationChannel(
                "ChannelId",
                "My Channel Name",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)


            val sharedPreferences = getSharedPreferences("log",Context.MODE_PRIVATE)
            val log = sharedPreferences.getString("log",null)

            var pendingIntent: PendingIntent? = null

            if (log == "client") {
                val intent = Intent(this, ClientActivity::class.java)
                intent.putExtra("fragment", "NoticeFragment")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
            } else if (log == "admin") {
                val intent = Intent(this, AdminActivity::class.java)
                intent.putExtra("fragment", "Notification")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
            }


            var notificationId: Int

            val sp = getSharedPreferences("OVERSPEED",Context.MODE_PRIVATE)
            val t = sp.getString("overspeed",null)
            when(type){
                "overspeed" ->{
                    notificationId = 1
                    Log.d("over",notificationId.toString())
                }else -> {
                notificationId = 0
                Log.d("over",notificationId.toString())
            }
            }

            notificationId = type.hashCode()
            val notification = NotificationCompat.Builder(this, "ChannelId")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_logo)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(notificationId, notification)

        }
        }

//    override fun onHandleIntent(intent: Intent?) {
//        // Get the client name from the intent extras
//        val clientName = intent?.getStringExtra("clientName")
//
//        // Customize the FCM notification payload and recipient based on your FCM implementation
//        val notificationData = mapOf(
//            "title" to "Geofence Event",
//            "body" to "Client $clientName has entered/exited a geofence"
//        )
//
//        val adminFCMToken = "cS72Uu82RCStl-W1qDpPlE:APA91bEFnD1pNRLg0pDuMXR0zsAkEjo60kHChfSNiyU23ihD2dADDTmFgNNxxi7tmVgUhHxXrMgZUJxDmLpsggtywnO16wHaQhYKl8h87ht4sFwrIBC4XxOqlErSp0cAIwibz4-q2yjZ" // Replace with the admin's FCM token
//
//        val adminNotification = RemoteMessage.Builder(adminFCMToken)
//            .setData(notificationData)
//            .build()
//
//        FirebaseMessaging.getInstance().send(adminNotification)
//    }

//    override fun onHandleIntent(intent: Intent?) {
//        // Get the client name from the intent extras
//     //   val clientName = intent?.getStringExtra("clientName")
//
//        // Customize the FCM notification payload and recipient based on your FCM implementation
//        val notificationData = mapOf(
//            "title" to "Geofence Event",
//            "body" to "Kanishk has entered/exited a geofence"
//        )
//
//        val adminFCMToken = "cS72Uu82RCStl-W1qDpPlE:APA91bEFnD1pNRLg0pDuMXR0zsAkEjo60kHChfSNiyU23ihD2dADDTmFgNNxxi7tmVgUhHxXrMgZUJxDmLpsggtywnO16wHaQhYKl8h87ht4sFwrIBC4XxOqlErSp0cAIwibz4-q2yjZ" // Replace with the admin's FCM token
//
//        sendFCMNotification(adminFCMToken, notificationData)
//    }
//
//    private fun sendFCMNotification(adminFCMToken: String, notificationData: Map<String, String>) {
//        val serverKey = "AAAAxBIai-g:APA91bFFw70TQq_jGpl2dsVoMF9jktv_mrpXQRGbkYFKY1Gtm-nLOSSc2VfygWPoG5UM44p1fK4PCIZFs11VbZMdcZaQeU2UdU2W7a7LqfNIIjfqh-_FfUppkXbnviVfpnHqbQKab-zJ" // Replace with your FCM server key
//
//        val jsonBody = JSONObject()
//        jsonBody.put("to", adminFCMToken)
//        jsonBody.put("data", JSONObject(notificationData))
//
//        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody.toString())
//
//        val request = Request.Builder()
//            .url("https://fcm.googleapis.com/fcm/send")
//            .post(requestBody)
//            .addHeader("Authorization", "key=$serverKey")
//            .build()
//
//        OkHttpClient().newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                // Handle the failure case
//                e.message?.let { Log.d("Failure", it) }
//                e.printStackTrace()
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                // Handle the response from the FCM API
//                if (response.isSuccessful) {
//                    // Notification sent successfully
//
//                    Log.d("Successful",response.message)
//                } else {
//                    // Notification sending failed
//                    Log.d("Unsuccessful",response.message)
//                }
//            }
//        })
//    }

}
