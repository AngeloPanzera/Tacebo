package com.example.myapplication.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.auth.AuthManager
import com.example.myapplication.auth.SessaoUsuario
import com.example.myapplication.data.Usuario
import com.example.myapplication.data.dal.UsuarioDAO
import com.example.myapplication.util.ImagemUtil
import com.example.myapplication.validation.CadastroValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsuarioViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = UsuarioDAO(application)

    val usuarioAtualId: Long? get() = SessaoUsuario.usuarioId

    var usuarios by mutableStateOf<List<Usuario>>(emptyList())
        private set
    var usuario by mutableStateOf<Usuario?>(null)
        private set
    var nome by mutableStateOf("")
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
        val lista = withContext(Dispatchers.IO) { dao.listar() }
        usuarios = lista
    }

    fun carregarAtual() = executar {
        val id = SessaoUsuario.exigirUsuario()
        val encontrado = withContext(Dispatchers.IO) { dao.buscarPorId(id) }
        val atual = requireNotNull(encontrado) { "Usuário não encontrado" }
        usuario = atual
        nome = atual.nome
    }

    fun buscarPorId(id: Long) {
        if (usuario?.id == id) return
        executar {
            val encontrado = withContext(Dispatchers.IO) { dao.buscarPorId(id) }
            usuario = requireNotNull(encontrado) { "Usuário não encontrado" }
            nome = encontrado.nome
        }
    }

    fun mudarNome(valor: String) {
        if (!carregando && valor.length <= 100) nome = valor
    }

    fun salvar(novaSenha: String) = executar {
        val selecionado = requireNotNull(usuario) { "Selecione um usuário" }
        val nomeLimpo = nome.trim()
        CadastroValidator.validar(nomeLimpo, novaSenha)
        withContext(Dispatchers.IO) {
            if (novaSenha.isEmpty()) {
                dao.alterar(selecionado.id, nomeLimpo)
            } else {
                val salt = AuthManager.criarSalt()
                val hash = AuthManager.gerarHash(novaSenha.trim(), salt)
                try {
                    dao.alterar(selecionado.id, nomeLimpo, hash, salt)
                } finally {
                    hash.fill(0)
                }
            }
        }
        salvo = true
    }

    fun excluir(id: Long) = executar {
        val atual = SessaoUsuario.exigirUsuario()
        require(id != atual) { "Você não pode excluir o seu usuário" }
        val foto = usuarios.firstOrNull { it.id == id }?.foto
        withContext(Dispatchers.IO) {
            dao.excluir(id, atual)
            if (foto != null) ImagemUtil.apagar(foto)
        }
        usuarios = usuarios.filterNot { it.id == id }
        mensagem = "Usuário excluído."
    }

    fun consumirSalvo() {
        salvo = false
    }

    // Chamado depois que a mensagem já foi mostrada (Snackbar), para ela não repetir
    fun consumirMensagem() {
        mensagem = null
    }

    // ---------- Recurso nativo: foto de perfil (galeria e câmera) ----------
    private var salvandoFoto = false

    // Foto escolhida na galeria de imagens do aparelho
    fun fotoDaGaleria(uri: Uri) = atualizarFoto { id ->
        ImagemUtil.salvarFotoDaGaleria(getApplication<Application>(), uri, id)
    }

    // Foto tirada agora com a câmera
    fun fotoDaCamera(bitmap: Bitmap) = atualizarFoto { id ->
        ImagemUtil.salvarBitmap(getApplication<Application>(), bitmap, id)
    }

    private fun atualizarFoto(salvarArquivo: (usuarioId: Long) -> String) {
        if (salvandoFoto) return
        salvandoFoto = true

        viewModelScope.launch {
            try {
                val id = SessaoUsuario.exigirUsuario()
                val atualizado = withContext(Dispatchers.IO) {
                    val anterior = dao.buscarPorId(id)?.foto
                    val novoCaminho = salvarArquivo(id)
                    dao.alterarFoto(id, novoCaminho)
                    if (anterior != null) ImagemUtil.apagar(anterior)
                    dao.buscarPorId(id)
                }
                usuario = requireNotNull(atualizado) { "Usuário não encontrado" }
                mensagem = "Foto de perfil atualizada."
            } catch (erro: CancellationException) {
                throw erro
            } catch (erro: IllegalArgumentException) {
                mensagem = erro.message
            } catch (erro: Exception) {
                mensagem = "Não foi possível salvar a foto."
            } finally {
                salvandoFoto = false
            }
        }
    }
}
