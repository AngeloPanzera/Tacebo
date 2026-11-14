package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.CoverAuto
import com.example.myapplication.ui.theme.BgDark
import com.example.myapplication.ui.theme.CardBg
import com.example.myapplication.ui.theme.PurplePrimary
import com.example.myapplication.ui.theme.TextMuted
import com.example.myapplication.ui.theme.TextSecondary
import com.example.myapplication.ui.viewmodel.MusicaViewModel

// CRUD de Músicas - LISTAR (LazyColumn) e EXCLUIR (diálogo). Criar/alterar ficam em MusicaFormScreen.
@Composable
fun MusicasScreen(
    viewModel: MusicaViewModel,
    irParaNova: () -> Unit,
    irParaEdicao: (Long) -> Unit
) {
    if (viewModel.usuarioAtualId == null) {
        Text("Faça login para continuar.")
        return
    }

    var excluirId by rememberSaveable { mutableStateOf<Long?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostra no Snackbar a mensagem do ViewModel (ex.: "Música excluída.") e depois a limpa
    LaunchedEffect(viewModel.mensagem) {
        val texto = viewModel.mensagem
        if (texto != null) {
            snackbarHostState.showSnackbar(texto)
            viewModel.consumirMensagem()
        }
    }

    Scaffold(
        containerColor = BgDark,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = irParaNova,
                containerColor = PurplePrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nova música")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Minhas músicas", style = MaterialTheme.typography.headlineMedium)
                    TextButton(onClick = viewModel::listar, enabled = !viewModel.carregando) {
                        Text("Atualizar lista")
                    }
                    if (viewModel.carregando) CircularProgressIndicator()
                    if (!viewModel.carregando && viewModel.musicas.isEmpty())
                        Text("Você ainda não salvou nenhuma música. Toque no + ou salve uma na busca.")
                }
            }

            items(viewModel.musicas, key = { it.id }) { musica ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBg)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CoverAuto(texto = musica.titulo, size = 48)
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(musica.titulo, style = MaterialTheme.typography.titleMedium)
                                Text(musica.artista, color = TextMuted)
                                val detalhes = listOf(musica.album, musica.genero)
                                    .filter { it.isNotEmpty() }
                                    .joinToString(" · ")
                                if (detalhes.isNotEmpty()) Text(detalhes, color = TextSecondary)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = { irParaEdicao(musica.id) },
                                enabled = !viewModel.carregando
                            ) { Text("Alterar") }
                            TextButton(
                                onClick = { excluirId = musica.id },
                                enabled = !viewModel.carregando
                            ) { Text("Excluir") }
                        }
                    }
                }
            }
        }
    }

    val selecionada = viewModel.musicas.firstOrNull { it.id == excluirId }
    if (selecionada != null) {
        AlertDialog(
            onDismissRequest = { excluirId = null },
            title = { Text("Excluir música?") },
            text = { Text("Excluir a música ${selecionada.titulo}? Esta ação não pode ser desfeita") },
            confirmButton = {
                TextButton(
                    onClick = {
                        excluirId = null
                        viewModel.excluir(selecionada.id)
                    },
                    enabled = !viewModel.carregando
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { excluirId = null }) { Text("Cancelar") }
            }
        )
    }
}
