package com.solutions.inwork.retrofitPost

import com.solutions.inwork.client.GeofenceBroadcastReceiver
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FcmService {
    @Headers(
        "Content-Type: application/json",
        "Authorization: key=AAAAa8gk1zc:APA91bEXlVMS52U7wSCnoNjRwmS0SsZWC__LZIh3cazVJDB7U_nN3x8VvvLR8T2YOckUMuXD8FB66v5pNTLqILQUFTecetIE-RMpzGyJCM-NlqjVOv6n3jmG_xdjxTzphB82Yf41kP-k"
    )
    @POST("fcm/send")
    fun sendNotification(@Body notification: GeofenceBroadcastReceiver.FcmNotification): Call<Any>
}
