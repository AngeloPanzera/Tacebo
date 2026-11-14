package com.example.myapplication.validation

object MusicaValidator {
    fun validar(titulo: String, artista: String, album: String, genero: String) {
        require(titulo.length in 1..100) { "Informe o título da música (até 100 caracteres)" }
        require(artista.length in 1..100) { "Informe o artista (até 100 caracteres)" }
        require(album.length <= 100) { "O álbum pode ter até 100 caracteres" }
        require(genero.length <= 50) { "O gênero pode ter até 50 caracteres" }
    }
}
