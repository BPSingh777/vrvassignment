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
import com.solutions.inwork.Admin.dataclasses.EmployeeDetails
import com.solutions.inwork.Admin.adapters.EmployeeAdapter
import com.solutions.inwork.databinding.FragmentAllEmployeesBinding
import com.solutions.inwork.retrofitget.RetrofitAllEmp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AllEmployeesFragment : Fragment() {


    private lateinit var binding: FragmentAllEmployeesBinding
    private lateinit var adapter: EmployeeAdapter
    private var progressDialog: ProgressDialog? = null
    private lateinit var EmpdataList: List<EmployeeDetails>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllEmployeesBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment


        binding.AllEmprecyclerview.layoutManager = LinearLayoutManager(context)


        showprogressbar()

        initialize()

        return binding.root
    }

    private fun fetchData() {
        val apiService = RetrofitAllEmp.apiService
        val sharedPreferences = requireContext().getSharedPreferences("ADMIN_PREF", Context.MODE_PRIVATE)
        val companyid = sharedPreferences.getString("company_id", null) ?: return
        val call = apiService.getData(companyid)

        call.enqueue(object : Callback<Map<String, EmployeeDetails>> {
            override fun onResponse(
                call: Call<Map<String, EmployeeDetails>>,
                response: Response<Map<String, EmployeeDetails>>
            ) {
                if (response.isSuccessful) {
                    val EmpMap = response.body()
                    if (EmpMap != null) {
                        EmpdataList = EmpMap.values.toList()
                        adapter = EmployeeAdapter(EmpdataList)
                        binding.AllEmprecyclerview.adapter = adapter
                        Log.d("emp",EmpMap.toString())
                        dismissprogressbar()

                    }
                } else {
                    dismissprogressbar()
                    Log.d("emp", "API request failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Map<String, EmployeeDetails>>, t: Throwable) {
                Log.d("emp", "API request failed", t)
            }

        })
    }

    private fun setupSearchEditText() {
        binding.searchemp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not used
            }

            override fun afterTextChanged(s: Editable?) {
                val searchEmployeeId = s.toString()
                val searchEmpName = s.toString().capitalize().trim()

                if (searchEmployeeId.isBlank()) {
                    // If the search text is empty, restore the original data in the RecyclerView
                    adapter.submitList(EmpdataList)
                } else {
                    binding.srchbtn.setOnClickListener {
                        // Filter the employee data based on the entered employee ID
                        val filteredList = EmpdataList.filter { it.employee_id == searchEmployeeId || it.first_name!!.contains(searchEmpName) || it.last_name!!.contains(searchEmpName) }
                        if (filteredList.isNotEmpty()) {
                            adapter.submitList(filteredList)
                        } else {
                            Toast.makeText(requireContext(), "No employee found with ID $searchEmployeeId", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            }
        })
    }

    private fun initialize() {
        // Fetch employee data
        fetchData()


            // Set up the search functionality
            setupSearchEditText()


    }

    private fun showprogressbar(){
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("Fetching Employees...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()

    }
    private fun dismissprogressbar(){
        progressDialog?.dismiss()
    }


}