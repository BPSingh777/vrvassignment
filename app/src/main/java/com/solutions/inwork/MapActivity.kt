package com.solutions.inwork

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.solutions.inwork.databinding.ActivityMapBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.gson.annotations.SerializedName

private const val BASE_URL = "https://maps.googleapis.com/maps/api/geocode/"

class MapActivity : AppCompatActivity() , OnMapReadyCallback , GoogleMap.OnMyLocationButtonClickListener{


    private lateinit var mapView: MapView
    private lateinit var searchView: SearchView
    private lateinit var googleMap: GoogleMap
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var binding : ActivityMapBinding
    private var mMap: GoogleMap? = null
    private val markersList: MutableList<Marker> = mutableListOf()
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mapView = findViewById(R.id.mapView)
        //searchView = findViewById(R.id.searchView)

        Places.initialize(applicationContext, getString(R.string.google_maps_api_key))
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this@MapActivity)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.donebtn.setOnClickListener {
            closeActivity()
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

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        setupsearchbar()
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String): Boolean {
//               // performSearch(query)
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String): Boolean {
//                // Handle search text changes if needed
//                return false
//            }
//        })
        googleMap.isMyLocationEnabled = true
        googleMap.setOnMyLocationButtonClickListener(this)
//        geofencingClient = LocationServices.getGeofencingClient(this)
//        if (ContextCompat.checkSelfPermission(this@MapActivity,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED ||
//            ContextCompat.checkSelfPermission(
//                this@MapActivity,
//                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this@MapActivity,
//                arrayOf(
//                    android.Manifest.permission.ACCESS_FINE_LOCATION,
//                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
//                ),
//                LOCATION_PERMISSION_REQUEST_CODE
//            )
//        } else {
//           // startGeofencing(28.4150,79.4404)
//        }
    }

    private var currentMarker: Marker? = null
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

//    private fun performSearch(query: String, currentLocation: LatLng) {
//        val retrofit = Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        val geocodingService = retrofit.create(GeocodingService::class.java)
//        val call = geocodingService.getGeocode(
//            query,
//            getString(R.string.google_maps_api_key),
//            "${currentLocation.latitude - 0.1},${currentLocation.longitude - 0.1}|${currentLocation.latitude + 0.1},${currentLocation.longitude + 0.1}"
//        )
//
//        call.enqueue(object : Callback<GeocodingResponse> {
//            override fun onResponse(
//                call: Call<GeocodingResponse>,
//                response: Response<GeocodingResponse>
//            ) {
//                if (response.isSuccessful) {
//                    val geocodingResponse = response.body()
//                    if (geocodingResponse?.results?.isNotEmpty() == true) {
//                        val result = geocodingResponse.results[0]
//                        val location = result.geometry?.location
//                        if (location != null) {
//                            val latLng = LatLng(location.lat, location.lng)
//                            currentMarker?.remove() // Remove previous marker
//                            currentMarker = googleMap.addMarker(MarkerOptions().position(latLng).title(query))
//                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
//
//                            currentLatitude = location.lat
//                            currentLongitude = location.lng
//
//                            // Use currentLatitude and currentLongitude as needed
//                            // For example, log or display them
//                            Toast.makeText(this@MapActivity, "Latitude: $currentLatitude, Longitude: $currentLongitude", Toast.LENGTH_SHORT).show()
//                            Log.d("MapActivity", "Latitude: $currentLatitude, Longitude: $currentLongitude")
//                        }
//                    } else {
//                        showToast("No results found for the query: $query")
//                    }
//                } else {
//                    showToast("Geocoding request failed")
//                }
//            }
//
//            override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
//                showToast("Geocoding request failed")
//            }
//        })
//    }

    private fun setupsearchbar(){
        // Set up search bar
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                for (marker in markersList) {
                    marker.remove()
                }
                // Add marker to map at selected place's location
                val markerOptions = MarkerOptions().position(place.latLng!!).title(place.name)
                val marker = googleMap?.addMarker(markerOptions)
                if (marker != null) {
                    markersList.add(marker)
                }
                // Animate camera to selected place's location
                place.latLng?.let { CameraUpdateFactory.newLatLngZoom(it, 15f) }
                    ?.let { googleMap?.animateCamera(it) }


                val latitude = place.latLng?.latitude
                val longitude = place.latLng?.longitude
                if (latitude != null && longitude != null) {
                    // Use latitude and longitude as needed
                    currentLongitude = longitude
                    currentLatitude = latitude
                }
                // Set click listener on marker to display place name and address in ListView
                if (marker != null) {
                    marker.tag = place
                }
            }
            override fun onError(status: Status) {
                Toast.makeText(this@MapActivity, "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

//    private fun performSearch(query: String) {
//        val placesClient = Places.createClient(this)
//        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
//
//        // Create a LocationRequest to get the device's current location
//        val locationRequest = LocationRequest.create()
//            .setInterval(10000)
//            .setFastestInterval(5000)
//            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//
//        val locationCallback = object : LocationCallback() {
//            override fun onLocationResult(p0: LocationResult) {
//                p0 ?: return
//                val lastLocation = p0.lastLocation
//
//                // Create a FindAutocompletePredictionsRequest with the device's location
//                val request = FindAutocompletePredictionsRequest.builder()
//                    .setTypeFilter(TypeFilter.ADDRESS)
//                    .setQuery(query)
//                    .setLocationRestriction(
//                        RectangularBounds.newInstance(
//                            LatLng(lastLocation!!.latitude - 0.1, lastLocation.longitude - 0.1),
//                            LatLng(lastLocation.latitude + 0.1, lastLocation.longitude + 0.1)
//                        )
//                    )
//                    .build()
//
//                placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
//                    if (response.autocompletePredictions.isNotEmpty()) {
//                        val prediction = response.autocompletePredictions[0]
//
//                        placesClient.fetchPlace(
//                            FetchPlaceRequest.newInstance(
//                                prediction.placeId,
//                                fields
//                            )
//                        )
//                            .addOnSuccessListener { fetchResponse ->
//                                val place = fetchResponse.place
//                                val latLng = place.latLng
//
//                                currentMarker?.remove()
//                                currentMarker =
//                                    latLng?.let { MarkerOptions().position(it).title(place.name) }
//                                        ?.let {
//                                            googleMap.addMarker(
//                                                it
//                                            )
//                                        }
//                                latLng?.let { CameraUpdateFactory.newLatLngZoom(it, 12f) }
//                                    ?.let { googleMap.moveCamera(it) }
//
//                                currentLatitude = latLng!!.latitude
//                                currentLongitude = latLng.longitude
//
////                                Toast.makeText(
////                                    this@MapActivity,
////                                    "Latitude: $currentLatitude, Longitude: $currentLongitude",
////                                    Toast.LENGTH_SHORT
////                                ).show()
//                                Log.d(
//                                    "MapActivity",
//                                    "Latitude: $currentLatitude, Longitude: $currentLongitude"
//                                )
//                                // Perform any additional actions with the retrieved location
//                                // or pass it to other functions as needed
//
//                            }
//                            .addOnFailureListener { exception ->
//                                showToast("Failed to fetch place details: ${exception.localizedMessage}")
//                            }
//                    } else {
//                        showToast("No results found")
//                    }
//                }.addOnFailureListener { exception ->
//                    Log.d("Map", "Autocomplete request failed: ${exception.localizedMessage}")
//                    showToast("Autocomplete request failed: ${exception.localizedMessage}")
//                }
//            }
//        }
//
//// Request location updates
//        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
//        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
//    }


//    private fun performSearch(query: String) {
//        val retrofit = Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        val geocodingService = retrofit.create(GeocodingService::class.java)
//        val call = geocodingService.getGeocode(query, getString(R.string.google_maps_api_key))
//
//        call.enqueue(object : Callback<GeocodingResponse> {
//            override fun onResponse(
//                call: Call<GeocodingResponse>,
//                response: Response<GeocodingResponse>
//            ) {
//                if (response.isSuccessful) {
//                    val geocodingResponse = response.body()
//                    if (geocodingResponse?.results?.isNotEmpty() == true) {
//                        val result = geocodingResponse.results[0]
//                        val location = result.geometry?.location
//                        if (location != null) {
//                            val latLng = LatLng(location.lat, location.lng)
//                            currentMarker?.remove() // Remove previous marker
//                            currentMarker = googleMap.addMarker(MarkerOptions().position(latLng).title(query))
//                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
//
//                            currentLatitude = location.lat
//                            currentLongitude = location.lng
//
//
//
//
//                            // Use currentLatitude and currentLongitude as needed
//                            // For example, log or display them
//                            Toast.makeText(this@MapActivity,"Latitude: $currentLatitude, Longitude: $currentLongitude",Toast.LENGTH_SHORT).show()
//                            Log.d("MapActivity", "Latitude: $currentLatitude, Longitude: $currentLongitude")
//                        }
//                    } else {
//                        showToast("No results found")
//                    }
//                } else {
//                    showToast("Geocoding request failed")
//                }
//            }
//
//            override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
//                showToast("Geocoding request failed")
//            }
//        })
//    }



    private fun closeActivity() {
        val companyId = intent.getStringExtra("companyId")
        val companyName = intent.getStringExtra("companyName")
        val industry = intent.getStringExtra("industry")
        val managingDirector = intent.getStringExtra("managingDirector")
        val mobile = intent.getStringExtra("mobile")
        val email = intent.getStringExtra("email")
        val address = intent.getStringExtra("address")
        val radius = intent.getStringExtra("radius")

        if (currentLatitude != 0.0 && currentLongitude != 0.0) {
            val intent = Intent()
            intent.putExtra("companyId", companyId)
            intent.putExtra("companyName", companyName)
            intent.putExtra("industry", industry)
            intent.putExtra("managingDirector", managingDirector)
            intent.putExtra("mobile", mobile)
            intent.putExtra("email", email)
            intent.putExtra("address", address)
            intent.putExtra("locationLat", currentLatitude.toString())
            intent.putExtra("locationLong", currentLongitude.toString())
            intent.putExtra("radius", radius)
            setResult(Activity.RESULT_OK, intent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

//
//    @SuppressLint("MissingPermission")
//    private fun startGeofencing(latitude: Double, longitude: Double) {
//        val geofence = Geofence.Builder()
//            .setRequestId("geofence_id")
//            .setCircularRegion(
//                latitude,  // Replace with the desired latitude
//                longitude, // Replace with the desired longitude
//                1000F     // Replace with the desired radius in meters
//            )
//            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
//            .setExpirationDuration(Geofence.NEVER_EXPIRE)
//            .build()
//
//        googleMap.addMarker(MarkerOptions().position(LatLng(latitude, longitude)).title("Geofence Marker"))
//        googleMap.addCircle(
//            CircleOptions()
//                .center(LatLng(latitude, longitude))
//                .radius(1000.0)
//                .strokeColor(Color.RED)
//                .fillColor(Color.argb(70, 255, 0, 0))
//        )
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 15f))
//
//        val geofencingRequest = GeofencingRequest.Builder()
//            .addGeofence(geofence)
//            .build()
//
//        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
//            .setAction("com.example.yourapp.ACTION_GEOFENCE_EVENT")
//        val pendingIntent = PendingIntent.getBroadcast(
//            this,
//            2607,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
//        )
//
//        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
//            .addOnSuccessListener {
//                showToast("Geofence added successfully")
//                Log.d(TAG, "Geofence added successfully")
//            }
//            .addOnFailureListener {
//                showToast("Failed to add geofence: ${it.message}")
//                Log.e(TAG, "Failed to add geofence: ${it.message}")
//            }
//    }

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

//
//
//    private fun getCurrentLocation(): LatLng? {
//        // Get the current location using a location provider (e.g., FusedLocationProviderClient)
//        // Replace this with your actual code to get the current location
//
//        // Example code to get a mock current location
//        val latitude = 37.7749
//        val longitude = -122.4194
//        return LatLng(latitude, longitude)
//    }
    private fun addMarkerToCurrentLocation(location: LatLng) {
        googleMap.addMarker(MarkerOptions().position(location).title("Current Location"))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }


    companion object {
        private const val TAG = "MapActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }



    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    data class GeocodingResponse(
        @SerializedName("results")
        val results: List<Result>,
        @SerializedName("status")
        val status: String
    )

    data class Result(
        @SerializedName("geometry")
        val geometry: Geometry?
    )

    data class Geometry(
        @SerializedName("location")
        val location: Location?
    )

    data class Location(
        @SerializedName("lat")
        val lat: Double,
        @SerializedName("lng")
        val lng: Double
    )

    override fun onMyLocationButtonClick(): Boolean {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        addMarkerToCurrentLocation(currentLocation)
                        currentLatitude = location.latitude
                        currentLongitude = location.longitude

                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to retrieve location: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        return true
    }
}