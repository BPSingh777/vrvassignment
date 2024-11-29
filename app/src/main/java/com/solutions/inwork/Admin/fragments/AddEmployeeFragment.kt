package com.solutions.inwork.Admin.fragments

import android.R
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.TypedArray
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.TintTypedArray.obtainStyledAttributes
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.solutions.inwork.Admin.dataclasses.ProfileModel
import com.solutions.inwork.databinding.FragmentAddEmployeeBinding
import com.solutions.inwork.retrofitPost.RetrofitAddEmployee
import com.solutions.inwork.retrofitget.RetrofitAdminProfile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar


class AddEmployeeFragment : Fragment() {

    private  lateinit var  binding : FragmentAddEmployeeBinding
    private lateinit var time : String
    private lateinit var date : String
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentAddEmployeeBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment



        binding.apply {

            Timepicker(workStartTimepicker,workStartTimeEditText)
            Timepicker(workEndTimepicker,workEndTimeEditText)
            datePicker(workeffectdatepicker,workEffectFromDateEditText)
            datePicker(workenddatepicker,workEndFromDateEditText)
            datePicker(btndatepicker,dateOfBirthEditText)





            val adminUser = FirebaseAuth.getInstance().currentUser
            Log.d("Pass","$adminUser")
            fetchProfile()

            addEmpbtn.setOnClickListener {
                if (employeeIdEditText.text.isNullOrEmpty() || firstNameEditText.text.isNullOrEmpty() ||lastNameEditText.text.isNullOrEmpty() ||
                    dateOfBirthEditText.text.isNullOrEmpty() || designationEditText.text.isNullOrEmpty()|| addressEditText.text.isNullOrEmpty() || adhaarNumberEditText.text.isNullOrEmpty() ||
                    mobileEditText.text.isNullOrEmpty() || emailEditText.text.isNullOrEmpty() || workStartTimeEditText.text.isNullOrEmpty() || workEndTimeEditText.text.isNullOrEmpty() ||
                    workEffectFromDateEditText.text.isNullOrEmpty()|| officeEditText.text.isNullOrEmpty() || workEndFromDateEditText.text.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in all the fields", Toast.LENGTH_SHORT).show()

                }else{
                    showprogressbar()
                    AddEmployee()
                }
            }

        }

        return binding.root
    }

    private var latitude : String = ""
    private var longitude : String = ""
    private var radius : String = ""


    private fun fetchProfile() {
        val apiService = RetrofitAdminProfile.apiService

        val sharedPreferences = requireContext().getSharedPreferences("ADMIN_PREF", Context.MODE_PRIVATE)
        val companyid = sharedPreferences.getString("company_id", null) ?: return
        val useremail = FirebaseAuth.getInstance().currentUser!!.email

        val call: Call<Map<String, ProfileModel>> = apiService.getData(companyid)
        call.enqueue(object : Callback<Map<String, ProfileModel>> {
            override fun onResponse(
                call: Call<Map<String, ProfileModel>>,
                response: Response<Map<String, ProfileModel>>,
            ) {
                if (response.isSuccessful) {
                    val data: Map<String, ProfileModel>? = response.body()
                    val searchEmail = useremail

                    //  Toast.makeText(requireContext(),"found",Toast.LENGTH_SHORT).show()
                    // Search for the specific email within the response data
                    val foundEntry = data?.values?.find {
                        it.email == searchEmail
                    }

                    if (foundEntry != null) {
                        // Found the email
                       // Toast.makeText(requireContext(),"found",Toast.LENGTH_SHORT).show()
                  //      dismissprogressbar()

                        longitude = foundEntry.location_long.toString()
                        latitude = foundEntry.location_lat.toString()
                        radius = foundEntry.radius.toString()
//                        binding.EmpNameTextView.text = foundEntry.managing_director
//                        binding.empCompanyName.text = foundEntry.company_name
//                        binding.empIndustry.text = foundEntry.industry
//                        binding.empCompanyId.text = foundEntry.company_id
//                        binding.empEmail.text = foundEntry.email
//                        binding.empPhone.text = foundEntry.mobile.toString()

//                        binding.profileDescriptionTextView.text = "Managing Director"
                    } else {
                        dismissprogressbar()
                        Toast.makeText(requireContext(),"Profile Not found",Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle error response
                    dismissprogressbar()
                    Toast.makeText(requireContext(),response.toString(),Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, ProfileModel>>, t: Throwable) {
                // Handle failure
                dismissprogressbar()
                Toast.makeText(requireContext(), "API request failed: ${t.message}", Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }

        })




    }

    private fun AddEmployee(){

        val sharedPreferences = requireContext().getSharedPreferences("ADMIN_PREF", Context.MODE_PRIVATE)
        val companyid = sharedPreferences.getString("company_id", null) ?: return
        val companyName = sharedPreferences.getString("company_name",null) ?: return
        val auth = FirebaseAuth.getInstance()

        Log.d("lat",latitude)
        Log.d("lng",longitude)
        Log.d("rad",radius)
        binding.apply {
            val jsonObject = JSONObject().apply {
                put("company_id", companyid)
                put("employee_id", employeeIdEditText.text.toString().trim())
                put("company_name",companyName)
                put("first_name", firstNameEditText.text.toString().trim())
                put("last_name", lastNameEditText.text.toString().trim())
                put("date_of_birth", dateOfBirthEditText.text.toString().trim())
                put("designation", designationEditText.text.toString().trim())
                put("address", addressEditText.text.toString().trim())
                put("adhaar_number", adhaarNumberEditText.text.toString().trim())
                put("mobile", mobileEditText.text.toString().trim())
                put("email", emailEditText.text.toString().trim())
                put("office", officeEditText.text.toString().trim())
                put("location_lat",latitude)
                put("location_long",longitude)
                put("radius",radius)
                put("work_start_time", workStartTimeEditText.text.toString().trim())
                put("work_end_time", workEndTimeEditText.text.toString().trim())
                put("work_effect_from_date", workEffectFromDateEditText.text.toString().trim())
                put("work_end_from_date", workEndFromDateEditText.text.toString().trim())
            }

            val requestBody =
                RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())

            val apiservice = RetrofitAddEmployee.apiService
            val call = apiservice.postData(requestBody)

            call.enqueue(object : Callback<Void> {
                @SuppressLint("SuspiciousIndentation")
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {

                        val sharedPreferences = requireContext().getSharedPreferences("AdminPass",Context.MODE_PRIVATE)
                        val password = sharedPreferences.getString("Pass",null)
                        val email = sharedPreferences.getString("AdminEmail",null)
                        Log.d("Password", "${email.toString()}  ${ password.toString() }")

                        val adminUser = FirebaseAuth.getInstance().currentUser
                        val credential = EmailAuthProvider.getCredential(email.toString(),password.toString())


//
//
//                                auth.createUserWithEmailAndPassword(
//                                    emailEditText.text.toString().trim(),
//                                    mobileEditText.text.toString().trim()
//                                )
//                                    .addOnCompleteListener(requireActivity()) { authResult ->
//                                        val uid = authResult.result?.user?.uid
//
//                                        // Store the UID in Firestore
//                                        val db = FirebaseFirestore.getInstance()
//                                        val adminCollection = db.collection("client")
//                                        val employeeDoc = adminCollection.document(
//                                            emailEditText.text.toString().trim()
//                                        )
//                                        employeeDoc.set(
//                                            mapOf(
//                                                "uid" to uid,
//                                                //         "employee_id" to employeeIdEditText.text.toString().trim()
//                                            )
//                                        )
//                                            .addOnSuccessListener {
//                                                Toast.makeText(
//                                                    requireContext(),
//                                                    "Employee added successfully",
//                                                    Toast.LENGTH_SHORT
//                                                ).show()
//
//
//
//                                                adminUser!!.reauthenticate(credential)
//                                                    .addOnSuccessListener {
//
//                                                dismissprogressbar()
//                                                // Clear the EditText fields
//                                                employeeIdEditText.text!!.clear()
//                                                firstNameEditText.text!!.clear()
//                                                lastNameEditText.text!!.clear()
//                                                dateOfBirthEditText.text!!.clear()
//                                                designationEditText.text!!.clear()
//                                                addressEditText.text!!.clear()
//                                                adhaarNumberEditText.text!!.clear()
//                                                mobileEditText.text!!.clear()
//                                                emailEditText.text!!.clear()
//                                                workStartTimeEditText.text!!.clear()
//                                                workEndTimeEditText.text!!.clear()
//                                                workEffectFromDateEditText.text!!.clear()
//                                                officeEditText.text!!.clear()
//                                                workEndFromDateEditText.text!!.clear()
//                                            }
//                                            .addOnFailureListener { exception ->
//                                                dismissprogressbar()
//                                                Toast.makeText(
//                                                    requireContext(),
//                                                    "${exception.message}",
//                                                    Toast.LENGTH_SHORT
//                                                ).show()
//                                            }
//                                    }
//                                    .addOnFailureListener { exception ->
//                                        dismissprogressbar()
//                                        Toast.makeText(
//                                            requireContext(),
//                                            "${exception.message}",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
//
//                            }
//                            .addOnFailureListener {
//                                dismissprogressbar()
//                            }

                        // Clear EditText fields
                     //   companyIdEditText.text!!.clear()



                                                dismissprogressbar()
                                                // Clear the EditText fields
                                                employeeIdEditText.text!!.clear()
                                                firstNameEditText.text!!.clear()
                                                lastNameEditText.text!!.clear()
                                                dateOfBirthEditText.text!!.clear()
                                                designationEditText.text!!.clear()
                                                addressEditText.text!!.clear()
                                                adhaarNumberEditText.text!!.clear()
                                                mobileEditText.text!!.clear()
                                                emailEditText.text!!.clear()
                                                workStartTimeEditText.text!!.clear()
                                                workEndTimeEditText.text!!.clear()
                                                workEffectFromDateEditText.text!!.clear()
                                                officeEditText.text!!.clear()
                                                workEndFromDateEditText.text!!.clear()
                        Toast.makeText(requireContext(), "Employee added successfully", Toast.LENGTH_SHORT).show()

                    } else {
                        dismissprogressbar()
                        Log.d("AddEmployee", response.message())
                        Toast.makeText(
                            requireContext(),
                            "Something went wrong!!",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.d("AddEmployee", t.toString())
                    dismissprogressbar()
                    Toast.makeText(requireContext(), "Something went wrong!!", Toast.LENGTH_SHORT)
                        .show()
                }

            })
        }


    }

    private fun showprogressbar(){
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("Adding employee...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()

    }
    private fun dismissprogressbar(){
        progressDialog?.dismiss()
    }

    private fun Timepicker(ViewId: FloatingActionButton , EditTextId: EditText){

        ViewId.setOnClickListener {

            val currentTime = Calendar.getInstance()
            val hour = currentTime.get(Calendar.HOUR_OF_DAY)
            val minute = currentTime.get(Calendar.MINUTE)
            val timePicker = TimePickerDialog(
                context,
                { _, selectedHour, selectedMinute ->
                    time = String.format("%02d:%02d", selectedHour, selectedMinute)
                    EditTextId.setText(time)

                },
                hour,
                minute,
                true
            )
            timePicker.show()
        }

    }
    private fun datePicker(ViewId: FloatingActionButton ,EditTextId: EditText) {

        ViewId.setOnClickListener {
            val currentDate = Calendar.getInstance()
            val year = currentDate.get(Calendar.YEAR)
            val month = currentDate.get(Calendar.MONTH)
            val dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)

            val datePicker = activity?.let { it1 ->
                DatePickerDialog(
                    it1,
                    { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                        // Date selection logic
                        date = String.format(
                            "%04d-%02d-%02d",
                            selectedYear,
                            selectedMonth + 1,
                            selectedDayOfMonth
                        )

                        EditTextId.setText(date)
                    },
                    year,
                    month,
                    dayOfMonth
                )
            }

            datePicker?.show()
        }

    }

}