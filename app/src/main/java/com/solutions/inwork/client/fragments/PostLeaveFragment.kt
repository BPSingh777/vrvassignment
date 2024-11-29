package com.solutions.inwork.client.fragments

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.solutions.inwork.databinding.FragmentPostLeaveBinding
import com.solutions.inwork.retrofitPost.RetrofitLeavePost
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PostLeaveFragment : Fragment() {


    private lateinit var binding: FragmentPostLeaveBinding
    private lateinit var date : String
    private lateinit var tilldate : String
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostLeaveBinding.inflate(inflater,container,false)

        binding.btndatepicker.setOnClickListener {
            datePicker()
        }
        binding.btntilldatepicker.setOnClickListener{
            tilldatePicker()
        }


        binding.postleavebtn.setOnClickListener {
            if( binding.leaveDateEditText.text.isNullOrEmpty() || binding.leaveReasonEditText.text.isNullOrEmpty() || binding.designationEditText.text.isNullOrEmpty() || binding.leavetillDateEditText.text.isNullOrEmpty()){
                Toast.makeText(requireContext(),"Please fill all the details",Toast.LENGTH_SHORT).show()
            }else{
                showprogressbar()
                SendLeave()
            }
        }
        return binding.root
    }
    private fun datePicker() {
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

                    binding.leaveDateEditText.setText(date)
                },
                year,
                month,
                dayOfMonth
            ).apply {
                // Set minimum date to current date
                datePicker.minDate = currentDate.timeInMillis
            }
        }

        datePicker?.show()
    }

    private fun tilldatePicker() {
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePicker = activity?.let { it1 ->
            DatePickerDialog(
                it1,
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    // Date selection logic
                    tilldate = String.format(
                        "%04d-%02d-%02d",
                        selectedYear,
                        selectedMonth + 1,
                        selectedDayOfMonth
                    )

                    binding.leavetillDateEditText.setText(tilldate)
                },
                year,
                month,
                dayOfMonth
            ).apply {
                // Set minimum date to current date
                datePicker.minDate = currentDate.timeInMillis
            }
        }

        datePicker?.show()
    }

    private fun SendLeave(){

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = Date()
        val timestamp = dateFormat.format(currentDate)

        val sharedPreferences = requireContext().getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val companyid = sharedPreferences.getString("company_id", null) ?: return
        val employeeid = sharedPreferences.getString("employee_id",null) ?: return
        val employeeName = sharedPreferences.getString("name",null) ?: return
        val companyName = sharedPreferences.getString("company_name",null) ?: return
        val jsonObject = JSONObject().apply {
            put("company_id", companyid)
            put("company_name", companyName)
            put("employee_id", employeeid)
            put("employee_name", employeeName)
            put("designation", binding.designationEditText.text.toString().trim())
            put("leave_date",date)
            put("leave_reason",binding.leaveReasonEditText.text.toString().trim())
            put("time_stamp",timestamp)
            put("till_date",tilldate)
        }


        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(),jsonObject.toString())


        val apiService = RetrofitLeavePost.apiService
        val call = apiService.postData(requestBody)


        call.enqueue(object : Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful){
                    dismissprogressbar()
               //     binding.companyIdEditText.text!!.clear()
                //    binding.companyNameEditText.text!!.clear()
                //    binding.employeeIdEditText.text!!.clear()
                  //  binding.employeeNameEditText.text!!.clear()
                    binding.designationEditText.text!!.clear()
                    binding.leaveDateEditText.text!!.clear()
                    binding.leavetillDateEditText.text!!.clear()
                    binding.leaveReasonEditText.text!!.clear()
                    Toast.makeText(requireContext(),"Leave Posted Successfully",Toast.LENGTH_SHORT).show()
                }
                else{
                    dismissprogressbar()
                    Toast.makeText(requireContext(),"Something went wrong!!",Toast.LENGTH_SHORT).show()
                    Log.d("SendLeave",response.message())
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                dismissprogressbar()
                Toast.makeText(requireContext(),t.toString(),Toast.LENGTH_SHORT).show()
                Log.d("SendLeave",t.toString())
            }

        })
    }
    private fun showprogressbar(){
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("Posting Leave...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()

    }
    private fun dismissprogressbar(){
        progressDialog?.dismiss()
    }





}