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
import com.example.myapplication.ui.viewmodel.AlbumViewModel

// CRUD de Álbuns - LISTAR (LazyColumn) e EXCLUIR (diálogo). Criar/alterar ficam em AlbumFormScreen.
@Composable
fun AlbumsScreen(
    viewModel: AlbumViewModel,
    irParaNovo: () -> Unit,
    irParaEdicao: (Long) -> Unit
) {
    if (viewModel.usuarioAtualId == null) {
        Text("Faça login para continuar.")
        return
    }

    var excluirId by rememberSaveable { mutableStateOf<Long?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostra no Snackbar a mensagem do ViewModel (ex.: "Álbum excluído.") e depois a limpa
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
                onClick = irParaNovo,
                containerColor = PurplePrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Novo álbum")
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
                    Text("Meus álbuns", style = MaterialTheme.typography.headlineMedium)
                    TextButton(onClick = viewModel::listar, enabled = !viewModel.carregando) {
                        Text("Atualizar lista")
                    }
                    if (viewModel.carregando) CircularProgressIndicator()
                    if (!viewModel.carregando && viewModel.albuns.isEmpty())
                        Text("Você ainda não cadastrou nenhum álbum. Toque no + para adicionar.")
                }
            }

            items(viewModel.albuns, key = { it.id }) { album ->
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
                            CoverAuto(texto = album.titulo, size = 56)
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(album.titulo, style = MaterialTheme.typography.titleMedium)
                                Text("${album.artista} · ${album.ano}", color = TextMuted)
                                if (album.genero.isNotEmpty()) Text(album.genero, color = TextSecondary)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = { irParaEdicao(album.id) },
                                enabled = !viewModel.carregando
                            ) { Text("Alterar") }
                            TextButton(
                                onClick = { excluirId = album.id },
                                enabled = !viewModel.carregando
                            ) { Text("Excluir") }
                        }
                    }
                }
            }
        }
    }

    val selecionado = viewModel.albuns.firstOrNull { it.id == excluirId }
    if (selecionado != null) {
        AlertDialog(
            onDismissRequest = { excluirId = null },
            title = { Text("Excluir álbum?") },
            text = { Text("Excluir o álbum ${selecionado.titulo}? Esta ação não pode ser desfeita") },
            confirmButton = {
                TextButton(
                    onClick = {
                        excluirId = null
                        viewModel.excluir(selecionado.id)
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
