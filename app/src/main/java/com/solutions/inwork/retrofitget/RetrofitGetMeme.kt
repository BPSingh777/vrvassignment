package com.solutions.inwork.retrofitget

import com.solutions.inwork.client.dataclasses.MemeData
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object RetrofitGetMeme {

    private const val BASE_URL = "https://meme.breakingbranches.tech/"

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
        @GET("api")
        fun getData(@Query("limit") limit: Int, @Query("type") type: String): Call<MemeData>
    }
}