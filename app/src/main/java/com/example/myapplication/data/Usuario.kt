package com.example.myapplication.data

data class Usuario(
    val id: Long,
    val nome: String,
    val email: String,
    // Caminho da foto de perfil salva no armazenamento do app (null = sem foto)
    val foto: String? = null
)
