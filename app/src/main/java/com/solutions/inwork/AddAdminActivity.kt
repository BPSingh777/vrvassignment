package com.solutions.inwork

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.solutions.inwork.databinding.ActivityAddAdminBinding
import com.solutions.inwork.retrofitPost.RetrofitAddAdmin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddAdminActivity : AppCompatActivity() {

    private val MAP_REQUEST_CODE = 123
    private lateinit var binding:ActivityAddAdminBinding
    private var progressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.apply {

            addAdminbtn.setOnClickListener {
                val emptyFields = ArrayList<String>()

                if (companyIdEditText.text.isNullOrEmpty()) {
                    emptyFields.add("Company ID")
                }

                if (companyNameEditText.text.isNullOrEmpty()) {
                    emptyFields.add("Company Name")
                }

                if (industryEditText.text.isNullOrEmpty()) {
                    emptyFields.add("Industry")
                }

                if (managingdirectorEditText.text.isNullOrEmpty()) {
                    emptyFields.add("Managing Director")
                }

                if (mobileEditText.text.isNullOrEmpty()) {
                    emptyFields.add("Mobile")
                }

                if (emailEditText.text.isNullOrEmpty()) {
                    emptyFields.add("Email")
                }

                if (addressEditText.text.isNullOrEmpty()) {
                    emptyFields.add("Address")
                }

                if (locationLatEditText.text.isNullOrEmpty()) {
                    emptyFields.add("Location Latitude")
                }

                if (locationLongEditText.text.isNullOrEmpty()) {
                    emptyFields.add("Location Longitude")
                }

                if (radiusEditText.text.isNullOrEmpty()) {
                    emptyFields.add("Radius")
                }

                if (emptyFields.isNotEmpty()) {
                    val errorMessage = "Please fill the following fields:\n" + emptyFields.joinToString(", ")
                    Toast.makeText(this@AddAdminActivity, errorMessage, Toast.LENGTH_LONG).show()
                } else {
                    showprogressbar()
                    PostAdminDetails()
                }
            }

            binding.btndatepicker.setOnClickListener {
                if (requestBackgroundPermission()){
                   buttonDetectGPS()
                }else{
                    requestBackgroundPermission()
                }

            }
        }



    }


    fun buttonDetectGPS() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            openMapActivity()
        } else {

            // GPS is disabled, show the pop-up to enable GPS
            AlertDialog.Builder(this)
                .setTitle("Enable Location")
                .setMessage("Please enable Location to use this feature.")
                .setPositiveButton("OK") { dialog, which ->
                    // Turn on GPS
                    val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(settingsIntent)

                }
                .setNegativeButton("Cancel") { dialog, which ->
                    // User clicked Cancel, handle it as needed
                    // binding.permission.text = "GPS is OFF"
                }
                .show()

        }
    }

    private fun requestBackgroundPermission() : Boolean{
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this,"not granted",Toast.LENGTH_SHORT).show()

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                123
            )
            return false
        } else {
            //permission is already granted
     //       Toast.makeText(requireContext(),"granted",Toast.LENGTH_SHORT).show()
            return true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val updatedLatitude = data?.getStringExtra("locationLat")?.toDoubleOrNull()
            val updatedLongitude = data?.getStringExtra("locationLong")?.toDoubleOrNull()
            val companyId = data?.getStringExtra("companyId")
            val companyName = data?.getStringExtra("companyName")
            val industry = data?.getStringExtra("industry")
            val managingDirector = data?.getStringExtra("managingDirector")
            val mobile = data?.getStringExtra("mobile")
            val email = data?.getStringExtra("email")
            val address = data?.getStringExtra("address")
            val radius = data?.getStringExtra("radius")

            binding.apply {
                // Update the latitude, longitude, and other relevant fields
                updatedLatitude?.let { locationLatEditText.setText(it.toString()) }
                updatedLongitude?.let { locationLongEditText.setText(it.toString()) }
                companyIdEditText.setText(companyId)
                companyNameEditText.setText(companyName)
                industryEditText.setText(industry)
                managingdirectorEditText.setText(managingDirector)
                mobileEditText.setText(mobile)
                emailEditText.setText(email)
                addressEditText.setText(address)
                radiusEditText.setText(radius)
            }

        }
    }


    private fun openMapActivity() {

        binding.apply {

            val companyId = companyIdEditText.text.toString()
            val companyName = companyNameEditText.text.toString()
            val industry = industryEditText.text.toString()
            val managingDirector = managingdirectorEditText.text.toString()
            val mobile = mobileEditText.text.toString()
            val email = emailEditText.text.toString()
            val address = addressEditText.text.toString()
            val locationLat = locationLatEditText.text.toString()
            val locationLong = locationLongEditText.text.toString()
            val radius = radiusEditText.text.toString()

            val intent = Intent(this@AddAdminActivity, MapActivity::class.java)
            intent.putExtra("companyId", companyId)
            intent.putExtra("companyName", companyName)
            intent.putExtra("industry", industry)
            intent.putExtra("managingDirector", managingDirector)
            intent.putExtra("mobile", mobile)
            intent.putExtra("email", email)
            intent.putExtra("address", address)
            intent.putExtra("locationLat", locationLat)
            intent.putExtra("locationLong", locationLong)
            intent.putExtra("radius", radius)
            startActivityForResult(intent, MAP_REQUEST_CODE)
        }
    }




    private fun PostAdminDetails(){

        binding.apply {
            val jsonObject = JSONObject().apply {
                put("company_id", companyIdEditText.text.toString().trim())
                put("company_name", companyNameEditText.text.toString().trim())
                put("industry", industryEditText.text.toString().trim())
                put("managing_director", managingdirectorEditText.text.toString().trim())
                put("mobile", mobileEditText.text.toString().trim())
                put("email", emailEditText.text.toString().trim())
                put("address", addressEditText.text.toString().trim())
                put("location_lat", locationLatEditText.text.toString().trim())
                put("location_long", locationLongEditText.text.toString().trim())
                put("radius", radiusEditText.text.toString().trim())
            }

            val requestBody =
                RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())

            val apiservice = RetrofitAddAdmin.apiService
            val call = apiservice.postData(requestBody)

            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful){

                        AddAdmin()


                       Toast.makeText(this@AddAdminActivity,"Admin created successfully",Toast.LENGTH_SHORT).show()
                       
                    }
                    else{
                        dismissprogressbar()
                        Toast.makeText(this@AddAdminActivity,"Something went wrong",Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    dismissprogressbar()
                    Toast.makeText(this@AddAdminActivity,"Error ${t.message}",Toast.LENGTH_SHORT).show()
                }


            })
        }
    }


    fun AddAdmin(){

        binding.apply {


            val auth = FirebaseAuth.getInstance()

            auth.createUserWithEmailAndPassword(
                emailEditText.text.toString().trim(),
                mobileEditText.text.toString().trim()
            )
                .addOnCompleteListener(this@AddAdminActivity) { authResult ->
                    val uid = authResult.result?.user?.uid

                    // Store the UID in Firestore
                    val db = FirebaseFirestore.getInstance()
                    val adminCollection = db.collection("admin")
                    val employeeDoc = adminCollection.document(emailEditText.text.toString().trim())
                    employeeDoc.set(
                        mapOf(
                            "uid" to uid,
                            "company_id" to companyIdEditText.text.toString().trim()
                        )
                    )
                        .addOnSuccessListener {
                            Toast.makeText(
                                this@AddAdminActivity,
                                "Admin added successfully in firebase",
                                Toast.LENGTH_SHORT
                            ).show()
                            dismissprogressbar()
                            companyIdEditText.text!!.clear()
                            companyNameEditText.text!!.clear()
                            industryEditText.text!!.clear()
                            managingdirectorEditText.text!!.clear()
                            mobileEditText.text!!.clear()
                            emailEditText.text!!.clear()
                            addressEditText.text!!.clear()
                            locationLatEditText.text!!.clear()
                            locationLongEditText.text!!.clear()
                            radiusEditText.text!!.clear()
                            startActivity(Intent(this@AddAdminActivity,LogInActivity::class.java))
                            finish()

                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                this@AddAdminActivity,
                                " error1 ${exception.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            dismissprogressbar()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this@AddAdminActivity, "error2 ${exception.message}", Toast.LENGTH_LONG)
                        .show()
                    dismissprogressbar()
                }

        }

    }
    private fun showprogressbar(){
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Adding Admin...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }
    private fun dismissprogressbar(){
        progressDialog?.dismiss()
    }
}