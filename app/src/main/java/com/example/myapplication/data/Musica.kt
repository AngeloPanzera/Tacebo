package com.example.myapplication.data

// Model do CRUD de Músicas (tabela "musicas")
data class Musica(
    val id: Long,
    val usuarioId: Long,
    val titulo: String,
    val artista: String,
    val album: String,
    val genero: String
)
