package com.solutions.inwork.Admin.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.solutions.inwork.Admin.dataclasses.ProfileModel
import com.solutions.inwork.LogInActivity
import com.solutions.inwork.R
import com.solutions.inwork.databinding.FragmentAdminProfileBinding
import com.solutions.inwork.retrofitget.RetrofitAdminProfile
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AdminProfileFragment : Fragment() {

    private lateinit var binding: FragmentAdminProfileBinding
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminProfileBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment



        binding.profileLgtBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val sharedPreferences = requireContext().getSharedPreferences("ADMIN_PREF",
                AppCompatActivity.MODE_PRIVATE
            )
            val editor = sharedPreferences.edit()
            editor.putString("company_id",null)
            editor.putString("fcm",null)
            editor.putString("company_name",null)
            editor.apply()
            startActivity(Intent(activity, LogInActivity::class.java))
            Toast.makeText(requireContext(), "Logout", Toast.LENGTH_SHORT).show()
            activity?.finish()
        }


        updatepass()
        showprogressbar()
        fetchProfile()
        return binding.root
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

    private fun fetchProfile() {
        val apiService = RetrofitAdminProfile.apiService

        val sharedPreferences = requireContext().getSharedPreferences("ADMIN_PREF", Context.MODE_PRIVATE)
        val companyid = sharedPreferences.getString("company_id", null) ?: return
        val useremail = FirebaseAuth.getInstance().currentUser!!.email

        val call: Call<Map<String, ProfileModel>> = apiService.getData(companyid)
        call.enqueue(object : Callback<Map<String, ProfileModel>> {
            override fun onResponse(
                call: Call<Map<String, ProfileModel>>,
                response: Response<Map<String, ProfileModel>>
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
                        dismissprogressbar()

                                binding.EmpNameTextView.text = foundEntry.managing_director
                                binding.empCompanyName.text = foundEntry.company_name
                                binding.empIndustry.text = foundEntry.industry
                                binding.empCompanyId.text = foundEntry.company_id
                                binding.empEmail.text = foundEntry.email
                                binding.empPhone.text = foundEntry.mobile.toString()
                                binding.profileDescriptionTextView.text = "Managing Director"
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

