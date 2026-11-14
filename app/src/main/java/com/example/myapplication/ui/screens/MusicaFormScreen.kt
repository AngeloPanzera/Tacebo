package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.campoColors
import com.example.myapplication.ui.theme.PurplePrimary
import com.example.myapplication.ui.viewmodel.MusicaViewModel

// CRUD de Músicas - CRIAR (musicaId = 0) e ALTERAR (musicaId > 0)
@Composable
fun MusicaFormScreen(
    musicaId: Long,
    viewModel: MusicaViewModel,
    voltar: () -> Unit
) {

    LaunchedEffect(musicaId) {
        if (musicaId > 0) viewModel.carregarParaEdicao(musicaId)
    }

    LaunchedEffect(viewModel.salvo) {
        if (viewModel.salvo) {
            viewModel.consumirSalvo()
            voltar()
        }
    }

    if (viewModel.usuarioAtualId == null) {
        Text("Faça login para continuar.")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            if (musicaId > 0) "Alterar música" else "Nova música",
            style = MaterialTheme.typography.headlineMedium
        )
        viewModel.mensagem?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (viewModel.carregando) CircularProgressIndicator()

        OutlinedTextField(
            value = viewModel.titulo,
            onValueChange = viewModel::mudarTitulo,
            label = { Text("Título da música") },
            singleLine = true,
            enabled = !viewModel.carregando,
            colors = campoColors(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = viewModel.artista,
            onValueChange = viewModel::mudarArtista,
            label = { Text("Artista") },
            singleLine = true,
            enabled = !viewModel.carregando,
            colors = campoColors(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = viewModel.album,
            onValueChange = viewModel::mudarAlbum,
            label = { Text("Álbum (opcional)") },
            singleLine = true,
            enabled = !viewModel.carregando,
            colors = campoColors(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = viewModel.genero,
            onValueChange = viewModel::mudarGenero,
            label = { Text("Gênero (opcional)") },
            singleLine = true,
            enabled = !viewModel.carregando,
            colors = campoColors(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.salvar(musicaId) },
            enabled = !viewModel.carregando && !viewModel.salvo,
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary, contentColor = Color.White)
        ) { Text("Salvar") }

        TextButton(onClick = voltar, enabled = !viewModel.carregando) {
            Text("Voltar para músicas")
        }
    }
}
