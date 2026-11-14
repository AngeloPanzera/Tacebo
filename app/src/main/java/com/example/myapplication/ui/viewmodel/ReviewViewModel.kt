package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.auth.SessaoUsuario
import com.example.myapplication.data.Review
import com.example.myapplication.data.dal.ReviewDAO
import com.example.myapplication.validation.ReviewValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReviewViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = ReviewDAO(application)

    val usuarioAtualId: Long? get() = SessaoUsuario.usuarioId

    var reviews by mutableStateOf<List<Review>>(emptyList())
        private set

    var musica by mutableStateOf("")
        private set
    var artista by mutableStateOf("")
        private set
    var nota by mutableStateOf(0)
        private set
    var texto by mutableStateOf("")
        private set

    var mensagem by mutableStateOf<String?>(null)
        private set
    var carregando by mutableStateOf(false)
        private set
    var salvo by mutableStateOf(false)
        private set

    private fun executar(operacao: suspend () -> Unit) {
        if (carregando || salvo) return
        carregando = true
        mensagem = null

        viewModelScope.launch {
            try {
                SessaoUsuario.exigirUsuario()
                operacao()
            } catch (erro: CancellationException) {
                throw erro
            } catch (erro: IllegalArgumentException) {
                mensagem = erro.message
            } catch (erro: Exception) {
                mensagem = "Ocorreu um erro inesperado!"
            } finally {
                carregando = false
            }
        }
    }

    fun listar() = executar {
        val usuarioId = SessaoUsuario.exigirUsuario()
        val lista = withContext(Dispatchers.IO) { dao.listar(usuarioId) }
        reviews = lista
    }

    fun carregarParaEdicao(id: Long) = executar {
        val usuarioId = SessaoUsuario.exigirUsuario()
        val encontrada = withContext(Dispatchers.IO) { dao.buscarPorId(id, usuarioId) }
        val review = requireNotNull(encontrada) { "Review não encontrada" }
        musica = review.musica
        artista = review.artista
        nota = review.nota
        texto = review.texto
    }

    // Usado quando a review nasce de uma música da busca (API): já vem com música e artista.
    // Só preenche se o formulário ainda estiver vazio (não apaga o que o usuário digitou).
    fun preencher(musicaInicial: String, artistaInicial: String) {
        if (musica.isEmpty() && artista.isEmpty()) {
            musica = musicaInicial.take(100)
            artista = artistaInicial.take(100)
        }
    }

    fun mudarMusica(valor: String) {
        if (!carregando && valor.length <= 100) musica = valor
    }

    fun mudarArtista(valor: String) {
        if (!carregando && valor.length <= 100) artista = valor
    }

    fun mudarNota(valor: Int) {
        if (!carregando) nota = valor
    }

    fun mudarTexto(valor: String) {
        if (!carregando && valor.length <= 500) texto = valor
    }

    fun salvar(id: Long) = executar {
        val usuarioId = SessaoUsuario.exigirUsuario()
        val musicaLimpa = musica.trim()
        val artistaLimpo = artista.trim()
        val textoLimpo = texto.trim()

        ReviewValidator.validar(musicaLimpa, artistaLimpo, nota, textoLimpo)
        withContext(Dispatchers.IO) {
            if (id > 0) {
                dao.alterar(id, usuarioId, musicaLimpa, artistaLimpo, nota, textoLimpo)
            } else {
                dao.cadastrar(usuarioId, musicaLimpa, artistaLimpo, nota, textoLimpo)
            }
        }
        salvo = true
    }

    fun excluir(id: Long) = executar {
        val usuarioId = SessaoUsuario.exigirUsuario()
        withContext(Dispatchers.IO) { dao.excluir(id, usuarioId) }
        reviews = reviews.filterNot { it.id == id }
        mensagem = "Review excluída."
    }

    fun consumirSalvo() {
        salvo = false
    }
}
