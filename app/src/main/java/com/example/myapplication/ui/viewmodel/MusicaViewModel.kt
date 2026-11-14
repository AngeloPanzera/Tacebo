package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.auth.SessaoUsuario
import com.example.myapplication.data.Musica
import com.example.myapplication.data.dal.MusicaDAO
import com.example.myapplication.validation.MusicaValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ViewModel do CRUD de Músicas (listar, cadastrar, alterar e excluir)
class MusicaViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = MusicaDAO(application)

    val usuarioAtualId: Long? get() = SessaoUsuario.usuarioId

    var musicas by mutableStateOf<List<Musica>>(emptyList())
        private set

    var titulo by mutableStateOf("")
        private set
    var artista by mutableStateOf("")
        private set
    var album by mutableStateOf("")
        private set
    var genero by mutableStateOf("")
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
        musicas = lista
    }

    fun carregarParaEdicao(id: Long) = executar {
        val usuarioId = SessaoUsuario.exigirUsuario()
        val encontrada = withContext(Dispatchers.IO) { dao.buscarPorId(id, usuarioId) }
        val musica = requireNotNull(encontrada) { "Música não encontrada" }
        titulo = musica.titulo
        artista = musica.artista
        album = musica.album
        genero = musica.genero
    }

    fun mudarTitulo(valor: String) {
        if (!carregando && valor.length <= 100) titulo = valor
    }

    fun mudarArtista(valor: String) {
        if (!carregando && valor.length <= 100) artista = valor
    }

    fun mudarAlbum(valor: String) {
        if (!carregando && valor.length <= 100) album = valor
    }

    fun mudarGenero(valor: String) {
        if (!carregando && valor.length <= 50) genero = valor
    }

    fun salvar(id: Long) = executar {
        val usuarioId = SessaoUsuario.exigirUsuario()
        val tituloLimpo = titulo.trim()
        val artistaLimpo = artista.trim()
        val albumLimpo = album.trim()
        val generoLimpo = genero.trim()

        MusicaValidator.validar(tituloLimpo, artistaLimpo, albumLimpo, generoLimpo)
        withContext(Dispatchers.IO) {
            if (id > 0) {
                dao.alterar(id, usuarioId, tituloLimpo, artistaLimpo, albumLimpo, generoLimpo)
            } else {
                dao.cadastrar(usuarioId, tituloLimpo, artistaLimpo, albumLimpo, generoLimpo)
            }
        }
        salvo = true
    }

    fun excluir(id: Long) = executar {
        val usuarioId = SessaoUsuario.exigirUsuario()
        withContext(Dispatchers.IO) { dao.excluir(id, usuarioId) }
        musicas = musicas.filterNot { it.id == id }
        mensagem = "Música excluída."
    }

    fun consumirSalvo() {
        salvo = false
    }

    // Chamado depois que a mensagem já foi mostrada (Snackbar), para ela não repetir
    fun consumirMensagem() {
        mensagem = null
    }
}
