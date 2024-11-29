package com.solutions.inwork.client.fragments

import android.Manifest
import com.google.android.gms.common.api.ResolvableApiException
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.ArrayMap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.solutions.inwork.GeofenceClass
import com.solutions.inwork.R
import com.solutions.inwork.client.GeofenceBroadcastReceiver
import com.solutions.inwork.client.LocationChangeDetector
import com.solutions.inwork.client.LocationHelper
import com.solutions.inwork.client.MidnightResetReceiver
import com.solutions.inwork.client.dataclasses.UniqueClient
import com.solutions.inwork.databinding.FragmentClientHomeBinding
import com.solutions.inwork.retrofitPost.RetrofitPostScreentime
import com.solutions.inwork.retrofitget.UniqueEmployeeGet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


class ClientHomeFragment : Fragment() , OnMapReadyCallback{

    private lateinit var binding: FragmentClientHomeBinding
    private lateinit var usageStatsManager: UsageStatsManager
    private val MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 100
//    private val REQUEST_ENABLE_GPS = 1001
    private lateinit var locationChangeDetector: LocationChangeDetector
    private var totalUsageTime : Long = 0
    private var whatsappDuration: String = ""
    private var facebookDuration: String = ""
    private var instagramDuration: String = ""
    private var twitterDuration: String = ""
    private var newsDuration: String = ""
    private var gamesDuration: String = ""
    private var callsDuration: String = ""
    private var othersDuration: String = ""
    private lateinit var mUsageStatsManager: UsageStatsManager
    private lateinit var mPm: PackageManager
    private var progressDialog: ProgressDialog? = null
    private val mPackageStats = ArrayList<UsageStats>()
    private lateinit var mapView: MapView
    private lateinit var googleMap : GoogleMap



    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentClientHomeBinding.inflate(inflater, container, false)

        mapView = binding.locMap
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)



        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ){

            refresh()

            val sharedPreferences2 = requireContext().getSharedPreferences("Geofence_dialog",Context.MODE_PRIVATE)
            val bool = sharedPreferences2.getBoolean("Geo",true)
            if(bool){
                AlertDialog.Builder(context)
                    .setTitle("Geofence")
                    .setMessage("Please click Add Geofence for automatic attendance.")
                    .setPositiveButton("OK") { dialog, which ->


                        val locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager

                        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

                            AddGeofence()


                        }else{
                            AlertDialog.Builder(context)
                                .setTitle("Enable GPS")
                                .setMessage("Please enable GPS to use this feature.")
                                .setPositiveButton("OK") { dialog, which ->
                                    // Turn on GPS]\
                                    dialog.dismiss()
                                  val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                   context?.startActivity(settingsIntent)

                                    // Dismiss the AlertDialog


                                }
                                .setNegativeButton("Cancel") { dialog, which ->
                                    // User clicked Cancel, handle it as needed
                                    // binding.permission.text = "GPS is OFF"
                                }
                                .setOnDismissListener {
                                    AddGeofence()
                                }
                                .show()
                        }





                        with(sharedPreferences2.edit()){
                            putBoolean("Geo",false)
                            apply()
                        }

                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        // User clicked Cancel, handle it as needed
                        // binding.permission.text = "GPS is OFF"
                    }
                    .show()
            }



            usageStatsManager =
                (activity?.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager)!!
            val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                requireContext().packageName
            )

            if (mode == AppOpsManager.MODE_ALLOWED) {

                binding.sendDetails.setOnClickListener {
                    showprogressbar()
                    SendScreenTime(requireContext())
                }
                printAppUsageTime(requireContext())

            } else {


                binding.sendDetails.setOnClickListener {
                    MidnightResetReceiver().SendScreenTimeShift(requireContext())
                }
                val sharedPreferences = requireContext().getSharedPreferences("ScreenTime_Details",Context.MODE_PRIVATE)
                val bool = sharedPreferences.getBoolean("ScreenDetails",true)
                if(bool){
                    Toast.makeText(requireContext(), "Permission denied give access in settings", Toast.LENGTH_SHORT).show()

                    startActivityForResult(
                        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                        MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS
                    )
                    with(sharedPreferences.edit()){
                        putBoolean("ScreenDetails",false)
                        apply()
                    }

                }


                // Permission is not granted, request for permission

            }




        }else{
            requestBackgroundPermission()
        }

        binding.refreshbtn.setOnClickListener {
            requireFragmentManager().beginTransaction().detach(this).commitNow();
            requireFragmentManager().beginTransaction().attach(this).commitNow();

        }

        binding.Takepermission.setOnClickListener {
            startActivityForResult(
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS
            )
        }

        return binding.root
    }








    @RequiresApi(Build.VERSION_CODES.Q)
    fun refresh(){









        mapView.getMapAsync(this)

        buttonDetectGPS()

        val sharedPreferences  = requireContext().getSharedPreferences("CHECK_STATUS",Context.MODE_PRIVATE)
        val status = sharedPreferences.getString("status","Check In/Out Status")
        // val currentstatus = sharedPreferences.getString("bool","")
        Log.d("Status", status.toString())

        val geofence = requireContext().getSharedPreferences("geofence_activate",Context.MODE_PRIVATE).getBoolean("geofence_activation",false)

        binding.ckeckStatus.apply {

            if (!geofence){
                text = "Add Geofence"
            }else{
                if (status == "yes"){
                    text = "You have checked In"
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.GREEN))
                }else{
                    text = "You have checked Out"
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.RED))
                }
            }

        }


        binding.geofencebtn.setOnClickListener {


            val locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager

            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

            AddGeofence()


            }else{
                AlertDialog.Builder(context)
                    .setTitle("Enable GPS")
                    .setMessage("Please enable GPS to use this feature.")
                    .setPositiveButton("OK") { dialog, which ->
                        // Turn on GPS
                        val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context?.startActivity(settingsIntent)
                        // Dismiss the AlertDialog
                        dialog.dismiss()

                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        // User clicked Cancel, handle it as needed
                        // binding.permission.text = "GPS is OFF"
                    }
                    .show()
            }


        }







        binding.showmorebtn.setOnClickListener {
            binding.screenLinearLayout.visibility = View.VISIBLE
            binding.showmorebtn.visibility = View.GONE
        }
        binding.showlessbtn.setOnClickListener {
            binding.screenLinearLayout.visibility = View.GONE
            binding.showmorebtn.visibility = View.VISIBLE
        }





        //  checkLocationStatus()
        resetvar()
//
//        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//            == PackageManager.PERMISSION_GRANTED
//        ) {
//            // Location permission has been granted
//            location()
//            locationChangeDetector = LocationChangeDetector(requireContext())
//            locationChangeDetector.startLocationUpdates()
//        } else {
//            Log.d("permission","Location permission not given")
//
//        }

        //   batteryBroadcast()




    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundPermission(){

//            Toast.makeText(requireContext(),"Permission not granted",Toast.LENGTH_SHORT).show()

            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                    // Manifest.permission.READ_EXTERNAL_STORAGE,
                    // Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                123
            )
    }

    private fun AddGeofence(){

        val currentApiVersion = Build.VERSION.SDK_INT
        if (currentApiVersion <= Build.VERSION_CODES.P) {

            val geofence = GeofenceClass(requireContext())
            geofence.fetchCoordinates()


        } else {
            // Android version is higher than 9

            Log.d("Version1","12")
            val locationPermission = Manifest.permission.ACCESS_BACKGROUND_LOCATION

            val backgroundLocationPermissionGranted =
                ContextCompat.checkSelfPermission(requireContext(), locationPermission) == PackageManager.PERMISSION_GRANTED
//            requestpermission()
            //  Log.d("permission",requestpermission().toString())
            if(backgroundLocationPermissionGranted && ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED){
                // for client attendance
                val geofence = GeofenceClass(requireContext())
                geofence.fetchCoordinates()

            }else{
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    123
                )
            }


        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestBackgroundLocationPermission()
            } else {
                // Foreground location permission is denied, handle the scenario
                // ...
            }
        } else if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Background location permission is granted, proceed with your logic
                // ...
            } else {
                // Background location permission is denied, handle the scenario
                // ...
            }
        }
    }
    private fun requestBackgroundLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Background location permission is not granted, request it
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 101)
        }
    }

    private var cellinfo = ""
    @RequiresApi(Build.VERSION_CODES.Q)
    fun buttonDetectGPS() {
       val locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
          //  binding.permission.text =    "GPS is ON"
            showprogressbar()
            fetchCoordinates()

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                // Location permission has been granted
                location()
                locationChangeDetector = LocationChangeDetector(requireContext())
                locationChangeDetector.startLocationUpdates()
            } else {
                Log.d("permission","Location permission not given")

            }

        } else {

                // GPS is disabled, show the pop-up to enable GPS
                AlertDialog.Builder(context)
                    .setTitle("Enable GPS")
                    .setMessage("Please enable GPS to use this feature.")
                    .setPositiveButton("OK") { dialog, which ->
                        // Turn on GPS
                        val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context?.startActivity(settingsIntent)

                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        // User clicked Cancel, handle it as needed
                       // binding.permission.text = "GPS is OFF"
                    }
                    .show()


            val sharedPreferences = requireContext().getSharedPreferences("SIM_DETAILS",Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("sim",cellinfo)
            editor.apply()


        }
    }




    private fun fetchCoordinates() {
        val apiService = UniqueEmployeeGet.apiService

        val sharedPreferences = requireContext().getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
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

                        Log.d("radius",radius)

                       // startGeofencing(latitude.toDouble(),longitude.toDouble())
                        Log.d("employee",uniqueClient.toString())
                        // Use latitude and longitude as needed

                        val map = view?.findViewById<MapView>(R.id.locMap)

                        var isselected = true

                        binding.officeMap.setOnClickListener {
                            if (!isselected){
                                binding.locMap.visibility = View.GONE
                            }else{
                                binding.locMap.visibility = View.VISIBLE
                                Log.d("office","$map  ${latitude.toDouble()} ${longitude.toDouble()}")
                                markLocationOnMap("Office",map, latitude.toDouble(), longitude.toDouble(),radius.toDouble())
                            }
                            isselected = !isselected
                        }
                        Log.d("home co", "Latitude: $latitude, Longitude: $longitude")
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

    inner class UsageStatsAdapter {
        fun getTotalUsageTime(): Long {
            var totalUsageTime = 0L
            for (usageStats in mPackageStats) {
                totalUsageTime += usageStats.totalTimeInForeground
            }
            return totalUsageTime
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
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
        googleMap.isMyLocationEnabled = true

    }


    fun resetvar(){
        // Set up the midnight alarm
        val midnightCalendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            add(Calendar.DAY_OF_MONTH, 1) // Set alarm for the next day
        }

        val midnightIntent = Intent(requireContext(), MidnightResetReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            midnightIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )


        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            midnightCalendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        // Register the MidnightResetReceiver
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK) // Trigger the receiver every minute
        }

        requireContext().registerReceiver(MidnightResetReceiver(), intentFilter)
    }




    fun printAppUsageTime(context: Context) {

        mUsageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        mPm = context.packageManager

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)

        val stats = mUsageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            cal.timeInMillis,
            System.currentTimeMillis()
        )

        val map = ArrayMap<String, UsageStats>()
        val statCount = stats.size
        for (i in 0 until statCount) {
            val pkgStats = stats[i]

            try {
                val appInfo = mPm.getApplicationInfo(pkgStats.packageName, 0)

                val existingStats = map[pkgStats.packageName]
                if (existingStats == null) {
                    map[pkgStats.packageName] = pkgStats
                } else {
                    existingStats.add(pkgStats)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                // This package may be gone.
            }
        }
        mPackageStats.addAll(map.values)

        val appUsageStatsAdapter = UsageStatsAdapter()


        totalUsageTime = appUsageStatsAdapter.getTotalUsageTime()
        othersDuration = formatTime(totalUsageTime)

        binding.othersDurationText.isEnabled = false
        binding.othersDurationText.setText(formatTime(totalUsageTime))

      //  Toast.makeText(requireContext(), formatTime(totalUsageTime), Toast.LENGTH_SHORT).show()


        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = calendar.timeInMillis

        val queryUsageStats = usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )


        var totalUsage = 0L
        if (queryUsageStats != null) {
            for (usageStat in queryUsageStats) {
                totalUsage += usageStat.totalTimeInForeground
            }
        }

        var totalDuration = 0L
        queryUsageStats?.forEach { usageStats ->
            val packageName = usageStats.packageName
            if (packageName in listOf("com.whatsapp", "com.facebook.katana", "com.instagram.android", "com.twitter.android", "com.google.android.apps.newsstand", "com.games", "com.google.android.dialer", "com.others")) {
                val totalTime = usageStats.totalTimeInForeground
                val formattedTime = formatTime(totalTime)


                when (packageName) {
                    "com.whatsapp" -> {
                   val whstapptme = totalTime
                        binding.whatsappDurationText.isEnabled = false
                        binding.whatsappDurationText.setText(formatTime(whstapptme))
                        totalDuration =  whstapptme
//
//                      Toast.makeText(requireContext(),"total whsapp : ${formatTime(totalDuration)}",Toast.LENGTH_SHORT).show()
                        whatsappDuration = formattedTime
                    }
                    "com.facebook.katana" -> {
                        val fcbtm = totalTime
                        binding.facebookDurationText.isEnabled = false
                        binding.facebookDurationText.setText(formatTime(fcbtm))
                        totalDuration += fcbtm
                      //  Toast.makeText(requireContext(),formattedTime,Toast.LENGTH_SHORT).show()
                     //   Toast.makeText(requireContext(),"total fb: ${formatTime(totalDuration)}",Toast.LENGTH_SHORT).show()
                        facebookDuration = formattedTime
                    }
                    "com.instagram.android" -> {
                        val insta = totalTime
                        binding.instagramDurationText.isEnabled = false
                        binding.instagramDurationText.setText(formatTime(insta))
                        totalDuration += insta
                    //    Toast.makeText(requireContext(),formattedTime,Toast.LENGTH_SHORT).show()
                       // Toast.makeText(requireContext(),"total insta : ${formatTime(totalDuration)}",Toast.LENGTH_SHORT).show()
                        instagramDuration = formattedTime
                    }
                    "com.twitter.android" -> {
                        val twit = totalTime
                        binding.twitterDurationText.isEnabled = false
                        binding.twitterDurationText.setText(formatTime(twit))
                        totalDuration += twit
                      //  Toast.makeText(requireContext(),formattedTime,Toast.LENGTH_SHORT).show()
                      //  Toast.makeText(requireContext(),"total twitter: ${formatTime(totalDuration)}",Toast.LENGTH_SHORT).show()
                        twitterDuration = formattedTime
                    }
                    "com.google.android.apps.newsstand" -> {
                        val news = totalTime
                        binding.newsDurationText.isEnabled = false
                        binding.newsDurationText.setText(formatTime(news))
                    //    totalDuration += news
                    //    Toast.makeText(requireContext(),formattedTime,Toast.LENGTH_SHORT).show()
                    //    Toast.makeText(requireContext(),"total : ${formatTime(totalDuration)}",Toast.LENGTH_SHORT).show()
                        newsDuration = formattedTime
                    }
                    "com.king.candycrushsaga" -> {
                        val games = totalTime
                        binding.gamesDurationText.isEnabled = false
                        binding.gamesDurationText.setText(formatTime(games))
                   //     totalDuration += games
                    //    Toast.makeText(requireContext(),formattedTime,Toast.LENGTH_SHORT).show()
                   //     Toast.makeText(requireContext(),"total : ${formatTime(totalDuration)}",Toast.LENGTH_SHORT).show()
                        gamesDuration = formattedTime
                    }
                    "com.google.android.dialer" -> {
                        val call = totalTime
                        binding.callsDurationText.isEnabled = false
                        binding.callsDurationText.setText(formatTime(call))
                        totalDuration += call
                   //     Toast.makeText(requireContext(),formattedTime,Toast.LENGTH_SHORT).show()
                      //  Toast.makeText(requireContext(),"total call : ${formatTime(totalDuration)}",Toast.LENGTH_SHORT).show()
                     //   callsDuration = formattedTime
                    }

                }

                            when (packageName) {
                "com.whatsapp" -> whatsappDuration = formattedTime
                "com.facebook.katana" -> facebookDuration = formattedTime
                "com.instagram.android" -> instagramDuration = formattedTime
                "com.twitter.android" -> twitterDuration = formattedTime
                "com.google.android.apps.newsstand" -> newsDuration = formattedTime
                "com.king.candycrushsaga" -> gamesDuration = formattedTime
                "com.google.android.dialer" -> callsDuration = formattedTime
                // "com.android.systemui" -> othersDuration = formattedTime
            }




            }
        }

//        val totalUsagetime =  totalDuration
//
//
        binding.screentimetxt.text ="Total Duration :  ${ formatTime(totalUsageTime + totalDuration) }"

      //  binding.screentimetxt.setText("App usage JSON: $appUsageJson")
    }

    fun SendAppUsageTime(context: Context,boolean: Boolean) {

        var usageStatsManager: UsageStatsManager = (context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager)!!

        mUsageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        mPm = context.packageManager

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)

        val stats = mUsageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            cal.timeInMillis,
            System.currentTimeMillis()
        )

        val map = ArrayMap<String, UsageStats>()
        val statCount = stats.size
        for (i in 0 until statCount) {
            val pkgStats = stats[i]

            try {
                val appInfo = mPm.getApplicationInfo(pkgStats.packageName, 0)

                val existingStats = map[pkgStats.packageName]
                if (existingStats == null) {
                    map[pkgStats.packageName] = pkgStats
                } else {
                    existingStats.add(pkgStats)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                // This package may be gone.
            }
        }
        mPackageStats.addAll(map.values)

        val appUsageStatsAdapter = UsageStatsAdapter()


        totalUsageTime = appUsageStatsAdapter.getTotalUsageTime()
        othersDuration = formatTime(totalUsageTime)



        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = calendar.timeInMillis

        val queryUsageStats = usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )


        var totalUsage = 0L
        if (queryUsageStats != null) {
            for (usageStat in queryUsageStats) {
                totalUsage += usageStat.totalTimeInForeground
            }
        }

        var totalDuration = 0L
        queryUsageStats?.forEach { usageStats ->
            val packageName = usageStats.packageName
            if (packageName in listOf("com.whatsapp", "com.facebook.katana", "com.instagram.android", "com.twitter.android", "com.google.android.apps.newsstand", "com.games", "com.google.android.dialer", "com.others")) {
                val totalTime = usageStats.totalTimeInForeground
                val formattedTime = formatTime(totalTime)


                when (packageName) {
                    "com.whatsapp" -> {
                        val whstapptme = totalTime
                        totalDuration =  whstapptme
                        whatsappDuration = formattedTime
                    }
                    "com.facebook.katana" -> {
                        val fcbtm = totalTime
                        totalDuration += fcbtm
                        facebookDuration = formattedTime
                    }
                    "com.instagram.android" -> {
                        val insta = totalTime
                        totalDuration += insta
                        instagramDuration = formattedTime
                    }
                    "com.twitter.android" -> {
                        val twit = totalTime
                        totalDuration += twit
                        twitterDuration = formattedTime
                    }
                    "com.google.android.apps.newsstand" -> {
                        val news = totalTime
                        newsDuration = formattedTime
                    }
                    "com.king.candycrushsaga" -> {
                        val games = totalTime
                        gamesDuration = formattedTime
                    }
                    "com.google.android.dialer" -> {
                        val call = totalTime
                        totalDuration += call
                    }

                }

                when (packageName) {
                    "com.whatsapp" -> whatsappDuration = formattedTime
                    "com.facebook.katana" -> facebookDuration = formattedTime
                    "com.instagram.android" -> instagramDuration = formattedTime
                    "com.twitter.android" -> twitterDuration = formattedTime
                    "com.google.android.apps.newsstand" -> newsDuration = formattedTime
                    "com.king.candycrushsaga" -> gamesDuration = formattedTime
                    "com.google.android.dialer" -> callsDuration = formattedTime
                    // "com.android.systemui" -> othersDuration = formattedTime
                }




            }
        }
        if (boolean){
            SendScreenTime(context)
        }
//        val totalUsagetime =  totalDuration
//
//

        //  binding.screentimetxt.setText("App usage JSON: $appUsageJson")
    }


    fun location(){

        val locationHelper = LocationHelper(requireContext())
        locationHelper.getCurrentLocation(object : LocationHelper.LocationListener {
            override fun onLocationReceived(location: Location) {
                val latitude = location.latitude
                val longitude = location.longitude
                // Do something with latitude and longitude

                dismissprogressbar()


                binding.checkInBtn.setOnClickListener {

                    val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("CheckIn")
                        .setMessage("Do you want to manually check in?")

                    dialogBuilder.setPositiveButton("Yes") { _, _ ->


                        getAdminFCM("Attendance","manually Checked In")
                        binding.ckeckStatus.text = "You have checked In"
                        binding.ckeckStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.GREEN))
                        GeofenceBroadcastReceiver().postStatus(requireContext(),"CHECKED IN")
                        GeofenceBroadcastReceiver().PostCheckIn(requireContext(),"manual $latitude $longitude")
                        // Handle the text entered in EditText and perform any necessary actions
                        // For example, you can save the text to a database or display it on the screen.
                    }

                    dialogBuilder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }

                    val alertDialog = dialogBuilder.create()
                    alertDialog.show()




                }
                binding.checkOutBtn.setOnClickListener {


                    val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("CheckOut")
                        .setMessage("Do you want to manually check out?")

                    dialogBuilder.setPositiveButton("Yes") { _, _ ->



                        getAdminFCM("Attendance","manually Checked Out")
                        binding.ckeckStatus.text = "You have checked Out"
                        binding.ckeckStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.RED))
                        GeofenceBroadcastReceiver().postStatus(requireContext(),"CHECKED OUT")
                        GeofenceBroadcastReceiver().PostChechOut(requireContext(),"manual $latitude $longitude")
                        // Handle the text entered in EditText and perform any necessary actions
                        // For example, you can save the text to a database or display it on the screen.
                    }

                    dialogBuilder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }

                    val alertDialog = dialogBuilder.create()
                    alertDialog.show()




                }

                getCompleteAddressString(latitude,longitude)
                binding.location.text = "Latitude: $latitude Longitude: $longitude"
                val map = view!!.findViewById<MapView>(R.id.locMap)

                var isselected = true

                binding.showMap.setOnClickListener {
                    if (!isselected){
                        binding.locMap.visibility = View.GONE
                    }else{
                        binding.locMap.visibility = View.VISIBLE
                        markLocationOnMap("My location",map, latitude, longitude,100.0)
                    }
                   isselected = !isselected
                }
             //   Toast.makeText(requireContext(),"$latitude $longitude",Toast.LENGTH_SHORT).show()
            }

            override fun onLocationFailed() {
                dismissprogressbar()
                // Handle location retrieval failure
            }

            override fun onProviderDisabled() {
                dismissprogressbar()
                Toast.makeText(requireContext(),"Gps is disabled",Toast.LENGTH_SHORT).show()
            }
        })



    }


    fun markLocationOnMap(place: String, mapView: MapView?, latitude: Double, longitude: Double,radius: Double) {
        mapView?.getMapAsync { googleMap ->
            val location = LatLng(latitude, longitude)
            val marker: Any = if (place == "Office") {
                googleMap.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title(place)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )!!
                googleMap.addCircle(
            CircleOptions()
                .center(LatLng(latitude, longitude))
                .radius(radius)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(70, 255, 0, 0))
        )
            } else {
                googleMap.addMarker(MarkerOptions().position(location).title(place))!!
            }

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

            googleMap.setOnMarkerClickListener { clickedMarker ->
                if (clickedMarker == marker) {
                    // Retrieve the marker title (tag)
                    val clickedMarkerTitle = clickedMarker.title
                    clickedMarker.tag = clickedMarkerTitle

                    // Display the marker title (tag)
                    Toast.makeText(context, "$clickedMarkerTitle", Toast.LENGTH_SHORT).show()
                }
                true
            }
        }
    }


    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            var strAdd = ""
            var cityAndCountry = ""
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            try {
                val addresses: List<Address>? = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
                if (addresses != null) {
                    val returnedAddress: Address = addresses[0]
                    val cityName: String = returnedAddress.locality
                    val countryName: String = returnedAddress.countryName
                    val stateName: String = returnedAddress.adminArea
                    val locality: String = returnedAddress.subLocality
                    val postal: String = returnedAddress.postalCode
                    cityAndCountry = "locality : $locality \n city: $cityName\n postal code: $postal\n State and Country:  $stateName, $countryName"

                    withContext(Dispatchers.Main) {
                        // Update the UI with the retrieved address
                        binding.Address.text = "Address: ${returnedAddress.getAddressLine(0)}"
                        dismissprogressbar()
                    }

                    val strReturnedAddress = StringBuilder("")
                    for (i in 0..returnedAddress.maxAddressLineIndex) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                    }
                    strAdd = strReturnedAddress.toString()
                    //  Toast.makeText(this,cityAndCountry,Toast.LENGTH_SHORT).show()
                    println("My Current location address: $strReturnedAddress")
                } else {
                    withContext(Dispatchers.Main) {
                        // Handle the case when no address is returned
                      //  Toast.makeText(this@YourActivity, "Error", Toast.LENGTH_SHORT).show()

                        Log.d("Address","No Address returned!")
                       // println("My Current location address: No Address returned!")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    // Handle the exception and display an error message
                    Log.d("Address","Cannot get Address!")
                   // Toast.makeText(this@YourActivity, "Cannot get Address!", Toast.LENGTH_SHORT).show()
                  //  println("My Current location address: Cannot get Address!")
                }
            }
        }
    }


    private fun formatTime(totalTimeInForeground: Long): String {
        val seconds = totalTimeInForeground / 1000 % 60
        val minutes = totalTimeInForeground / (1000 * 60) % 60
        val hours = totalTimeInForeground / (1000 * 60 * 60)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

   fun SendScreenTime(context: Context){

       val sharedPreferences = context.getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
       val EMPLOYEEID= sharedPreferences.getString("employee_id", null) ?: return
       val companyId = sharedPreferences.getString("company_id",null) ?:return
       val companyName = sharedPreferences.getString("company_name",null) ?:return
       val workstarttime = sharedPreferences.getString("work_start_time",null) ?:return
       val workEndTime = sharedPreferences.getString("work_end_time",null) ?:return
       val name = sharedPreferences.getString("name", null) ?: return


       val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
       val currentTime = Calendar.getInstance().time
       val formattedDate= dateFormat.format(currentTime)


// Format the current date
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
       // val formattedDate = currentDate.format(dateFormatter)
        val jsonObject = JSONObject().apply {
            put("company_id", companyId)
            put("company_name", companyName)
            put("employee_id", EMPLOYEEID)
            put("employee_name", name)
            put("designation", "Employee")
            put("work_start_time", workstarttime)
            put("work_end_time", workEndTime)
            put("whatsapp_duration", whatsappDuration)
            put("facebook_duration", facebookDuration)
            put("instagram_duration", instagramDuration)
            put("twitter_duration", twitterDuration)
            put("News_duration", newsDuration)
            put("Games_duration", gamesDuration)
            put("calls_duration", callsDuration)
            put("others_duration", othersDuration)
            put("time_stamp", formattedDate)
        }

        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())

        // POST request
        val apiService = RetrofitPostScreentime.apiService
        val call = apiService.postData(requestBody)

        call.enqueue(object: Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {

                if (response.isSuccessful){
                    dismissprogressbar()
                    Log.d("SendScreentime","sucees")

                    Toast.makeText(context,"Screen time details sent successfully",Toast.LENGTH_SHORT).show()
                }else{
                    dismissprogressbar()
                    Toast.makeText(context,"Something went wrong!!",Toast.LENGTH_SHORT).show()
                    Log.d("SendScreentime",response.message())
                }

            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                dismissprogressbar()
                Toast.makeText(context,t.toString(),Toast.LENGTH_SHORT).show()
                Log.d("SendScreentime", t.toString())
            }

        })



    }

    private fun showprogressbar(){
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("Loading...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()

    }
    private fun dismissprogressbar(){
        progressDialog?.dismiss()
    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, perform the actions that require this permission
                usageStatsManager =
                    (activity?.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager)!!
                val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val mode = appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    requireContext().packageName
                )

                if (mode == AppOpsManager.MODE_ALLOWED) {
                    binding.sendDetails.setOnClickListener {
                        showprogressbar()
                        SendScreenTime(requireContext())
                    }
                    printAppUsageTime(requireContext())
                } else {
                    Toast.makeText(requireContext(), "Permission denied give access in settings", Toast.LENGTH_SHORT).show()

                    startActivityForResult(
                        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                        MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS
                    )

                    // Handle the case where the permission is still not granted even after returning from settings
                }
            } else {

                // Permission is still not granted after returning from settings
                // Handle this case accordingly
            }
        }
    }



    fun getAdminFCM( title: String,message: String) {

        val sharedPreferences = requireContext().getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
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

                        val notification = GeofenceBroadcastReceiver()
                        notification.postNotification(requireContext(),"${employeeName}'s $message",title)

                        val fcmList = mutableListOf<String>()
                        for (documentSnapshot in querySnapshot.documents) {
                            val fcm = documentSnapshot.get("fcm") as? List<String>
                            if (fcm != null) {

                                fcmList.addAll(fcm)
                            }
                        }
                        if (fcmList.isNotEmpty()) {
                            GpsNotification(title,"$employeeName's $message",fcmList)
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

    fun GpsNotification(title: String,message: String, adminFcmList: List<String>) {
        val url = "https://fcm.googleapis.com/fcm/send"
        val notificationObject = JSONObject()
        notificationObject.put("title", title)
        notificationObject.put("message", message)

//        Log.d("sent","true")
//        GeofenceBroadcastReceiver().postNotification(requireContext(),message,title)

        for (adminFcm in adminFcmList) {
            val jsonObject = JSONObject()
            jsonObject.put("data", notificationObject)
            jsonObject.put("to", adminFcm)

            Log.d("fcmto",adminFcm)
            val request: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, jsonObject,
                { response ->
                    // Handle the response here
                    Log.d("mygps", "success")
                  //  Toast.makeText(context, "GPS Successful", Toast.LENGTH_SHORT).show()
                },
                { error ->
                    // Handle the error here
                    Log.d("mygps", "fail")
                   // Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
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


}













