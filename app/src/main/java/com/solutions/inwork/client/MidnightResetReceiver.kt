package com.solutions.inwork.client

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.solutions.inwork.Admin.adapters.EmployeeAdapter
import com.solutions.inwork.Admin.dataclasses.EmployeeDetails
import com.solutions.inwork.Admin.dataclasses.EmployeeStatus
import com.solutions.inwork.R
import com.solutions.inwork.client.fragments.ClientHomeFragment
import com.solutions.inwork.retrofitPost.RetrofitPostScreentime
import com.solutions.inwork.retrofitget.EmployeeCurrentStatusGet
import com.solutions.inwork.retrofitget.RetrofitAllEmp
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

class MidnightResetReceiver : BroadcastReceiver() {

    private var bool = true
    override fun onReceive(context: Context, intent: Intent) {


        val  sharedPreferences = context.getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val work_end = sharedPreferences.getString("work_end_time", null)
        val employeeID = sharedPreferences.getString("employee_id", null) ?: return
        val companyID = sharedPreferences.getString("company_id", null) ?: return


        if (work_end !=null){

            val (hours, minutes) = work_end.split(":").map { it.toInt() }

            val desiredSecond = 0



            // Get the current time
            val currentTime = Calendar.getInstance()
            val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
            val currentMinute = currentTime.get(Calendar.MINUTE)
            val currentSecond = currentTime.get(Calendar.SECOND)

            if (currentHour == hours && currentMinute == minutes && currentSecond == desiredSecond ) {
                val sf = context.getSharedPreferences("Screen",Context.MODE_PRIVATE)
                val isSent = sf.getBoolean("Work_end_sent",true)
                Log.d("Screentime212",isSent.toString())
                if (isSent){
                    if(ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED) {


                        fetchData(companyID,employeeID){bool ->

                            if (bool){
                                ClientHomeFragment().SendAppUsageTime(context, true)
                                with(sf.edit()) {
                                    putBoolean("Work_end_sent", false)
                                    apply()
                                }
                            }

                        }


                    }else{
                        SendScreenTimeShift(context)
                    }
                }

              //  bool = false
            }else{
                val sf = context.getSharedPreferences("Screen",Context.MODE_PRIVATE)
                sf.edit().putBoolean("Work_end_sent",true).apply()
                Log.d("Screentime213",sf.getBoolean("Work_end_sent",false).toString())

               // bool = true
            }


            val fragment = ClientHomeFragment()
            fragment.view?.apply {

                // Reset the EditText values
                findViewById<EditText>(R.id.whatsappDurationText).text = null
                findViewById<EditText>(R.id.facebookDurationText).text = null
                findViewById<EditText>(R.id.twitterDurationText).text = null
                findViewById<EditText>(R.id.instagramDurationText).text = null
                findViewById<EditText>(R.id.callsDurationText).text = null
                findViewById<EditText>(R.id.gamesDurationText).text = null
                findViewById<EditText>(R.id.newsDurationText).text = null
                findViewById<EditText>(R.id.othersDurationText).text = null
            }
        }


//        fragment.SendScreenTime(context)


//        val currentDate = LocalDate.now()
//        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//        val formattedDate = currentDate.format(dateFormatter)
//
//        val jsonObject = JSONObject().apply {
//            put("company_id", "CIN05")
//            put("company_name", "INVYU")
//            put("employee_id", "M20556")
//            put("employee_name", "Kanishk")
//            put("designation", "AndDev")
//            put("work_start_time", "10:00:00")
//            put("work_end_time", "18:00:00")
//            put("whatsapp_duration", "")
//            put("facebook_duration", "")
//            put("instagram_duration", "")
//            put("twitter_duration", "")
//            put("News_duration", "")
//            put("Games_duration", "")
//            put("calls_duration", "")
//            put("others_duration", "")
//            put("date", formattedDate)
//        }
//
//        val requestBody =
//            RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())
//
//        // POST request
//        val apiService = RetrofitPostScreentime.apiService
//        val call = apiService.postData(requestBody)
//
//        call.enqueue(object : Callback<Void> {
//            override fun onResponse(call: Call<Void>, response: Response<Void>) {
//                if (response.isSuccessful) {
//                    dismissprogressbar()
//                    binding.apply {
//                        whatsappDurationText.text.clear()
//                        facebookDurationText.text.clear()
//                        instagramDurationText.text.clear()
//                        twitterDurationText.text.clear()
//                        newsDurationText.text.clear()
//                        gamesDurationText.text.clear()
//                        callsDurationText.text.clear()
//                        othersDurationText.text.clear()
//                    }
//                    Toast.makeText(
//                        requireContext(),
//                        "Screen time details sent successfully",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                } else {
//                    dismissprogressbar()
//                    Toast.makeText(
//                        requireContext(),
//                        "Something went wrong!!",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    Log.d("SendScreentime", response.message())
//                }
//            }
//
//            override fun onFailure(call: Call<Void>, t: Throwable) {
//                dismissprogressbar()
//                Toast.makeText(requireContext(), t.toString(), Toast.LENGTH_SHORT).show()
//                Log.d("SendScreentime", t.toString())
//            }
//        })
    }


    fun SendScreenTimeShift(context: Context){

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
            put("whatsapp_duration", "00:00:00")
            put("facebook_duration", "00:00:00")
            put("instagram_duration", "00:00:00")
            put("twitter_duration", "00:00:00")
            put("News_duration", "00:00:00")
            put("Games_duration", "00:00:00")
            put("calls_duration", "00:00:00")
            put("others_duration", "00:00:00")
            put("time_stamp", formattedDate)
        }

        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())

        // POST request
        val apiService = RetrofitPostScreentime.apiService
        val call = apiService.postData(requestBody)

        call.enqueue(object: Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {

                if (response.isSuccessful){

                    Log.d("SendScreentime","sucees")

                    Toast.makeText(context,"Screen time details sent successfully", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context,"Something went wrong!!", Toast.LENGTH_SHORT).show()
                    Log.d("SendScreentime",response.message())
                }

            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context,t.toString(), Toast.LENGTH_SHORT).show()
                Log.d("SendScreentime", t.toString())
            }

        })



    }


    private fun fetchData(companyid: String,employeeId: String, callback: (Boolean) -> Unit) {
        val apiService = EmployeeCurrentStatusGet.apiService

        val call = apiService.getData(companyid,employeeId)

        call.enqueue(object : Callback<Map<String, EmployeeStatus>> {
            override fun onResponse(
                call: Call<Map<String, EmployeeStatus>>,
                response: Response<Map<String, EmployeeStatus>>
            ) {
                if (response.isSuccessful) {
                    val EmpMap = response.body()
                    if (EmpMap != null) {
                       val EmpdataList = EmpMap.values.toList()

                        // Accessing the current_status for each employee
                        for (employeeDetails in EmpdataList) {
                            val currentStatus = employeeDetails.current_status
                            // Do something with the currentStatus (e.g., display, process, etc.)

                            if (currentStatus == "CHECKED IN"){
                                callback(true)
                            }
                            else{
                                callback(false)
                            }
                            Log.d("EMployee status",currentStatus.toString())

                        }

                        Log.d("emp", EmpMap.toString())

                    }
                    callback(false)
                } else {
                    callback(false)
                    Log.d("emp", "API request failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Map<String, EmployeeStatus>>, t: Throwable) {
                callback(false)
                Log.d("emp", "API request failed", t)
            }

        })
    }



}


