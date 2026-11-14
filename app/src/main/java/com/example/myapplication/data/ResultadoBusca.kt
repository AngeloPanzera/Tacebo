package com.example.myapplication.data

// Música devolvida pela API (já convertida do JSON do Retrofit) e exibida na tela de busca
data class ResultadoBusca(
    val id: Long,
    val titulo: String,
    val artista: String,
    val album: String,
    val genero: String
)
