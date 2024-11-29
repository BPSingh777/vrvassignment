package com.solutions.inwork.retrofitget

import com.solutions.inwork.Admin.dataclasses.CheckIn
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object RetrofitgetcheckIn {

    private const val BASE_URL = "https://s65nfrqfv6.execute-api.ap-south-1.amazonaws.com/"

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
        fun getData(@Query("employee_id") companyId: String): Call<Map<String, CheckIn>>
    }

}