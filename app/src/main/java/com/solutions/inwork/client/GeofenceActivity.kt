package com.solutions.inwork.client

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.solutions.inwork.R
import com.solutions.inwork.client.dataclasses.UniqueClient
import com.solutions.inwork.databinding.ActivityGeofenceBinding
import com.solutions.inwork.retrofitget.UniqueEmployeeGet
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GeofenceActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener{

    private lateinit var binding : ActivityGeofenceBinding
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val FINE_LOCATION_ACCESS_REQUEST_CODE = 10001



    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeofenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this@GeofenceActivity)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

      requestpermission()

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestpermission(){
        if (ContextCompat.checkSelfPermission(this@GeofenceActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this@GeofenceActivity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@GeofenceActivity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

    }



    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        enableUserLocation(map)
        googleMap.isMyLocationEnabled = true
        googleMap.setOnMyLocationButtonClickListener(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        if (ContextCompat.checkSelfPermission(this@GeofenceActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this@GeofenceActivity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@GeofenceActivity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            fetchCoordinates()

        }
    }


     fun fetchCoordinates() {
        val apiService = UniqueEmployeeGet.apiService

        val sharedPreferences = getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
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


    private fun enableUserLocation(map: GoogleMap) {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.setMyLocationEnabled(true)
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                //We need to show user a dialog for displaying why the permission is needed and then ask for the permission...
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_ACCESS_REQUEST_CODE
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_ACCESS_REQUEST_CODE
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun addMarkerToCurrentLocation(location: LatLng) {
        googleMap.addMarker(MarkerOptions().position(location).title("Current Location"))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    override fun onMyLocationButtonClick(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        addMarkerToCurrentLocation(currentLocation)
                        geofencingClient = LocationServices.getGeofencingClient(this)

                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to retrieve location: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        return true
    }



    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // startGeofencing()
                onMyLocationButtonClick()
            } else {
                Log.e(TAG, "Location permission denied")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "GeoFenceActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }

    @SuppressLint("MissingPermission")
    private fun startGeofencing(latitude: Double, longitude: Double,radius : Float) {
        val geofence = Geofence.Builder()
            .setRequestId("geofence_id")
            .setCircularRegion(
                latitude,  // Replace with the desired latitude
                longitude, // Replace with the desired longitude
                radius     // Replace with the desired radius in meters
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        googleMap.addMarker(MarkerOptions().position(LatLng(latitude, longitude)).title("Geofence Marker"))
        googleMap.addCircle(
            CircleOptions()
                .center(LatLng(latitude, longitude))
                .radius(100.0)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(70, 255, 0, 0))
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 15f))

        val geofencingRequest = GeofencingRequest.Builder()
            .addGeofence(geofence)
            .build()

        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
            .setAction("com.example.Inwork.ACTION_GEOFENCE_EVENT")
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            2607,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener {
                showToast("Geofence added successfully")
                Log.d(TAG, "Geofence added successfully")
            }
            .addOnFailureListener {
                showToast("Failed to add geofence: ${it.message}")
                Log.e(TAG, "Failed to add geofence: ${it.message}")
            }
    }
}