package com.solutions.inwork

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import com.solutions.inwork.Admin.AdminActivity
import com.solutions.inwork.Admin.dataclasses.AdminEmail
import com.solutions.inwork.client.ClientActivity
import com.solutions.inwork.client.dataclasses.ClientemailInfo
import com.solutions.inwork.databinding.ActivityLogInBinding
import com.solutions.inwork.retrofitget.RetrofitClientEmailInfo
import com.solutions.inwork.retrofitget.Retrofitgetadminemail
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LogInActivity : AppCompatActivity() {


    private lateinit var binding: ActivityLogInBinding
    private var progressDialog: ProgressDialog? = null
    private lateinit var auth : FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isReadPermissionGranted = false
    private var isLocationPermissionGranted = false
    private var isRecordPermissionGranted = false
    private var isPhonePermissionGranted = false
    private var isWritePermissionGranted = false
    //    private val permissions = arrayOf(
//        Manifest.permission.READ_EXTERNAL_STORAGE,
//        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//        Manifest.permission.ACCESS_FINE_LOCATION,
//        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
//        Manifest.permission.READ_PHONE_STATE
//        // Add more permissions here as needed
//    )
    private val PERMISSION_REQUEST_CODE = 123


    private val FINE_LOCATION_ACCESS_REQUEST_CODE = 10001


    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser!= null){
            UserLoggedIn()
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageView: ImageView = findViewById(R.id.imageView)
        val animation = AnimationUtils.loadAnimation(this, R.anim.tofro)

        // Start the animation on the ImageView
        imageView.startAnimation(animation)
//
//      setSupportActionBar(binding.topAppBar)`

        //    checkPermissions()


        textwatcher()
        //  requestBackgroundPermission()
        getNotificationPermission()
        logUser()



    }



    fun logUser(){
        val radiogroup = binding.radioGroup
        auth = FirebaseAuth.getInstance()

        if(auth.currentUser !=null){
            Log.d("Kanishk","${auth.currentUser!!.displayName.toString()} ${auth.currentUser?.email.toString()}")

        }else{
            binding.logBtn.setOnClickListener {
                val selectedRadioBtnId = radiogroup.checkedRadioButtonId
                val selectedRadioBtn = findViewById<RadioButton>(selectedRadioBtnId)

                val email = binding.loginEmail.text.toString().trim()
                val password = binding.loginPassword.text.toString().trim()

                showprogressbar()

                if (selectedRadioBtn != null && email.isNotEmpty() && password.isNotEmpty()) {
                    val loginType = selectedRadioBtn.text.toString()

                    val auth = FirebaseAuth.getInstance()

                    // Perform the login based on the selected login type
                    when (loginType) {
                        "Admin" -> {
                            // Authenticate the user as an admin
                            // Use appropriate Firebase Authentication method for admin login (e.g., email/password)
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        val sharedPreferences =
                                            getSharedPreferences("AdminPass", Context.MODE_PRIVATE)
                                        sharedPreferences.edit().putString("Pass", password).apply()
                                        sharedPreferences.edit().putString("AdminEmail", email)
                                            .apply()
                                        // Admin login success

                                        adminverification()
                                        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                                            // Store the user's device token in Firebase Realtime Database
                                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                                            if (userId != null) {
                                                // Toast.makeText(this,"Device token $token",Toast.LENGTH_SHORT).show()
                                                Log.d("Devicetoken", token)

                                                addToken("admin", email, token)
                                            }
                                        }
//                                        startActivity(Intent(this, AdminActivity::class.java))
//                                        finish()
                                    } else {
                                        // Admin login failed
                                        dismissprogressbar()
                                        Toast.makeText(
                                            this,
                                            "Admin login failed",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }

                        "Client" -> {
                            // Authenticate the user as a client
                            // Use appropriate Firebase Authentication method for client login (e.g., email/password)

                            auth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener {
                                    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                                        // Store the user's device token in Firebase Realtime Database
                                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                                        if (userId != null) {
                                            //Toast.makeText(this,"Device token $token",Toast.LENGTH_SHORT).show()
                                            Log.d("Devicetoken", token)
                                            clientVerification(token, email)
                                            addToken("admin", token, email)


                                            val sf = getSharedPreferences(
                                                "GPS_FCM",
                                                Context.MODE_PRIVATE
                                            )
                                            val ed = sf.edit()
                                            ed.putString("gps", token)
                                            ed.apply()


                                        }
                                    }
                                }.addOnFailureListener { Exception->


                                    Log.d("LoginError",Exception?.message.toString() + Exception.cause)
                                    if (Exception.message.equals("An internal error has occurred. [ INVALID_LOGIN_CREDENTIALS ]",true)){
                                        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                                            createClientUser(token,email, password)
                                        }
                                    }else{
                                        dismissprogressbar()
                                        Toast.makeText(this,Exception.message.toString(),Toast.LENGTH_SHORT).show()
                                    }


                                }


                        }
                    }
                }else {
                    dismissprogressbar()
                    Toast.makeText(this, "Fill in all the credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.forgotPassBtn.setOnClickListener {
            showRecoverPasswordDialog()
        }


        radiogroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadiobtn = findViewById<RadioButton>(checkedId)
            if (selectedRadiobtn?.text == "Admin") {
                binding.createAdminLayout.visibility = View.VISIBLE
                binding.createAdminbtn.setOnClickListener {
                    startActivity(Intent(this,AddAdminActivity::class.java))
                }
            } else {
                binding.createAdminLayout.visibility = View.GONE
            }
        }
    }


    fun addToken(collection: String, email: String, token: String) {
        val db = Firebase.firestore
        val document = db.collection(collection).document(email)

        document.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val existingTokens = snapshot.get("fcm") as? List<String>

                    if (existingTokens == null) {
                        // No existing tokens, create a new list with the current token
                        val newTokens = listOf(token)
                        document.update("fcm", newTokens)
                            .addOnSuccessListener {
                                // Token added successfully
                            }
                            .addOnFailureListener { exception ->
                                // Error occurred while adding the token
                            }
                    } else {
                        if (!existingTokens.contains(token)) {
                            // Token doesn't exist in the list, replace the existing tokens with the current token
                            val newTokens = listOf(token)
                            document.update("fcm", newTokens)
                                .addOnSuccessListener {
                                    // Token added successfully
                                }
                                .addOnFailureListener { exception ->
                                    // Error occurred while adding the token
                                }
                        } else {
                            // Token already exists in the list
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Error occurred while retrieving the document
            }
    }




    private fun showRecoverPasswordDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Recover Password")
        val linearLayout = LinearLayout(this)
        val emailet = EditText(this)

        // write the email using which you registered
        emailet.hint = "Recovery Email Address"
        emailet.minEms = 16
        emailet.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        linearLayout.addView(emailet)
        linearLayout.setPadding(10, 10, 10, 10)
        builder.setView(linearLayout)

        // Click on Recover and a email will be sent to your registered email id
        builder.setPositiveButton("Recover"
        ) { _, _ ->
            val email = emailet.text.toString().trim { it <= ' ' }
            beginRecovery(email)
        }
        builder.setNegativeButton("Cancel"
        ) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    var loadingBar: ProgressDialog? = null
    private fun beginRecovery(email: String) {
        loadingBar = ProgressDialog(this)
        loadingBar!!.setMessage("Sending Email....")
        loadingBar!!.setCanceledOnTouchOutside(false)
        loadingBar!!.show()

        val mAuth = FirebaseAuth.getInstance()
        // calling sendPasswordResetEmail
        // open your email and write the new
        // password and then you can login
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                loadingBar!!.dismiss()
                if (task.isSuccessful) {
                    // if isSuccessful then done message will be shown
                    // and you can change the password
                    Toast.makeText(this@LogInActivity, "Done sent", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@LogInActivity, "Error Occurred", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener {
                loadingBar!!.dismiss()
                Toast.makeText(this@LogInActivity, "Error Failed", Toast.LENGTH_LONG).show()
            }
    }

    fun clientVerification(token: String,email: String){
        //  sharedPreferences = getSharedPreferences("CLIENT_PREF", MODE_PRIVATE)
        val currentuid = FirebaseAuth.getInstance().currentUser?.uid
        val firestore = FirebaseFirestore.getInstance()
        val clientcollection = firestore.collection("client")




        clientcollection.whereEqualTo("uid",currentuid)
            .get()
            .addOnCompleteListener {it ->
                if(it.isSuccessful){
                    val querySnapshot = it.result
                    if (querySnapshot!=null && !querySnapshot.isEmpty) {
                        //         Toast.makeText(this,"Logging in successful",Toast.LENGTH_SHORT).show()

//                    getAdminFCM("INVYU1")
                        getClientEmail(token,email)

                    }else {
                        Log.d("ClientError",querySnapshot.toString())
                        dismissprogressbar()
                        FirebaseAuth.getInstance().signOut()
                        Toast.makeText(this, "Client login failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Error occurred while querying admin section
                    dismissprogressbar()
                    //  Log.d("adminerror",)
                    val exception = it.exception
                    FirebaseAuth.getInstance().signOut()
                    Log.d("clienterror",exception.toString())
                    Toast.makeText(this, "Error querying client section", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                dismissprogressbar()
            }

    }


    private fun textwatcher(){
        binding.loginPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not used
            }

            override fun afterTextChanged(s: Editable?) {
                // Check the input length and show error message if less than 6 characters
                if (s?.length ?: 0 < 6) {
                    binding.textInputLayout.error = "Password must be at least 6 characters"
                } else {
                    binding.textInputLayout.error = null
                }
            }
        })

        binding.loginPassword.addTextChangedListener()
    }

    fun adminverification(){
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val firestore = FirebaseFirestore.getInstance()
        val adminCollection = firestore.collection("admin")

        adminCollection.whereEqualTo("uid", currentUserUid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    if (querySnapshot != null && !querySnapshot.isEmpty) {
                        // Admin login success
                        val documentSnapshot = querySnapshot.documents[0] // Assuming there's only one document returned
                        Log.d("Admin info",documentSnapshot.toString())
//                        val companyId = documentSnapshot.getString("company_id")
//                        //val fcm = documentSnapshot.getString("fcm")
//
//                        if (fcm != null && companyId != null) {
//                            Log.d("fcm",fcm)
//                            Log.d("company_id",companyId)
//                        }
//                        val editor = sharedPreferences.edit()
//                        editor.putString("fcm",fcm)
//                        editor.putString("company_id",companyId)
//                        editor.apply()

                        getAdminEmail(FirebaseAuth.getInstance().currentUser!!.email.toString())


                    } else {
                        // Admin login failed
                        dismissprogressbar()
                        //   FirebaseAuth.getInstance().signOut()
                        Toast.makeText(this, "Admin login failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Error occurred while querying admin section
                    dismissprogressbar()
                    val exception = task.exception
                    Log.d("Adminerror", exception.toString())
                    //   FirebaseAuth.getInstance().signOut()
                    Toast.makeText(this, "Error querying admin section", Toast.LENGTH_SHORT).show()
                }
            }

    }


    fun createClientUser(token: String,email : String,password : String){
        sharedPreferences = getSharedPreferences("CLIENT_DETAILS", MODE_PRIVATE)

        val apiservice = RetrofitClientEmailInfo.apiService
        val call = apiservice.getData(email)

        call.enqueue(object : Callback<ClientemailInfo> {
            override fun onResponse(call: Call<ClientemailInfo>, response: Response<ClientemailInfo>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    if (responseData != null) {
                        val emailExists = responseData.email_exists
                        val companyId = responseData.company_id
                        val companyName = responseData.company_name
                        val employeeId = responseData.employee_id
                        val name = responseData.first_name
                        val work_start_time = responseData.work_start_time
                        val work_end_time = responseData.work_end_time

                        //  getAdminFCM(companyId.toString())
                        val editor = sharedPreferences.edit()
                        // editor.putString("fcm",fcm)
                        editor.putString("company_id", companyId)
                        editor.putString("company_name", companyName)
                        editor.putString("employee_id", employeeId)
                        editor.putString("work_start_time", work_start_time)
                        editor.putString("work_end_time", work_end_time)
                        editor.putString("name", name)
                        editor.apply()




                        //  val database = FirebaseDatabase.getInstance()
                        //  val path = database.getReference(companyId.toString())
                        //  val dbref = path.child(employeeId.toString())

                        val sh = getSharedPreferences("log", Context.MODE_PRIVATE)
                        val ed = sh.edit()
                        ed.putString("log", "client")
                        ed.apply()


                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this@LogInActivity) { creationTask ->
                                if (creationTask.isSuccessful) {
                                    val uid = creationTask.result?.user?.uid


                                    val database = FirebaseDatabase.getInstance()
                                    val path = database.getReference(companyId.toString())
                                    val dbref = path.child(employeeId.toString())

                                    val sh = getSharedPreferences("log",Context.MODE_PRIVATE)
                                    val ed = sh.edit()
                                    ed.putString("log","client")
                                    ed.apply()


// Check if the token already exists in the database
                                    val query = dbref.orderByValue().equalTo(token)
                                    query.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            if (dataSnapshot.childrenCount > 0) {
                                                // Token already exists, do not store it again
                                                return
                                            } else {
                                                // Token is unique, store it in the database under the employee ID
                                                dbref.child("fcm").setValue(token)
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {
                                            // Handle any errors
                                        }
                                    })

                                    val db = FirebaseFirestore.getInstance()
                                    val adminCollection = db.collection("client")
                                    val employeeDoc = adminCollection.document(
                                        email
                                    )
                                    employeeDoc.set(mapOf(
                                        "uid" to uid)
                                    ).addOnCompleteListener {
                                        dismissprogressbar()
                                        startActivity(Intent(this@LogInActivity, ClientActivity::class.java))
                                        Toast.makeText(this@LogInActivity, "Logging in successful", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                        .addOnFailureListener {exception ->
                                            dismissprogressbar()
                                            Toast.makeText(this@LogInActivity, "${exception.message}", Toast.LENGTH_SHORT).show()
                                        }



                                } else {


                                    dismissprogressbar()
                                    Toast.makeText(this@LogInActivity, "Client user creation failed", Toast.LENGTH_SHORT).show()
                                }
                            }

                    }
                }
                else{
                    dismissprogressbar()
                    Toast.makeText(this@LogInActivity, "No Employee found/ Internal Server error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ClientemailInfo>, t: Throwable) {
                // Handle failure
                dismissprogressbar()
                Log.d("ErrorClient",t.toString())
                Toast.makeText(this@LogInActivity, "Something went wrong!!", Toast.LENGTH_SHORT).show()
            }
        })



    }

    fun getClientEmail(token: String,email : String){

        sharedPreferences = getSharedPreferences("CLIENT_DETAILS", MODE_PRIVATE)

        val apiservice = RetrofitClientEmailInfo.apiService
        val call = apiservice.getData(email)

        call.enqueue(object : Callback<ClientemailInfo> {
            override fun onResponse(call: Call<ClientemailInfo>, response: Response<ClientemailInfo>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    if (responseData != null) {
                        val emailExists = responseData.email_exists
                        val companyId = responseData.company_id
                        val companyName = responseData.company_name
                        val employeeId = responseData.employee_id
                        val name = responseData.first_name
                        val work_start_time = responseData.work_start_time
                        val work_end_time = responseData.work_end_time

                        //  getAdminFCM(companyId.toString())
                        val editor = sharedPreferences.edit()
                        // editor.putString("fcm",fcm)
                        editor.putString("company_id",companyId)
                        editor.putString("company_name",companyName)
                        editor.putString("employee_id",employeeId)
                        editor.putString("work_start_time",work_start_time)
                        editor.putString("work_end_time",work_end_time)
                        editor.putString("name",name)
                        editor.apply()

                        val database = FirebaseDatabase.getInstance()
                        val path = database.getReference(companyId.toString())
                        val dbref = path.child(employeeId.toString())

                        val sh = getSharedPreferences("log",Context.MODE_PRIVATE)
                        val ed = sh.edit()
                        ed.putString("log","client")
                        ed.apply()


// Check if the token already exists in the database
                        val query = dbref.orderByValue().equalTo(token)
                        query.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.childrenCount > 0) {
                                    // Token already exists, do not store it again
                                    return
                                } else {
                                    // Token is unique, store it in the database under the employee ID
                                    dbref.child("fcm").setValue(token)
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle any errors
                                Log.d("DbError",databaseError.toString())
                                dismissprogressbar()
                            }
                        })
                        dismissprogressbar()
                        startActivity(Intent(this@LogInActivity, ClientActivity::class.java))
                        Toast.makeText(this@LogInActivity, "Logging in successful", Toast.LENGTH_SHORT).show()
                        finish()




                        // Use the retrieved data as needed
                    } else {
                        //    FirebaseAuth.getInstance().signOut()
                        dismissprogressbar()
                        Toast.makeText(this@LogInActivity, "Logging unsuccessful", Toast.LENGTH_SHORT).show()
                        // Handle null response body
                    }
                } else {
                    // Handle unsuccessful response
                    //   FirebaseAuth.getInstance().signOut()
                    dismissprogressbar()
                    Toast.makeText(this@LogInActivity, "No Employee found/ Internal Server error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ClientemailInfo>, t: Throwable) {
                // Handle failure
                dismissprogressbar()
                Log.d("ErrorClient",t.toString())
                Toast.makeText(this@LogInActivity, "Something went wrong!!", Toast.LENGTH_SHORT).show()
            }
        })


    }


    fun getNotificationPermission() {
        try {
            if (Build.VERSION.SDK_INT > 32) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            }
        } catch (e: Exception) {
        }
    }



    fun getAdminEmail(email: String){

        sharedPreferences = getSharedPreferences("ADMIN_PREF", MODE_PRIVATE)

        val apiservice = Retrofitgetadminemail.apiService
        val call = apiservice.getData(email)

        call.enqueue(object : Callback<AdminEmail> {
            override fun onResponse(call: Call<AdminEmail>, response: Response<AdminEmail>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    if (responseData != null) {
                        val emailExists = responseData.email_exists
                        val companyId = responseData.company_id
                        val companyName = responseData.company_name
                        dismissprogressbar()


                        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->

                            addToken("admin",email,token)
                        }



                        val editor = sharedPreferences.edit()
                        // editor.putString("fcm",fcm)
                        editor.putString("company_id",companyId)
                        editor.putString("company_name",companyName)
                        editor.apply()
                        val sh = getSharedPreferences("log",Context.MODE_PRIVATE)
                        val ed = sh.edit()
                        ed.putString("log","admin")
                        ed.apply()
                        val intent = Intent(this@LogInActivity, AdminActivity::class.java)
//                        intent.putExtra("fcm",fcm)
//                        intent.putExtra("company_id",companyId)
                        startActivity(intent)
                        Toast.makeText(this@LogInActivity, "Logging in successful", Toast.LENGTH_SHORT).show()
                        finish()

                        // Use the retrieved data as needed
                    } else {
                        dismissprogressbar()
                        Log.d("AdminInfo","err1")
                        // FirebaseAuth.getInstance().signOut()
                        // Handle null response body
                    }
                } else {dismissprogressbar()
                    Log.d("AdminInfo","err2")
                    //  FirebaseAuth.getInstance().signOut()
                    // Handle unsuccessful response
                }
            }

            override fun onFailure(call: Call<AdminEmail>, t: Throwable) {dismissprogressbar()
                Log.d("AdminInfo","err3")
                // Handle failure
                //   FirebaseAuth.getInstance().signOut()
            }
        })


    }

    fun UserLoggedIn(){

        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        showprogressbar()
        //    val  = "your_user_id"

        val adminCollection = FirebaseFirestore.getInstance().collection("admin")
        val clientCollection = FirebaseFirestore.getInstance().collection("client")

        // Create two queries to check if the user ID exists in the admin and client collections
        val adminQuery = adminCollection.whereEqualTo("uid", userId)
        val clientQuery = clientCollection.whereEqualTo("uid", userId)

        // Perform the queries asynchronously
        Tasks.whenAllSuccess<QuerySnapshot>(adminQuery.get(), clientQuery.get())
            .addOnSuccessListener { results ->
                val adminQuerySnapshot = results[0] as QuerySnapshot
                val clientQuerySnapshot = results[1] as QuerySnapshot

                if (!adminQuerySnapshot.isEmpty) {
                    // User ID exists in the admin collection
                    getAdminEmail(FirebaseAuth.getInstance().currentUser!!.email.toString())
//                    startActivity(Intent(this@LogInActivity,AdminActivity::class.java))
//                    finish()

                } else if (!clientQuerySnapshot.isEmpty) {
                    // User ID exists in the client collection
                    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                        // Store the user's device token in Firebase Realtime Database
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            //Toast.makeText(this,"Device token $token",Toast.LENGTH_SHORT).show()
                            Log.d("Devicetoken",token)
                            getClientEmail(token,FirebaseAuth.getInstance().currentUser!!.email.toString())
                        }

                    }
                    dismissprogressbar()


//                    startActivity(Intent(this@LogInActivity,ClientActivity::class.java))
//                    finish()

                } else {
                    FirebaseAuth.getInstance().signOut()
                    dismissprogressbar()
                    Toast.makeText(this,"User doesn't exits",Toast.LENGTH_SHORT).show()

                }
            }
            .addOnFailureListener { exception ->
                Log.d("ClientErrort",exception.toString())
                dismissprogressbar()
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this,"Something went wrong!!",Toast.LENGTH_SHORT).show()

            }



    }










    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    // allow
                } else {
                    //deny
                }
                return
            }

        }
    }



    private fun showprogressbar(){
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Verifying...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()

    }
    private fun dismissprogressbar(){
        progressDialog?.dismiss()
    }
}