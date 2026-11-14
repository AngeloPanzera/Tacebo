package com.example.myapplication.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

// Interface do Retrofit: cada função vira uma chamada HTTP à API (iTunes Search, gratuita e sem chave).
// URL final de exemplo:
// https://itunes.apple.com/search?media=music&entity=song&country=BR&term=coldplay&limit=25
interface ItunesApi {

    @GET("search?media=music&entity=song&country=BR")
    suspend fun buscarMusicas(
        @Query("term") termo: String,
        @Query("limit") limite: Int
    ): ItunesResposta
}
