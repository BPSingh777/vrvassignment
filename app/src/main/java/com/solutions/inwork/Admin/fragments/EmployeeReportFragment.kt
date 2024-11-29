package com.solutions.inwork.Admin.fragments

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.solutions.inwork.Admin.adapters.EmployeeReportAdapter
import com.solutions.inwork.Admin.dataclasses.EmployeeDetails
import com.solutions.inwork.databinding.FragmentEmployeeReportBinding
import com.solutions.inwork.retrofitget.RetrofitAllEmp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class EmployeeReportFragment : Fragment() {

    private lateinit var binding: FragmentEmployeeReportBinding
    private lateinit var EmpdataList: List<EmployeeDetails>
    private lateinit var adapter: EmployeeReportAdapter
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmployeeReportBinding.inflate(inflater,container,false)

        binding.AllReportRecyclerView.layoutManager = LinearLayoutManager(context)


        requestStoragepermission()
        showprogressbar()
        initialize()


        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, generate the attendance report
                //   generateAttendanceReport()
                Toast.makeText(requireContext(), "Storage permission granted", Toast.LENGTH_SHORT).show()
                showprogressbar()
                initialize()
            } else {
                // Permission denied, handle the error
                Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun requestStoragepermission(){
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 1)
        }
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
                        adapter = EmployeeReportAdapter(EmpdataList)
                        binding.AllReportRecyclerView.adapter = adapter
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
        progressDialog?.setMessage("Fetching Reports...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()

    }
    private fun dismissprogressbar(){
        progressDialog?.dismiss()
    }

}