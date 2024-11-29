package com.solutions.inwork.Admin.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.solutions.inwork.Admin.dataclasses.EmployeeDetails
import com.solutions.inwork.Admin.dataclasses.EmployeeStatus
import com.solutions.inwork.R
import com.solutions.inwork.retrofitget.RetrofitgetAdminStatus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//fun isAppInstalled(context: Context, packageName: String?): Boolean {
//    return try {
//        context.packageManager.getApplicationInfo(packageName!!, 0)
//        true
//    } catch (e: PackageManager.NameNotFoundException) {
//        false
//    }
//}
class EmployeeAdapter(private var EmpList : List<EmployeeDetails>): RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder>() {


    fun submitList(newList: List<EmployeeDetails>) {
        EmpList = newList
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.expanded_emp_details,parent,false)
        return EmployeeViewHolder(view)
    }

    override fun getItemCount(): Int {
       return EmpList.size
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {

        val empdata = EmpList[position]
        holder.bind(holder.itemView.context, empdata)
    }

    class EmployeeViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        private val employeeIdTextView: TextView = itemView.findViewById(R.id.employeeIdTextView)
        private val firstNameTextView: TextView = itemView.findViewById(R.id.firstNameTextView)
        private val lastNameTextView: TextView = itemView.findViewById(R.id.lastNameTextView)
        private val dateOfBirthTextView: TextView = itemView.findViewById(R.id.dateOfBirthTextView)
        private val designationTextView: TextView = itemView.findViewById(R.id.designationTextView)
        private val viewMoreButton: TextView = itemView.findViewById(R.id.viewMoreButton)
        private val expandedDetailsLayout: LinearLayout = itemView.findViewById(R.id.expandedDetailsLayout)
        private val addressTextView: TextView = itemView.findViewById(R.id.addressTextView)
        private val adhaarNumberTextView: TextView = itemView.findViewById(R.id.adhaarNumberTextView)
        private val mobileTextView: TextView = itemView.findViewById(R.id.mobileTextView)
        private val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        private val officeTextView: TextView = itemView.findViewById(R.id.officeTextView)
        private val workStartTimeTextView: TextView = itemView.findViewById(R.id.workStartTimeTextView)
        private val workEndTimeTextView: TextView = itemView.findViewById(R.id.workEndTimeTextView)
        private val workEffectFromDateTextView: TextView = itemView.findViewById(R.id.workEffectFromDateTextView)
        private val workEndFromDateTextView: TextView = itemView.findViewById(R.id.workEndFromDateTextView)
        private val viewLessButton: TextView = itemView.findViewById(R.id.viewLessButton)
        private val status: TextView = itemView.findViewById(R.id.EmployeeStatusTextView)

//        private val previewbtn : Button = itemView.findViewById(R.id.previewpdfbtn)
//        private val downloadbtn : Button = itemView.findViewById(R.id.downloadpdfbtn)

        @SuppressLint("SetTextI18n", "SuspiciousIndentation")
        fun bind(context: Context, employeeData: EmployeeDetails) {
            employeeIdTextView.text = "Employee ID: ${employeeData.employee_id}"
            firstNameTextView.text = "First Name: ${employeeData.first_name}"
            lastNameTextView.text = "Last Name: ${employeeData.last_name}"
            dateOfBirthTextView.text = "Date of Birth: ${employeeData.date_of_birth}"
            designationTextView.text = "Designation: ${employeeData.designation}"
//            if(isAppInstalled(context,"com.solutions.inwork")){
//                status.text = "Installed"
//            }
//            else{
//                status.text = "Uninstalled"
//            }



            viewMoreButton.setOnClickListener {
                expandedDetailsLayout.visibility = View.VISIBLE
                viewMoreButton.visibility = View.GONE
            }

            viewLessButton.setOnClickListener {
                expandedDetailsLayout.visibility = View.GONE
                viewMoreButton.visibility = View.VISIBLE
            }

            addressTextView.text = "Address: ${employeeData.address}"
            adhaarNumberTextView.text = "Adhaar Number: ${employeeData.adhaar_number}"
            mobileTextView.text = "Mobile: ${employeeData.mobile}"
            emailTextView.text = "Email: ${employeeData.email}"
            officeTextView.text = "Office: ${employeeData.office}"
            workStartTimeTextView.text = "Work Start Time: ${employeeData.work_start_time}"
            workEndTimeTextView.text = "Work End Time: ${employeeData.work_end_time}"
            workEffectFromDateTextView.text = "Work Effective From Date: ${employeeData.work_effect_from_date}"
            workEndFromDateTextView.text = "Work End From Date: ${employeeData.work_end_from_date}"



            fetchStatus(context, employeeData.employee_id.toString())
        }

        fun fetchStatus(context: Context, employeeId: String) {
            val apiService = RetrofitgetAdminStatus.apiService
            val sharedPreferences = context.getSharedPreferences("ADMIN_PREF", Context.MODE_PRIVATE)
            val companyId = sharedPreferences.getString("company_id", null) ?: return
            val call = apiService.getData(companyId)

            call.enqueue(object : Callback<Map<String, EmployeeStatus>> {
                override fun onResponse(
                    call: Call<Map<String, EmployeeStatus>>,
                    response: Response<Map<String, EmployeeStatus>>
                ) {
                    if (response.isSuccessful) {
                        val dataMap = response.body()
                        if (dataMap != null) {
                            var currentStatus: String? = null
                            for ((key, employeeStatus) in dataMap) {
                                if (employeeStatus.employee_id == employeeId) {
                                    Log.d("inworkid", employeeStatus.employee_id.toString())

                                    currentStatus = employeeStatus.current_status
                                    break
                                }
                            }

                            Log.d("inworkid1", currentStatus.toString())

                            // Update the status TextView with the current status
                            if (currentStatus != null) {
                                status.text = currentStatus
                                if (currentStatus == "CHECKED IN"){
                                    status.setTextColor(ContextCompat.getColor(context, R.color.GREEN))
                                }else{
                                    status.setTextColor(ContextCompat.getColor(context, R.color.RED))
                                }
                            } else {
                                // Employee status not found
                                status.text = ""
                            }
                        }

                        else {
                            Log.d("data", "Response body is null")
                        }
                    } else {
                        Log.d("data", "API request failed: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<Map<String, EmployeeStatus>>, t: Throwable) {
                    Log.d("data", "API request failed", t)
                }
            })
        }


    }







}
