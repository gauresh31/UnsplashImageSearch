package com.kt.unsplashimagesearch.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object APIAdapter {
    val apiClient: APIClient = Retrofit.Builder()
        .baseUrl("https://api.unsplash.com")
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(APIClient::class.java)
}