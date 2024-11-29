package com.solutions.inwork.Admin.fragments

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.solutions.inwork.databinding.FragmentSendNoticeBinding
import com.solutions.inwork.retrofitPost.RetrofitNoticePost
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class SendNoticeFragment : Fragment() {

    private lateinit var binding: FragmentSendNoticeBinding
    private lateinit var timeStamp: String
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSendNoticeBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // Get the current timestamp as a Date object
        val currentDate = Date()

        // Format the Date object to a string in the desired format
        timeStamp = dateFormat.format(currentDate)

        binding.SendNoticebtn.setOnClickListener {
            if (binding.TitleEditText.text.isNullOrEmpty() ||binding.NoticeEdittext.text.isNullOrEmpty()){
            Toast.makeText(requireContext(),"Please fill all the details",Toast.LENGTH_SHORT).show()
//                Toast.makeText(requireContext(),timeStamp,Toast.LENGTH_SHORT).show()
            }else{
                showprogressbar()
                SendNotice()
            }
        }




        return binding.root




    }

    private fun SendNotice(){

        val sharedPreferences = requireContext().getSharedPreferences("ADMIN_PREF", Context.MODE_PRIVATE)
        val companyid = sharedPreferences.getString("company_id", null) ?: return
        val companyName = sharedPreferences.getString("company_name",null) ?: return
        val jsonObject = JSONObject().apply {
            put("company_id", companyid)
            put("company_name", companyName)
            put("title", binding.TitleEditText.text.toString().trim())
            put("notice", binding.NoticeEdittext.text.toString().trim())
            put("time_stamp", timeStamp)
        }

        // JSON object
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())

      // POST request
        val apiService = RetrofitNoticePost.apiService
        val call = apiService.postData(requestBody)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Request successful

                    sentNoticeNotification()
                  //  binding.companyIDeditext.text!!.clear()

                    Toast.makeText(requireContext(),"Notice Sent Successfully",Toast.LENGTH_SHORT).show()
                    dismissprogressbar()
                } else {
                    dismissprogressbar()
                    // Request failed
                    Toast.makeText(requireContext(),"Something went wrong!!",Toast.LENGTH_SHORT).show()
                    Log.d("SendNotice",response.message())
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Request failed due to network error or other issues
                dismissprogressbar()
                Toast.makeText(requireContext(),t.toString(),Toast.LENGTH_SHORT).show()
                Log.d("SendNotice", t.toString())
            }
        })


    }

    private fun sentNoticeNotification(){

        val sharedPreferences = requireContext().getSharedPreferences("ADMIN_PREF", Context.MODE_PRIVATE)
        val companyid = sharedPreferences.getString("company_id", null) ?: return


        val database = FirebaseDatabase.getInstance()
        val path = database.getReference(companyid)
        Log.d("DatabaseReference", "Reference path notice: ${path.toString()}")

        path.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (employeeSnapshot in dataSnapshot.children) {
                    val employeeId = employeeSnapshot.key
                //    val tokenList = mutableListOf<String>()
                    for (tokenSnapshot in employeeSnapshot.children) {
                        val token = tokenSnapshot.getValue(String::class.java)
                        if (token != null) {

                            val url = "https://fcm.googleapis.com/fcm/send"

                            val jsonObject = JSONObject()
                            val notificationObject = JSONObject()
                            notificationObject.put("title", "Notice Board")
                            notificationObject.put("message", binding.NoticeEdittext.text.toString())
                            notificationObject.put("fragment", "NOTICE_FRAGMENT_TAG")
                            jsonObject.put("data", notificationObject)
                            jsonObject.put("to", token)

                            val request: JsonObjectRequest = object : JsonObjectRequest(
                                Method.POST, url, jsonObject,
                                { response ->
                                    dismissprogressbar()
                                    // Handle the response here
                                    Log.d("volley","success")
                                    binding.TitleEditText.text!!.clear()
                                    //      binding.companyNameeditext.text!!.clear()
                                    binding.NoticeEdittext.text!!.clear()
                                  //  Toast.makeText(context,"Successful",Toast.LENGTH_SHORT).show()
                                },
                                { error ->
                                    dismissprogressbar()

                                    // Handle the error here
                                    Log.d("volley","fail")

                                    Toast.makeText(context,"Failed to notify",Toast.LENGTH_SHORT).show()

                                }) {
                                override fun getHeaders(): MutableMap<String, String> {
                                    val headers = HashMap<String, String>()
                                    headers["Authorization"] ="key=AAAAa8gk1zc:APA91bEXlVMS52U7wSCnoNjRwmS0SsZWC__LZIh3cazVJDB7U_nN3x8VvvLR8T2YOckUMuXD8FB66v5pNTLqILQUFTecetIE-RMpzGyJCM-NlqjVOv6n3jmG_xdjxTzphB82Yf41kP-k"
                                    headers["Content-Type"] = "application/json"
                                    return headers
                                }
                            }
                            Volley.newRequestQueue(context).add(request)

                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                dismissprogressbar()
                // Handle any errors
            }
        })



//        val companyRef = FirebaseDatabase.getInstance().getReference(companyid)
//        companyRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (employeeSnapshot in dataSnapshot.children) {
//                    val employeeToken = employeeSnapshot.getValue(String::class.java)
//
//
//
//
//                    // Do something with the employee token, such as sending a notice
//                    // For example, you can use the employeeToken to send a notification using FCM
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Handle any errors or cancellation of the database query
//            }
//        })



    }

    private fun showprogressbar(){
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("Posting Notice...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()

    }
    private fun dismissprogressbar(){
        progressDialog?.dismiss()
    }


}