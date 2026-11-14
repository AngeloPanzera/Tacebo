package com.example.myapplication.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Login

@Serializable
data object Cadastro

@Serializable
data object Buscar

@Serializable
data object Amigos

@Serializable
data object Perfil

@Serializable
data object Reviews

// musica/artista chegam preenchidos quando a review é criada a partir da busca (API)
@Serializable
data class ReviewForm(
    val id: Long = 0,
    val musica: String = "",
    val artista: String = ""
)

@Serializable
data class UsuarioEditar(val id: Long)

@Serializable
data object CadastrarUsuario

// CRUD de Álbuns
@Serializable
data object Albuns

@Serializable
data class AlbumForm(val id: Long = 0)

// CRUD de Músicas
@Serializable
data object Musicas

@Serializable
data class MusicaForm(val id: Long = 0)
