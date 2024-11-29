package com.solutions.inwork.retrofitget
import com.solutions.inwork.Admin.dataclasses.LeaveData
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


object RetrofitLeaves {
    private const val BASE_URL = "https://mgw11rbpe8.execute-api.ap-south-1.amazonaws.com/"


    interface ApiService {
        @GET("dev/resources")
        fun getData(
            @Query("company_id") companyId: String
            //   @Query("employee_id") employeeId: String
        ): Call<Map<String, LeaveData>>
    }

    fun create(): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}







