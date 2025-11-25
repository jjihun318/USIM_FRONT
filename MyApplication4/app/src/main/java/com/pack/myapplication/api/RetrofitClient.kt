/*package com.pack.myapplication.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Direction5 전용 Base URL
    private const val DIRECTION_BASE_URL = "https://naveropenapi.apigw.ntruss.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val direction5Service: Direction5Service by lazy {
        Retrofit.Builder()
            .baseUrl(DIRECTION_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Direction5Service::class.java)
    }
}*/