package com.solutions.inwork.Admin.fragments

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.solutions.inwork.Admin.adapters.EmployeeStatusAdapter
import com.solutions.inwork.Admin.dataclasses.EmployeeStatus
import com.solutions.inwork.databinding.FragmentEmployeeStatusBinding
import com.solutions.inwork.retrofitget.RetrofitgetAdminStatus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmployeeStatusFragment : Fragment() {


    private lateinit var binding:FragmentEmployeeStatusBinding
    private lateinit var adapter : EmployeeStatusAdapter
    private var progressDialog: ProgressDialog? = null
    private lateinit var EmpdataList: List<EmployeeStatus>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmployeeStatusBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment

        binding.StatusRecyclerview.layoutManager = LinearLayoutManager(context)


        showprogressbar()
        initialize()
        return binding.root
    }

    fun fetchStatus(){
        val apiService = RetrofitgetAdminStatus.apiService
        val sharedPreferences = requireContext().getSharedPreferences("ADMIN_PREF", Context.MODE_PRIVATE)
        val companyid = sharedPreferences.getString("company_id", null) ?: return
        val call = apiService.getData(companyid)


        call.enqueue(object: Callback<Map<String,EmployeeStatus>>{
            override fun onResponse(
                call: Call<Map<String, EmployeeStatus>>,
                response: Response<Map<String, EmployeeStatus>>
            ) {
                if (response.isSuccessful){
                    val dataMap = response.body()
                    if (dataMap!=null){
                        dismissprogressbar()
                        EmpdataList = dataMap.values.toList()
                        adapter = EmployeeStatusAdapter(EmpdataList)
                        binding.StatusRecyclerview.adapter = adapter
                    }
                }else{
                    dismissprogressbar()
                    Log.d("data", "API request failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Map<String, EmployeeStatus>>, t: Throwable) {
                dismissprogressbar()
                Log.d("data", "API request failed", t)
            }

        })



    }

    private fun setupSearchEditText() {
        binding.StatusSearchView.addTextChangedListener(object : TextWatcher {
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
                    binding.searchbtn.setOnClickListener {
                        // Filter the employee data based on the entered employee ID
                        val filteredList = EmpdataList.filter { it.employee_id == searchEmployeeId  }
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
        fetchStatus()


        // Set up the search functionality
        setupSearchEditText()


    }

    private fun showprogressbar(){
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("Loading...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()

    }
    private fun dismissprogressbar(){
        progressDialog?.dismiss()
    }

}