package com.solutions.inwork.client.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.solutions.inwork.client.adapter.NotificationAdapter
import com.solutions.inwork.client.dataclasses.ClientNotification
import com.solutions.inwork.databinding.FragmentNotificationClientBinding
import com.solutions.inwork.retrofitget.RetrofitGetClientNotifications
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationClientFragment : Fragment() {


    private lateinit var binding : FragmentNotificationClientBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationClientBinding.inflate(inflater,container,false)


        binding.ClientNotificationRecyclerview.layoutManager = LinearLayoutManager(context)

        binding.pgbar.visibility = View.VISIBLE


        fetchNotifications()
        return binding.root
    }

    fun fetchNotifications(){

        val sharedPreferences = requireContext().getSharedPreferences("CLIENT_DETAILS", Context.MODE_PRIVATE)
        val employeeID = sharedPreferences.getString("employee_id", null) ?: return
        val companyID = sharedPreferences.getString("company_id", null) ?: return

        val apiService = RetrofitGetClientNotifications.apiService
        val call = apiService.getData(employeeID.toString(), companyID.toString())

        call.enqueue(object : Callback<Map<String,ClientNotification>>{
            override fun onResponse(
                call: Call<Map<String, ClientNotification>>,
                response: Response<Map<String, ClientNotification>>
            ) {
                if(response.isSuccessful){

                    binding.pgbar.visibility = View.GONE
                    val notificationMap = response.body()
                    if (notificationMap!=null){
                        val notificationList = notificationMap.values.toList()
                        val sortedList = notificationList.sortedByDescending { it.notification_date }

                       val adapter = NotificationAdapter(sortedList)
                        binding.ClientNotificationRecyclerview.adapter = adapter
                    }
                }
                else{
                    binding.pgbar.visibility = View.GONE
                    Toast.makeText(requireContext(),"No notifications",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, ClientNotification>>, t: Throwable) {
               Log.d("Notificationfailure","$t")
                binding.pgbar.visibility = View.GONE
            }

        })



    }
}