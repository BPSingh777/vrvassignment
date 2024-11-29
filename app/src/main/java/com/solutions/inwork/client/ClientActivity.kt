package com.solutions.inwork.client

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.solutions.inwork.GpsNotificationService
import com.solutions.inwork.LogInActivity
import com.solutions.inwork.R
import com.solutions.inwork.client.fragments.ClientHomeFragment
import com.solutions.inwork.client.fragments.ClientProfileFragment
import com.solutions.inwork.client.fragments.NoticeFragment
import com.solutions.inwork.client.fragments.NotificationClientFragment
import com.solutions.inwork.client.fragments.PostLeaveFragment
import com.solutions.inwork.databinding.ActivityClientBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.solutions.inwork.client.adapter.MemeAdapter
import com.solutions.inwork.client.adapter.NewsAdapter
import com.solutions.inwork.client.adapter.PermissionAdapter
import com.solutions.inwork.client.dataclasses.MemeData
import com.solutions.inwork.client.dataclasses.NewsResponse
import com.solutions.inwork.client.dataclasses.PermissionItem
import com.solutions.inwork.client.fragments.CheckEvent
import com.solutions.inwork.client.fragments.ClientLeaveFragment
import com.solutions.inwork.client.fragments.ContactUsFragment
import com.solutions.inwork.retrofitget.RetrofitGetMeme
import com.solutions.inwork.retrofitget.RetrofitNewsGet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientActivity : AppCompatActivity() {

    private lateinit var binding : ActivityClientBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var username : TextView
    private lateinit var email : TextView
    private val permissionsList = listOf(
        PermissionItem("Location", "For usage of maps."),
        PermissionItem("Background Location","Auto CheckIn/CheckOut"),
        PermissionItem("ScreenTime","Screen time analysis for productivity"),
        PermissionItem("Phone","For contacting us"),
        PermissionItem("Notifications","To Post Notifications")
    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val serviceIntent = Intent(this, GpsNotificationService::class.java)

        startService(serviceIntent)



        val fragmentName = intent.getStringExtra("fragment")
        Log.d("fg",fragmentName.toString())
        if (fragmentName  == "NoticeFragment") {
            Log.d("fg","true")
            // Open the desired fragment
            loadFragment(NoticeFragment())
        }else if(fragmentName == "NotificationClientFragment"){
            loadFragment(NotificationClientFragment())
        }
        else{
            loadFragment(ClientHomeFragment())
        }




        binding.fab.setOnClickListener {
            showEditTextDialog()
        }

        //startGpsForegroundService()
        setUpDrawerLayout()
        binding.appbartextclient.text = "Home"
        //

        binding.profile.setOnClickListener {
            loadFragment(ClientProfileFragment())
            binding.appbartextclient.text = "Profile"
        }

        binding.menubtn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = getCurrentFragment()
            currentFragment?.let {
                updateAppBarText(it)
            }
        }


        binding.bottomNavigation.background = null
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.bottom_home ->loadFragment(ClientHomeFragment())
                R.id.bottom_addgeo -> startActivity(Intent(this,GeofenceActivity::class.java))
                R.id.bottom_notices ->loadFragment(NoticeFragment())
                R.id.bottom_notification -> loadFragment(NotificationClientFragment())

            }
            true

        }



    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen (GravityCompat.START)) {
            binding.drawerLayout.closeDrawer (GravityCompat.START)
        } else {
            val currentFragment = getCurrentFragment()
            if (currentFragment is ClientHomeFragment) {
                finish()
            } else {
                super.onBackPressed()
            }
        }
    }





    @SuppressLint("SuspiciousIndentation")
    private fun setUpDrawerLayout() {

        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, 0, R.string.app_name)
        toggle.syncState()


        val sharedPreferences1 = getSharedPreferences("CLIENT_DETAILS", MODE_PRIVATE)
        val name = sharedPreferences1.getString("name","Username")

        // Find the navigation header view
        val navigationView = findViewById<NavigationView>(R.id.ClientNavigationView)
        val headerView = navigationView.getHeaderView(0)

        val email = headerView.findViewById<TextView>(R.id.Emailtxt)

        email.text = FirebaseAuth.getInstance().currentUser?.email

        //
        username = headerView.findViewById(R.id.UserName)
        username.text = name


        binding.ClientNavigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.homebtn -> {
                    binding.appbartextclient.text = "Home"
                    loadFragment(ClientHomeFragment())
                    //   Toast.makeText(applicationContext, "Home", Toast.LENGTH_SHORT).show()
                }
                R.id.profilebtn -> {
                    binding.appbartextclient.text = "Profile"
                    loadFragment(ClientProfileFragment())
                    //   Toast.makeText(applicationContext, "Profile", Toast.LENGTH_SHORT).show()
                }

                R.id.reportbtn -> {
                    binding.appbartextclient.text = "Notices"
                    loadFragment(NoticeFragment())
                    //   Toast.makeText(applicationContext, "Reports", Toast.LENGTH_SHORT).show()
                }
                R.id.attndncbtn -> {
                    startActivity(Intent(this,GeofenceActivity::class.java))
                    //  Toast.makeText(applicationContext, "Geofence", Toast.LENGTH_SHORT).show()
                }
                R.id.checkevent -> {
                    binding.appbartextclient.text = "Check Event"
                    loadFragment(CheckEvent())
                    //   Toast.makeText(applicationContext, "Post Leave", Toast.LENGTH_SHORT).show()
                }
                R.id.postleave -> {
                    binding.appbartextclient.text = "Post Leave"
                    loadFragment(PostLeaveFragment())
                    //   Toast.makeText(applicationContext, "Post Leave", Toast.LENGTH_SHORT).show()
                }
                R.id.leavehistory ->{
                    binding.appbartextclient.text = "Leave History"
                    loadFragment(ClientLeaveFragment())
                }

                R.id.contactUsbtn -> {
                    binding.appbartextclient.text = "Contact Us"
                    loadFragment(ContactUsFragment())
                }

                R.id.memebtn -> NewsDialog()

                R.id.SettingsBtn -> PermissionDialog()



                R.id.logoutbtn -> {

                    val sharedPreferences = getSharedPreferences("ScreenTime_Details",Context.MODE_PRIVATE)
                    sharedPreferences.edit().putBoolean("ScreenDetails",true).apply()
                    val sharedPreferences2 = getSharedPreferences("Geofence_dialog",Context.MODE_PRIVATE)
                    sharedPreferences2.edit().putBoolean("Geo",true).apply()
                    getAdminFCM()
                    // Assuming you have the reference to the Service instance
                    val serviceIntent = Intent(this, GpsNotificationService::class.java)

                    // Stop the foreground service
                    stopService(serviceIntent)

                }
            }

            binding.drawerLayout.closeDrawer(GravityCompat.START)

            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.clientContainer,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.clientContainer)
    }

    private fun updateAppBarText(fragment: Fragment) {
        binding.appbartextclient.text = when (fragment) {
            is ClientHomeFragment-> "Home"
            is NotificationClientFragment -> "Notification"
            is ClientProfileFragment -> "Profile"
            is NoticeFragment -> "Notices"
            is CheckEvent -> "Check Event"
            is PostLeaveFragment -> "Post Leave"
            is ClientLeaveFragment -> "Leave History"
            is ContactUsFragment -> "Contact Us"
            else -> "Unknown Fragment"
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun PermissionDialog(){
        val dialogView = layoutInflater.inflate(R.layout.dialog_permission_layout, null)
        val dialog = AlertDialog.Builder(ContextThemeWrapper(this, R.style.CustomDialogTheme))
            .setView(dialogView)
            .create()

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.dialogRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PermissionAdapter(permissionsList, dialog)
        dialog.show()
    }
    private fun MemeDialog(){
        val dialogView = layoutInflater.inflate(R.layout.enterntainment_dialog,null)
        val dialog = AlertDialog.Builder(ContextThemeWrapper(this, R.style.CustomDialogTheme))
            .setView(dialogView)
            .create()
        val progressbar = dialogView.findViewById<ProgressBar>(R.id.newsPgbar)
        val closebtn = dialogView.findViewById<ImageView>(R.id.closenews)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.newsRecyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        progressbar.visibility = View.VISIBLE


        val call = RetrofitGetMeme.apiService.getData(100,"dank")
        call.enqueue(object : Callback<MemeData>{
            override fun onResponse(call: Call<MemeData>, response: Response<MemeData>) {
                if (response.isSuccessful){
                    val memeResponse = response.body()?.memes?.toList()
                    recyclerView.adapter = memeResponse?.let { MemeAdapter(it) }
                    progressbar.visibility = View.GONE
                    Log.d("MemeData1",memeResponse.toString())
                }
                else{
                    progressbar.visibility = View.GONE
                    Log.d("MemeData2",response.message().toString())
                }
            }

            override fun onFailure(call: Call<MemeData>, t: Throwable) {
                progressbar.visibility = View.GONE
                Log.d("MemeData3",t.toString())
            }

        })

        dialog.show()

        closebtn.setOnClickListener {
            dialog.dismiss()
        }


    }
    @SuppressLint("MissingInflatedId")
    private fun NewsDialog(){
        val dialogView = layoutInflater.inflate(R.layout.enterntainment_dialog,null)
        val dialog = AlertDialog.Builder(ContextThemeWrapper(this, R.style.CustomDialogTheme))
            .setView(dialogView)
            .create()
        val progressbar = dialogView.findViewById<ProgressBar>(R.id.newsPgbar)
        val closebtn = dialogView.findViewById<ImageView>(R.id.closenews)



        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.newsRecyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        progressbar.visibility = View.VISIBLE

        val call = RetrofitNewsGet.apiService.getData("in", apiKey = "3df7c7834b50487b8962e1592e811d5d")
        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful) {
                    val newsResponse = response.body()?.articles?.toList()
                    recyclerView.adapter = newsResponse?.let { NewsAdapter(it) }
                    Log.d("NewsApi1",newsResponse.toString())
                    progressbar.visibility = View.GONE

                } else {
                    Log.d("NewsApi2",response.message())
                    progressbar.visibility = View.GONE
                    // Handle error
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                // Handle failure
                Log.d("NewsApi3",t.toString())
                progressbar.visibility = View.GONE
            }
        })

        dialog.show()

        closebtn.setOnClickListener {
            dialog.dismiss()
        }



    }
    @SuppressLint("MissingInflatedId")
    private fun showEditTextDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sos, null)
        val editText = dialogView.findViewById<EditText>(R.id.sosText)
        editText.setText("Emergency Situation")
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Enter SOS Text")

        dialogBuilder.setPositiveButton("Send") { _, _ ->
            val enteredText = editText.text.toString()

            location(enteredText)
            // Handle the text entered in EditText and perform any necessary actions
            // For example, you can save the text to a database or display it on the screen.
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }
    private fun getEmployeeToken(latitude : String,longitude : String,message:String){

        val sharedPreferences = getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val employeeName = sharedPreferences.getString("name", null)
        val companyID = sharedPreferences.getString("company_id", null) ?:return


        val database = FirebaseDatabase.getInstance()
        val reference = database.reference

        reference.child(companyID).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val fcmList = mutableListOf<String>()
                for (employeeSnapshot in snapshot.children) {
                    val employeeId = employeeSnapshot.key
                    val fcmToken = employeeSnapshot.child("fcm").getValue(String::class.java)

                    // Process the FCM token here (e.g., store it in a list, send notifications, etc.)
                    if (fcmToken != null) {
                        fcmList.add(fcmToken)
                        // Do something with the FCM token
                        // For example, you can store them in a list or use them to send notifications
                        // adminTokensList.add(fcmToken)
                    }

                }
                if (fcmList.isNotEmpty()) {
                    GpsNotificationService().GpsNotification("$employeeName S.O.S","$message $latitude $longitude",fcmList,this@ClientActivity)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error if fetching data is unsuccessful
            }

        })


    }
    private fun location(message: String){

        val sharedPreferences = getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val employeeName = sharedPreferences.getString("name", null)
        val companyID = sharedPreferences.getString("company_id", null) ?:return

        val locationHelper = LocationHelper(this)
        locationHelper.getCurrentLocation(object : LocationHelper.LocationListener {
            override fun onLocationReceived(location: Location) {
                val latitude = location.latitude
                val longitude = location.longitude
                // Do something with latitude and longitude

                Log.d("timestamp2","$latitude $longitude")

                Toast.makeText(this@ClientActivity,"SOS message sent",Toast.LENGTH_SHORT).show()
                getEmployeeToken(latitude.toString(),longitude.toString(),message)
                GpsNotificationService().getAdminFCM("$employeeName SOS","$message $latitude $longitude",applicationContext)

                var isselected = true


                //   Toast.makeText(requireContext(),"$latitude $longitude",Toast.LENGTH_SHORT).show()
            }

            override fun onLocationFailed() {
                getEmployeeToken("","",message)
                GpsNotificationService().getAdminFCM("$employeeName SOS","$message",applicationContext)

                //                dismissprogressbar()
                Log.d("LocationBg","failed to fetch")
                // Handle location retrieval failure
            }

            override fun onProviderDisabled() {
                getEmployeeToken("","",message)
                GpsNotificationService().getAdminFCM("$employeeName SOS","$message",applicationContext)
            }

        })



    }
    fun getAdminFCM() {

        val sharedPreferences = getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        //  val employeeName = sharedPreferences.getString("name", null)
        val companyID = sharedPreferences.getString("company_id", null)



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

                                Log.d("tokenRemove1",fcm.toString())
                                fcmList.addAll(fcm)
                            }
                        }
                        if (fcmList.isNotEmpty()) {
                            deleteAdmintoken(fcmList)
                            // GpsNotification(title,"$employeeName's $message",fcmList)
                        }else{
                            FirebaseAuth.getInstance().signOut()
                            val sharedPreferences = getSharedPreferences(
                                "CLIENT_PREF",
                                Context.MODE_PRIVATE
                            )
                            val sharedPreferences1 = getSharedPreferences(
                                "CLIENT_DETAILS",
                                Context.MODE_PRIVATE
                            )
                            val editor = sharedPreferences.edit()
                            val editor1 = sharedPreferences1.edit()
                            //editor.putString("company_id",null)
                            editor.putString("fcm", null)
                            editor1.putString("company_id", null)
                            editor1.putString("company_name", null)
                            editor1.putString("employee_id", null)
                            editor1.putString("name", null)
                            editor.apply()
                            editor1.apply()

                            startActivity(
                                Intent(
                                    this@ClientActivity,
                                    LogInActivity::class.java
                                )
                            )
                            Toast.makeText(
                                applicationContext,
                                "Logout",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
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
    fun deleteAdmintoken(adminFcmList : List<String>){
        // Assuming you have the employee's unique identifier and their FCM token
        val sharedPreferences = getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val employeeId  = sharedPreferences.getString("employee_id",null)
        val companyId = sharedPreferences.getString("company_id",null)



        FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
            // Store the user's device token in Firebase Realtime Database
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            for (adminFcmtoken in adminFcmList) {

                Log.d("tokenremove", "$adminFcmtoken  $fcmToken")

                if (userId != null && adminFcmtoken == fcmToken) {

                    // Toast.makeText(this,"Device token $token",Toast.LENGTH_SHORT).show()
                    Log.d("tokenremove", "Token equal")
                    val database = FirebaseDatabase.getInstance().reference
                    val employeeTokensRef =
                        database.child(companyId.toString()).child(employeeId.toString())

                    employeeTokensRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                val tokensData = dataSnapshot.getValue<HashMap<String, Any>>()
                                tokensData?.let { tokensMap ->
                                    val tokens =
                                        tokensMap.values.filterIsInstance<String>().toMutableList()

                                    // If the token exists, delete it from the list
                                    if (tokens.contains(fcmToken)) {
                                        tokens.remove(fcmToken)
                                        tokensMap.values.removeAll { it == fcmToken }
                                        employeeTokensRef.setValue(tokensMap)
                                            .addOnSuccessListener {
                                                // Token removed successfully
                                                Log.d("tokenremove", "Token removed successfully")

                                                // Assuming you have the reference to the Service instance
                                                val serviceIntent = Intent(this@ClientActivity, GpsNotificationService::class.java)

                                                // Stop the foreground service
                                                stopService(serviceIntent)
                                                FirebaseAuth.getInstance().signOut()
                                                val sharedPreferences = getSharedPreferences(
                                                    "CLIENT_PREF",
                                                    Context.MODE_PRIVATE
                                                )
                                                val sharedPreferences1 = getSharedPreferences(
                                                    "CLIENT_DETAILS",
                                                    Context.MODE_PRIVATE
                                                )
                                                val editor = sharedPreferences.edit()
                                                val editor1 = sharedPreferences1.edit()
                                                //editor.putString("company_id",null)
                                                editor.putString("fcm", null)
                                                editor1.putString("company_id", null)
                                                editor1.putString("company_name", null)
                                                editor1.putString("employee_id", null)
                                                editor1.putString("name", null)
                                                editor.apply()
                                                editor1.apply()
                                                startActivity(
                                                    Intent(
                                                        this@ClientActivity,
                                                        LogInActivity::class.java
                                                    )
                                                )
                                                Toast.makeText(
                                                    applicationContext,
                                                    "Logout",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                finish()
                                            }
                                            .addOnFailureListener { exception ->
                                                // Error occurred while removing the token
                                                // Handle the error accordingly
                                                Log.d(
                                                    "tokenremove",
                                                    "Token remove failed: ${exception.message}"
                                                )
                                            }
                                    }
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Error occurred while accessing the database
                            // Handle the error accordingly
                            Log.d("tokenremove", "Token Error: ${databaseError.message}")
                        }
                    })


                }

            }



            Log.d("tokenremov","No token $fcmToken ")
            FirebaseAuth.getInstance().signOut()
            val sharedPreferences = getSharedPreferences(
                "CLIENT_PREF",
                Context.MODE_PRIVATE
            )
            val sharedPreferences1 = getSharedPreferences(
                "CLIENT_DETAILS",
                Context.MODE_PRIVATE
            )
            val editor = sharedPreferences.edit()
            val editor1 = sharedPreferences1.edit()
            //editor.putString("company_id",null)
            editor.putString("fcm", null)
            editor1.putString("company_id", null)
            editor1.putString("company_name", null)
            editor1.putString("employee_id", null)
            editor1.putString("name", null)
            editor.apply()
            editor1.apply()

            Log.d("EmpCred","${sharedPreferences1.getString("name",null)}")


            startActivity(
                Intent(
                    this@ClientActivity,
                    LogInActivity::class.java
                )
            )
            Toast.makeText(
                applicationContext,
                "Logout",
                Toast.LENGTH_SHORT
            ).show()
            finish()

        }


    }


}