package com.example.myapplication.data.remote

import com.example.myapplication.data.ResultadoBusca

// Classes que representam o JSON devolvido pela API.
// O Gson preenche cada campo pelo nome (resultCount, results, trackName...).
data class ItunesResposta(
    val resultCount: Int = 0,
    val results: List<ItunesMusica>? = null
)

data class ItunesMusica(
    val trackId: Long? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val collectionName: String? = null,
    val primaryGenreName: String? = null
)

// Converte o objeto vindo da API no model usado pelas telas (ignora itens incompletos).
fun ItunesMusica.paraResultado(): ResultadoBusca? {
    val id = trackId ?: return null
    val titulo = trackName?.trim().orEmpty()
    val artista = artistName?.trim().orEmpty()
    if (titulo.isEmpty() || artista.isEmpty()) return null
    return ResultadoBusca(
        id = id,
        titulo = titulo,
        artista = artista,
        album = collectionName?.trim().orEmpty(),
        genero = primaryGenreName?.trim().orEmpty()
    )
}
