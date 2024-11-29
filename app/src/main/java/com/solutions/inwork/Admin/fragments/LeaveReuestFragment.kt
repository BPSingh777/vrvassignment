package com.solutions.inwork.Admin.fragments

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.solutions.inwork.Admin.adapters.LeaveAdapter
import com.solutions.inwork.Admin.dataclasses.LeaveData
import com.solutions.inwork.databinding.FragmentLeaveReuestBinding
import com.solutions.inwork.retrofitget.RetrofitLeaves
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LeaveReuestFragment : Fragment() {

    private lateinit var binding: FragmentLeaveReuestBinding
    private lateinit var adapter : LeaveAdapter
    private var progressDialog: ProgressDialog? = null
    private lateinit var Employee_id : String
    private lateinit var LeaveDataList: List<LeaveData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLeaveReuestBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment


         binding.LeavrReqrecyclerview.layoutManager = LinearLayoutManager(context)


//        binding.searchbtn.setOnClickListener {
//            if(binding.leaveseacrhEditText.text.isEmpty()){
//                Toast.makeText(requireContext(),"Fill the Employee ID",Toast.LENGTH_SHORT).show()
//            }else{
//                Employee_id = binding.leaveseacrhEditText.text.toString()
//
//            }
//        }
        initialize()
        showprogressbar()

        return binding.root
    }

    private fun fetchLeave() {
        val apiService = RetrofitLeaves.create()
        val sharedPreferences = requireContext().getSharedPreferences("ADMIN_PREF", Context.MODE_PRIVATE)
        val companyid = sharedPreferences.getString("company_id", null) ?: return
     //   val employeeId = Employee_id //S20558
        val call = apiService.getData(companyid)

        call.enqueue(object : Callback<Map<String, LeaveData>> {
            override fun onResponse(
                call: Call<Map<String, LeaveData>>,
                response: Response<Map<String, LeaveData>>
            ) {
                if (response.isSuccessful) {
                    val LeaveMap = response.body()
                    if (LeaveMap != null) {
                        LeaveDataList = LeaveMap.values.toList()

                        val sortedList = LeaveDataList.sortedByDescending { leave ->
                            if (leave.approved == 1 || leave.approved == 0) {
                                0 // Employees with approved or declined leave will have a lower sorting value
                            } else {
                                1 // Employees with leave not approved or declined will have a higher sorting value
                            }
                        }


                        dismissprogressbar()
                        adapter = LeaveAdapter(requireContext(),sortedList)
                        binding.LeavrReqrecyclerview.adapter = adapter
                        Log.d("leave",LeaveMap.toString())
                     //   Toast.makeText(requireContext(),LeaveMap.toString(),Toast.LENGTH_SHORT).show()
                    }

                } else {
                    dismissprogressbar()
                    Toast.makeText(requireContext(),"Leave requests are null",Toast.LENGTH_SHORT).show()
                    Log.d("leave", "API request failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Map<String, LeaveData>>, t: Throwable) {
                Log.d("leavefailure", "API request failed", t)
                dismissprogressbar()
            }
        })
    }

    private fun setupSearchEditText() {
        binding.leaveseacrhEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not used
            }

            override fun afterTextChanged(s: Editable?) {
                val searchEmployeeId = s.toString()
                val searchEmpName = s.toString().capitalize()

                if (searchEmployeeId.isBlank()) {
                    // If the search text is empty, restore the original data in the RecyclerView
                    adapter.submitList(LeaveDataList)
                } else {
                    binding.searchbtn.setOnClickListener {
                        showprogressbar()
                        // Filter the employee data based on the entered employee ID or name
                        val filteredList = LeaveDataList.filter { it.employee_id == searchEmployeeId || it.employee_name.contains(searchEmpName) }
                        if (filteredList.isNotEmpty()) {
                            dismissprogressbar()
                            // Sort the filtered list based on the employee name
                            val sortedList = filteredList.sortedBy { it.employee_name }
                            adapter.submitList(sortedList)
                        } else {
                            Toast.makeText(requireContext(), "No such employee found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        })
    }
    private fun initialize() {
        // Fetch employee data
        fetchLeave()


        // Set up the search functionality
        setupSearchEditText()


    }
    private fun showprogressbar(){
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("Fetching details...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()

    }
    private fun dismissprogressbar(){
        progressDialog?.dismiss()
    }

}