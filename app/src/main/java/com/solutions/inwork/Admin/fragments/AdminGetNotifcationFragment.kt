package com.solutions.inwork.Admin.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.solutions.inwork.Admin.dataclasses.AdminGetNotifications
import com.solutions.inwork.client.adapter.NotificationAdapter
import com.solutions.inwork.databinding.FragmentAdminGetNotifcationBinding
import com.solutions.inwork.retrofitget.RetrofitAdminGetNotifications
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminGetNotifcationFragment : Fragment() {

    private var _binding: FragmentAdminGetNotifcationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminGetNotifcationBinding.inflate(inflater, container, false)
        return binding.root



        
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        getNotifications()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks by setting the binding to null
    }

    private fun setupViews() {
        binding.AdmingetNotificationRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.pgbar.visibility = View.VISIBLE
    }

    private fun getNotifications() {
        val apiService = RetrofitAdminGetNotifications.apiService
        val sharedPreferences = requireContext().getSharedPreferences("ADMIN_PREF", Context.MODE_PRIVATE)
        val companyid = sharedPreferences.getString("company_id", null) ?: return
        val call = apiService.getData(companyid)

        call.enqueue(object : Callback<Map<String, AdminGetNotifications>> {
            override fun onResponse(
                call: Call<Map<String, AdminGetNotifications>>,
                response: Response<Map<String, AdminGetNotifications>>
            ) {
                if (response.isSuccessful) {
                    val notificationMap = response.body()
                    if (notificationMap != null) {
                        val notificationList = notificationMap.values.toList()

                        // Sort the notification list based on arrival time in descending order
                        val sortedList = notificationList.sortedByDescending { it.notification_date }

                        val adapter = NotificationAdapter(sortedList)
                        binding.AdmingetNotificationRecyclerView.adapter = adapter
                        binding.pgbar.visibility = View.GONE
                        Log.d("AdminNotification", "Successful")
                    }
                } else {
                    Log.d("AdminNotification", "Something went wrong")
                    binding.pgbar.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<Map<String, AdminGetNotifications>>, t: Throwable) {
                Log.d("AdminNotification", t.toString())
                binding.pgbar.visibility = View.GONE
            }
        })
    }
}
