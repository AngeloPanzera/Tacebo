package com.example.myapplication.validation

object AlbumValidator {
    fun validar(titulo: String, artista: String, ano: Int?, genero: String) {
        require(titulo.length in 1..100) { "Informe o título do álbum (até 100 caracteres)" }
        require(artista.length in 1..100) { "Informe o artista (até 100 caracteres)" }
        require(ano != null && ano in 1900..2100) {
            "Informe o ano de lançamento (entre 1900 e 2100)"
        }
        require(genero.length <= 50) { "O gênero pode ter até 50 caracteres" }
    }
}
