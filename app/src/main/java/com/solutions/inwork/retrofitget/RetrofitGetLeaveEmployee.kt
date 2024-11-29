package com.solutions.inwork.retrofitget

import com.solutions.inwork.Admin.dataclasses.LeaveData
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object RetrofitGetLeaveEmployee {

    private const val BASE_URL = "https://55h10yporc.execute-api.ap-south-1.amazonaws.com/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    interface ApiService {
        @GET("test/resources")
        fun getData(
            @Query("company_id") companyId : String,
            @Query("employee_id") employeeId: String): Call<Map<String, LeaveData>>
    }

}