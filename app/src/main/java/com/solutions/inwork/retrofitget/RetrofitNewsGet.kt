package com.solutions.inwork.retrofitget

import com.solutions.inwork.client.dataclasses.NewsResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object RetrofitNewsGet {

    private const val BASE_URL = "https://newsapi.org/v2/"

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
        @GET("top-headlines")
        fun getData(@Query("country") country: String, @Query("apiKey") apiKey: String): Call<NewsResponse>
    }

}