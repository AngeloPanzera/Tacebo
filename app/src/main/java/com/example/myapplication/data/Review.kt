package com.example.myapplication.data

data class Review(
    val id: Long,
    val usuarioId: Long,
    val musica: String,
    val artista: String,
    val nota: Int,
    val texto: String
)
