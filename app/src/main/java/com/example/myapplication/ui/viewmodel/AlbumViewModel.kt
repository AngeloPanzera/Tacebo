package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.auth.SessaoUsuario
import com.example.myapplication.data.Album
import com.example.myapplication.data.dal.AlbumDAO
import com.example.myapplication.validation.AlbumValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ViewModel do CRUD de Álbuns (listar, cadastrar, alterar e excluir)
class AlbumViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AlbumDAO(application)

    val usuarioAtualId: Long? get() = SessaoUsuario.usuarioId

    var albuns by mutableStateOf<List<Album>>(emptyList())
        private set

    var titulo by mutableStateOf("")
        private set
    var artista by mutableStateOf("")
        private set
    // O ano é digitado como texto e só vira número na hora de salvar
    var ano by mutableStateOf("")
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
        albuns = lista
    }

    fun carregarParaEdicao(id: Long) = executar {
        val usuarioId = SessaoUsuario.exigirUsuario()
        val encontrado = withContext(Dispatchers.IO) { dao.buscarPorId(id, usuarioId) }
        val album = requireNotNull(encontrado) { "Álbum não encontrado" }
        titulo = album.titulo
        artista = album.artista
        ano = album.ano.toString()
        genero = album.genero
    }

    fun mudarTitulo(valor: String) {
        if (!carregando && valor.length <= 100) titulo = valor
    }

    fun mudarArtista(valor: String) {
        if (!carregando && valor.length <= 100) artista = valor
    }

    fun mudarAno(valor: String) {
        if (!carregando && valor.length <= 4 && valor.all { it.isDigit() }) ano = valor
    }

    fun mudarGenero(valor: String) {
        if (!carregando && valor.length <= 50) genero = valor
    }

    fun salvar(id: Long) = executar {
        val usuarioId = SessaoUsuario.exigirUsuario()
        val tituloLimpo = titulo.trim()
        val artistaLimpo = artista.trim()
        val generoLimpo = genero.trim()
        val anoNumero = ano.trim().toIntOrNull()

        AlbumValidator.validar(tituloLimpo, artistaLimpo, anoNumero, generoLimpo)
        val anoValido = requireNotNull(anoNumero) { "Informe o ano de lançamento" }

        withContext(Dispatchers.IO) {
            if (id > 0) {
                dao.alterar(id, usuarioId, tituloLimpo, artistaLimpo, anoValido, generoLimpo)
            } else {
                dao.cadastrar(usuarioId, tituloLimpo, artistaLimpo, anoValido, generoLimpo)
            }
        }
        salvo = true
    }

    fun excluir(id: Long) = executar {
        val usuarioId = SessaoUsuario.exigirUsuario()
        withContext(Dispatchers.IO) { dao.excluir(id, usuarioId) }
        albuns = albuns.filterNot { it.id == id }
        mensagem = "Álbum excluído."
    }

    fun consumirSalvo() {
        salvo = false
    }

    // Chamado depois que a mensagem já foi mostrada (Snackbar), para ela não repetir
    fun consumirMensagem() {
        mensagem = null
    }
}
