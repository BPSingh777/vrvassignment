package com.solutions.inwork

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.solutions.inwork.client.GeofenceBroadcastReceiver
import com.solutions.inwork.client.dataclasses.UniqueClient
import com.solutions.inwork.retrofitget.UniqueEmployeeGet
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GeofenceClass(val context: Context) {


    private val geofencingClient = LocationServices.getGeofencingClient(context)



    fun fetchCoordinates() {
        val apiService = UniqueEmployeeGet.apiService

        val sharedPreferences = context.getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val EMPLOYEEID= sharedPreferences.getString("employee_id", null) ?: return
        val companyId = sharedPreferences.getString("company_id",null) ?: return
        Log.d("ID",EMPLOYEEID)
        val call = apiService.getData(companyId,EMPLOYEEID)

        call.enqueue(object : Callback<UniqueClient> {


            override fun onResponse(call: Call<UniqueClient>, response: Response<UniqueClient>) {


                if (response.isSuccessful) {
                    val uniqueClient = response.body()
                    if (uniqueClient != null) {
                        val latitude = uniqueClient.location_lat
                        val longitude = uniqueClient.location_long
                        val radius = uniqueClient.radius

                        startGeofencing(latitude.toDouble(),longitude.toDouble(),radius.toFloat())
                        Log.d("employee",uniqueClient.toString())
                        // Use latitude and longitude as needed
                        Log.d("Coordinates", "Latitude: $latitude, Longitude: $longitude")
                    }
                } else {
                    //   dismissprogressbar()
                    Log.d("emp", "API request failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<UniqueClient>, t: Throwable) {
                Log.d("emp", "API request failed", t)
            }

        })
}

    @SuppressLint("MissingPermission")
     fun startGeofencing(latitude: Double, longitude: Double,radius:Float) {
        val geofence = Geofence.Builder()
            .setRequestId("geofence_id")
            .setCircularRegion(latitude,  // Replace with the desired latitude
                longitude, // Replace with the desired longitude
                radius     // Replace with the desired radius in meters
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            .setAction("com.example.Inwork.ACTION_GEOFENCE_EVENT")
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val sharedPreferences = context.getSharedPreferences("geofence_activate",Context.MODE_PRIVATE)
        geofencingClient.addGeofences(geofencingRequest, pendingIntent).run{
            addOnSuccessListener {
                sharedPreferences.edit().putBoolean("geofence_activation",true).apply()
            Toast.makeText(context,"Geofence added successfully",Toast.LENGTH_SHORT).show()
            Log.d("GeofenceClass", "Geofence added successfully")
        }
            addOnFailureListener {
                Toast.makeText(context,"Failed to add geofence: ${it.message}",Toast.LENGTH_SHORT).show()
                Log.e("GeofenceClass", "Failed to add geofence: ${it.message}")

                sharedPreferences.edit().putBoolean("geofence_activation",false).apply()


            }
        }

    }


}


