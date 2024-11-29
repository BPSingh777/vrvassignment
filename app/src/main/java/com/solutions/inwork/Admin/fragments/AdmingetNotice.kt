package com.solutions.inwork.Admin.fragments

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.solutions.inwork.client.dataclasses.DataModel
import com.solutions.inwork.client.adapter.DataAdapter
import com.solutions.inwork.databinding.FragmentAdmingetNoticeBinding
import com.solutions.inwork.retrofitget.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AdmingetNotice : Fragment() {

    private lateinit var binding: FragmentAdmingetNoticeBinding
    private lateinit var adapter: DataAdapter
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdmingetNoticeBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment


        binding.noticerecyclerview.layoutManager = LinearLayoutManager(context)


        showprogressbar()
        fetchData()

        return binding.root
    }

    private fun fetchData() {
        val apiService = RetrofitClient.apiService
        val sharedPreferences = requireContext().getSharedPreferences("ADMIN_PREF", Context.MODE_PRIVATE)
        val companyid = sharedPreferences.getString("company_id", null) ?: return


        val call = apiService.getData(companyid)

        call.enqueue(object : Callback<Map<String, DataModel>> {
            override fun onResponse(
                call: Call<Map<String, DataModel>>,
                response: Response<Map<String, DataModel>>
            ) {
                if (response.isSuccessful) {
                    val dataMap = response.body()
                    if (dataMap != null) {
                        val dataList = dataMap.values.toList()
                        // Sort the dataList based on the timestamp in descending order
                        val sortedDataList = dataList.sortedByDescending { it.time_stamp }

                        adapter = DataAdapter(sortedDataList)
                        binding.noticerecyclerview.adapter = adapter
//
//                        adapter = DataAdapter(dataList)
//                        binding.noticerecyclerview.adapter = adapter
                        Log.d("data",dataMap.toString())
                        dismissprogressbar()

                    }
                } else {
                    dismissprogressbar()
                    Log.d("data", "API request failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Map<String, DataModel>>, t: Throwable) {
                dismissprogressbar()
                Log.d("data", "API request failed", t)
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