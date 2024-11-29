package com.solutions.inwork.client.fragments

import android.app.ProgressDialog
import android.content.AbstractThreadedSyncAdapter
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.solutions.inwork.Admin.dataclasses.LeaveData
import com.solutions.inwork.R
import com.solutions.inwork.client.adapter.LeaveClientAdapter
import com.solutions.inwork.databinding.FragmentClientLeaveBinding
import com.solutions.inwork.retrofitget.RetrofitGetLeaveEmployee
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientLeaveFragment : Fragment() {

    private lateinit var binding: FragmentClientLeaveBinding
    private lateinit var adapter: LeaveClientAdapter
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentClientLeaveBinding.inflate(inflater,container,false)

        binding.clientleaveRecyclerview.layoutManager = LinearLayoutManager(context)

        showprogressbar()
        getLeaveData()


        return binding.root
    }

    fun getLeaveData(){
        val sharedPreferences = requireContext().getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val employeeID = sharedPreferences.getString("employee_id", null) ?: return
        val companyID = sharedPreferences.getString("company_id", null) ?: return


        val apiService = RetrofitGetLeaveEmployee.apiService
        val call = apiService.getData(companyID,employeeID)

        call.enqueue(object: Callback<Map<String,LeaveData>>{
            override fun onResponse(
                call: Call<Map<String, LeaveData>>,
                response: Response<Map<String, LeaveData>>
            ) {
                if (response.isSuccessful){
                    val LeaveMap = response.body()?.values?.toList()
                    if (LeaveMap != null){
                        dismissprogressbar()
                        adapter = LeaveClientAdapter(LeaveMap)
                        binding.clientleaveRecyclerview.adapter = adapter
                    }else{
                        dismissprogressbar()
                        Toast.makeText(requireContext(),"No Leaves",Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    dismissprogressbar()
                    Toast.makeText(requireContext(),"Response Unsuccessful!!",Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: Call<Map<String, LeaveData>>, t: Throwable) {
                dismissprogressbar()
               Toast.makeText(requireContext(),"Something Went Wrong!!",Toast.LENGTH_SHORT).show()
            }

        })
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