package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.auth.SessaoUsuario
import com.example.myapplication.data.ResultadoBusca
import com.example.myapplication.data.dal.MusicaDAO
import com.example.myapplication.data.remote.RetrofitClient
import com.example.myapplication.data.remote.paraResultado
import com.example.myapplication.validation.MusicaValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

// ViewModel da tela de busca: consome a API com o Retrofit e salva músicas na biblioteca (SQLite)
class BuscaViewModel(application: Application) : AndroidViewModel(application) {
    private val musicaDao = MusicaDAO(application)

    private var buscaAtual: Job? = null
    private var ultimoTermo: String? = null

    var consulta by mutableStateOf("")
        private set
    var resultados by mutableStateOf<List<ResultadoBusca>>(emptyList())
        private set
    var carregando by mutableStateOf(false)
        private set
    // Vira true quando a API responde (diferencia "nenhum resultado" de "ainda não buscou")
    var buscou by mutableStateOf(false)
        private set
    var mensagem by mutableStateOf<String?>(null)
        private set

    fun mudarConsulta(valor: String) {
        if (valor.length <= 100) consulta = valor
    }

    fun buscar() {
        val termo = consulta.trim()
        if (termo == ultimoTermo) return

        buscaAtual?.cancel()
        ultimoTermo = termo

        if (termo.isEmpty()) {
            resultados = emptyList()
            buscou = false
            carregando = false
            return
        }

        buscaAtual = viewModelScope.launch {
            carregando = true
            try {
                // Chamada HTTP feita pelo Retrofit (suspend: não trava a tela)
                val resposta = RetrofitClient.itunesApi.buscarMusicas(termo, 25)
                resultados = resposta.results.orEmpty()
                    .mapNotNull { it.paraResultado() }
                    .distinctBy { it.id }
                buscou = true
            } catch (erro: CancellationException) {
                throw erro
            } catch (erro: IOException) {
                ultimoTermo = null
                mensagem = "Sem conexão com a internet. Tente novamente."
            } catch (erro: HttpException) {
                ultimoTermo = null
                mensagem = "A API respondeu com erro (${erro.code()}). Tente novamente."
            } catch (erro: Exception) {
                ultimoTermo = null
                mensagem = "Não foi possível buscar as músicas."
            } finally {
                // Se esta busca foi cancelada por uma mais nova, quem cuida do "carregando" é a nova
                if (isActive) carregando = false
            }
        }
    }

    // Guarda a música da API na tabela "musicas" (CRUD de Músicas)
    fun salvarNaBiblioteca(item: ResultadoBusca) {
        viewModelScope.launch {
            try {
                val usuarioId = SessaoUsuario.exigirUsuario()
                val titulo = item.titulo.take(100)
                val artista = item.artista.take(100)
                val album = item.album.take(100)
                val genero = item.genero.take(50)

                MusicaValidator.validar(titulo, artista, album, genero)
                val jaExistia = withContext(Dispatchers.IO) {
                    if (musicaDao.existe(usuarioId, titulo, artista)) {
                        true
                    } else {
                        musicaDao.cadastrar(usuarioId, titulo, artista, album, genero)
                        false
                    }
                }
                mensagem = if (jaExistia) {
                    "\"$titulo\" já está na sua biblioteca."
                } else {
                    "\"$titulo\" salva na sua biblioteca."
                }
            } catch (erro: CancellationException) {
                throw erro
            } catch (erro: IllegalArgumentException) {
                mensagem = erro.message
            } catch (erro: Exception) {
                mensagem = "Não foi possível salvar a música."
            }
        }
    }

    // Chamado depois que a mensagem já foi mostrada (Snackbar), para ela não repetir
    fun consumirMensagem() {
        mensagem = null
    }
}
