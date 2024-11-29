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
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.solutions.inwork.Admin.adapters.ConnectivityAdapter
import com.solutions.inwork.Admin.dataclasses.AdminConnectivity
import com.solutions.inwork.Admin.dataclasses.EmployeeDetails
import com.solutions.inwork.Admin.dataclasses.EmployeeStatus
import com.solutions.inwork.databinding.FragmentAdminHomeBinding
import com.solutions.inwork.retrofitget.RetrofitAllEmp
import com.solutions.inwork.retrofitget.RetrofitCoonectivityGet
import com.solutions.inwork.retrofitget.RetrofitgetAdminStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AdminHomeFragment : Fragment() {

    private lateinit var binding : FragmentAdminHomeBinding
    private var progressDialog: ProgressDialog? = null
    private lateinit var EmpdataList: List<AdminConnectivity>
    private lateinit var adapter : ConnectivityAdapter
    private var fragmentContext: Context? = null
    private var checkedInCount: Int = 0
    private var checkOutCount: Int = 0


    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onDetach() {
        super.onDetach()
        fragmentContext = null
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminHomeBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment

        binding.statusConnectivityRecyclerview.layoutManager = LinearLayoutManager(context)

        showprogressbar()
        initialize()




        return binding.root
    }

    fun getTotalEMployee(companyId : String){
        val sf = requireContext().getSharedPreferences("TotalEmp",Context.MODE_PRIVATE)
        val apiService = RetrofitAllEmp.apiService
        apiService.getData(companyId).enqueue(object: Callback<Map<String,EmployeeDetails>>{
            override fun onResponse(
                call: Call<Map<String, EmployeeDetails>>,
                response: Response<Map<String, EmployeeDetails>>
            ) {

                if (response.isSuccessful){
                    val dataMap = response.body()?.values?.size
                    if (dataMap != null && dataMap != sf.getInt("Total",0)) {

                            sf.edit().putInt("Total",dataMap).apply()

                    }

                }
            }

            override fun onFailure(call: Call<Map<String, EmployeeDetails>>, t: Throwable) {

            }


        })

    }

    private fun getCount(companyId : String){

        val sf = requireContext().getSharedPreferences("TotalEmp",Context.MODE_PRIVATE)




        var TotalEMployee  = sf.getInt("Total",0)

       val apiService = RetrofitgetAdminStatus.apiService

        // Call the API method to get the status data
        apiService.getData(companyId).enqueue(object : Callback<Map<String,EmployeeStatus>> {
            override fun onResponse(
                call: Call<Map<String, EmployeeStatus>>,
                response: Response<Map<String, EmployeeStatus>>
            ) {
                if (response.isSuccessful) {
                    val statusresponse = response.body()
                    statusresponse?.let { response ->
                        val DataMap = response.values.toList()
                        for (item in DataMap) {
                            when (item.current_status) {
                                "CHECKED IN" -> checkedInCount++
                                "CHECKED OUT" -> checkOutCount++
                            }
                        }
                        binding.checkedInEmployee.text = "Checked In : $checkedInCount"
                        binding.checkedOutEmployee.text = "Checked Out : $checkOutCount"
                        binding.UnknownStatus.text = "Unknown : ${TotalEMployee - (checkedInCount + checkOutCount)}"
                        binding.TotalEmployees.text = "Total: $TotalEMployee"
//                        getTotalEMployee(companyId){total ->
//                            binding.UnknownStatus.text = "Unknown Status: ${total - (checkedInCount + checkOutCount)}"
//                            binding.TotalEmployees.text = "Total: $total"
//                        }
                    }
                }
                else {
                   Log.d("homeAdmin","error")
                    binding.checkedInEmployee.text = "Checked In : $checkedInCount"
                    binding.checkedOutEmployee.text = "Checked Out : $checkOutCount"
                }

            }
            override fun onFailure(call: Call<Map<String, EmployeeStatus>>, t: Throwable) {
                Log.d("homeAdmin",t.toString())
                binding.checkedInEmployee.text = "Checked In : $checkedInCount"
                binding.checkedOutEmployee.text = "Checked Out : $checkOutCount"
            }

        })
    }


    fun  fetchConnectivity(){
        val apiService = RetrofitCoonectivityGet.apiService
        val sharedPreferences = requireContext().getSharedPreferences("ADMIN_PREF", Context.MODE_PRIVATE)
        val companyid = sharedPreferences.getString("company_id", null) ?: return
        val call = apiService.getData(companyid)



        getTotalEMployee(companyid)
        getCount(companyid)


        call.enqueue(object : Callback<Map<String,AdminConnectivity>> {
            override fun onResponse(
                call: Call<Map<String, AdminConnectivity>>,
                response: Response<Map<String, AdminConnectivity>>
            ) {
                if (response.isSuccessful){
                    val EmpMap = response.body()
                    if (EmpMap != null) {
                        EmpdataList = EmpMap.values.toList()
                        val context = fragmentContext
                        if (context != null) {
                            adapter = ConnectivityAdapter(context, EmpdataList)
                            binding.statusConnectivityRecyclerview.adapter = adapter
                        }

                        Log.d("emp",EmpMap.toString())
                        dismissprogressbar()

                    }
                }else{
                    dismissprogressbar()
                    Log.d("emp", "API request failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Map<String, AdminConnectivity>>, t: Throwable) {
                dismissprogressbar()
                Log.d("ConnectivityStatus",t.toString())
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
                        val filteredList = EmpdataList.filter { it.employee_id == searchEmployeeId }
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
        fetchConnectivity()


        // Set up the search functionality
        setupSearchEditText()


    }

    private fun showprogressbar(){
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("Fetching Details...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()

    }
    private fun dismissprogressbar(){
        progressDialog?.dismiss()
    }
}