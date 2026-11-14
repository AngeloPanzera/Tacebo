package com.example.myapplication.validation

object ReviewValidator {
    fun validar(musica: String, artista: String, nota: Int, texto: String) {
        require(musica.length in 1..100) { "Informe o nome da música (até 100 caracteres)" }
        require(artista.length in 1..100) { "Informe o artista (até 100 caracteres)" }
        require(nota in 1..5) { "Escolha uma nota de 1 a 5 estrelas" }
        require(texto.length <= 500) { "A review pode ter até 500 caracteres" }
    }
}
