package com.solutions.inwork.client.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.solutions.inwork.Admin.dataclasses.EmployeeDetails
import com.solutions.inwork.LogInActivity
import com.solutions.inwork.R
import com.solutions.inwork.databinding.FragmentClientProfileBinding
import com.solutions.inwork.retrofitget.RetrofitAllEmp
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.solutions.inwork.GpsNotificationService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ClientProfileFragment : Fragment() {

    private lateinit var binding: FragmentClientProfileBinding
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentClientProfileBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment

        updatepass()

        binding.profileLgtBtn.setOnClickListener {
            val sharedPreferences = requireContext().getSharedPreferences("ScreenTime_Details",Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("ScreenDetails",true).apply()
            val serviceIntent = Intent(requireContext(), GpsNotificationService::class.java)
            requireContext().stopService(serviceIntent)
             getAdminFCM()
        }


       showprogressbar()
       fetchprofile()

        return binding.root
    }


    fun getAdminFCM() {

        val sharedPreferences = requireContext().getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
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

                                fcmList.addAll(fcm)
                            }
                        }
                        if (fcmList.isNotEmpty()) {
                            deleteAdmintoken(fcmList)
                            // GpsNotification(title,"$employeeName's $message",fcmList)
                        }
                        else{
                            FirebaseAuth.getInstance().signOut()
                            val sharedPreferences = requireContext().getSharedPreferences(
                                "CLIENT_PREF",
                                AppCompatActivity.MODE_PRIVATE
                            )
                            val sharedPreferences1 = requireContext().getSharedPreferences(
                                "CLIENT_DETAILS",
                                AppCompatActivity.MODE_PRIVATE
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
                                    requireContext(),
                                    LogInActivity::class.java
                                )
                            )
                            Toast.makeText(
                                requireContext(),
                                "Logout",
                                Toast.LENGTH_SHORT
                            ).show()
                            activity?.finish()
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
        val sharedPreferences = requireContext().getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val employeeId  = sharedPreferences.getString("employee_id",null)
        val companyId = sharedPreferences.getString("company_id",null)
        //   val employeeId = "employee_user_id" // Replace with the actual employee's unique identifier
        val fcmToken = FirebaseMessaging.getInstance().token

        FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
            // Store the user's device token in Firebase Realtime Database
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            for (adminFcmtoken in adminFcmList) {

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

                                                FirebaseAuth.getInstance().signOut()
                                                val sharedPreferences = requireContext().getSharedPreferences(
                                                    "CLIENT_PREF",
                                                    AppCompatActivity.MODE_PRIVATE
                                                )
                                                val sharedPreferences1 = requireContext().getSharedPreferences(
                                                    "CLIENT_DETAILS",
                                                    AppCompatActivity.MODE_PRIVATE
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
                                                        requireContext(),
                                                        LogInActivity::class.java
                                                    )
                                                )
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Logout",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                activity?.finish()
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
            val sharedPreferences = requireContext().getSharedPreferences(
                "CLIENT_PREF",
                AppCompatActivity.MODE_PRIVATE
            )
            val sharedPreferences1 = requireContext().getSharedPreferences(
                "CLIENT_DETAILS",
                AppCompatActivity.MODE_PRIVATE
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
                    requireContext(),
                    LogInActivity::class.java
                )
            )
            Toast.makeText(
                requireContext(),
                "Logout",
                Toast.LENGTH_SHORT
            ).show()
            activity?.finish()
        }


    }


    @SuppressLint("MissingInflatedId")
    fun updatepass(){


        val auth = FirebaseAuth.getInstance().currentUser

        binding.updatePassBtn.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(activity)
            val layoutInflater = activity?.layoutInflater
            val dialogView = layoutInflater?.inflate(R.layout.update_pass_dialog,null)
            dialogBuilder.setView(dialogView)
            val alertDialog = dialogBuilder.create()
            alertDialog.show()

            dialogView?.findViewById<Button>(R.id.update_btn)?.setOnClickListener {
                val newPass =
                    dialogView.findViewById<TextInputEditText>(R.id.update_password)?.text.toString()

                // Prompt the user to re-authenticate
                val credential = EmailAuthProvider.getCredential(
                    auth?.email!!,
                    dialogView.findViewById<TextInputEditText>(R.id.old_password)?.text.toString()
                ) // Replace "current_password" with the user's current password
                auth.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
                    if (reAuthTask.isSuccessful) {
                        // User has been re-authenticated, proceed with password update
                        auth.updatePassword(newPass).addOnCompleteListener { updatePassTask ->
                            if (updatePassTask.isSuccessful) {
                                Toast.makeText(
                                    requireContext(),
                                    "Password updated successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                alertDialog.dismiss()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to update password: ${updatePassTask.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                alertDialog.dismiss()
                            }
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Re-authentication failed: ${reAuthTask.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        alertDialog.dismiss()
                    }
                }
            }

//            dialogView?.findViewById<Button>(R.id.update_btn)?.setOnClickListener {
//                auth?.updatePassword(newpass)?.addOnCompleteListener { task->
//                    if(task.isSuccessful){
//                        Toast.makeText(requireContext(),"Password Updated Successfully!!",Toast.LENGTH_SHORT).show()
//                    }else{
//                        Toast.makeText(requireContext(),"Something went wrong try again",Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }


        }


    }

    fun fetchprofile(){

        val apiService = RetrofitAllEmp.apiService

        val sharedPreferences = requireContext().getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val companyid = sharedPreferences.getString("company_id", null) ?: return
        val useremail = FirebaseAuth.getInstance().currentUser!!.email
        Log.d("ClientEmail",useremail.toString())

      //  dismissprogressbar()
        val call: Call<Map<String, EmployeeDetails>> = apiService.getData(companyid)
        call.enqueue(object : Callback<Map<String, EmployeeDetails>> {
            override fun onResponse(
                call: Call<Map<String, EmployeeDetails>>,
                response: Response<Map<String, EmployeeDetails>>
            ) {
                if (response.isSuccessful) {
                    val data: Map<String, EmployeeDetails>? = response.body()
                    val searchEmail = useremail
                    val foundEntry = data?.values?.find {
                        it.email == useremail.toString()
                    }
                    if (foundEntry != null) {
                        // Found the email
                        // Toast.makeText(requireContext(),"found",Toast.LENGTH_SHORT).show()
                        dismissprogressbar()
                   //     Toast.makeText(requireContext(),"found", Toast.LENGTH_SHORT).show()
                        binding.EmpNameTextView.text = foundEntry.first_name
                        binding.empCompanyName.text = foundEntry.employee_id
                        binding.empIndustry.text = foundEntry.office
                        binding.empCompanyId.text = foundEntry.company_id
                        binding.empEmail.text = foundEntry.email
                        binding.empPhone.text = foundEntry.mobile.toString()
                        binding.profileDescriptionTextView.text = foundEntry.designation
                    } else {
                        dismissprogressbar()
                        Toast.makeText(requireContext(),"Profile Not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle error response
                    dismissprogressbar()
                    Toast.makeText(requireContext(),response.toString(), Toast.LENGTH_SHORT).show()
                }

                dismissprogressbar()
            }

            override fun onFailure(call: Call<Map<String, EmployeeDetails>>, t: Throwable) {
                // Handle failure
                dismissprogressbar()
                Toast.makeText(requireContext(), "API request failed: ${t.message}", Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }

        })




    }
    private fun showprogressbar(){
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("Loading profile...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()

    }
    private fun dismissprogressbar(){
        progressDialog?.dismiss()
    }

}