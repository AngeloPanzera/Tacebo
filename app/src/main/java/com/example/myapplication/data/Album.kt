package com.example.myapplication.data

// Model do CRUD de Álbuns (tabela "albuns")
data class Album(
    val id: Long,
    val usuarioId: Long,
    val titulo: String,
    val artista: String,
    val ano: Int,
    val genero: String
)
