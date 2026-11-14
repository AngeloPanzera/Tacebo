package com.example.myapplication.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://itunes.apple.com/"

    // "by lazy": o Retrofit só é criado na primeira vez em que a API for usada.
    val itunesApi: ItunesApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // JSON -> data class
            .build()
            .create(ItunesApi::class.java)
    }
}
