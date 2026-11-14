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
import com.example.myapplication.ui.components.StarsInput
import com.example.myapplication.ui.components.campoColors
import com.example.myapplication.ui.theme.PurplePrimary
import com.example.myapplication.ui.theme.TextMuted
import com.example.myapplication.ui.viewmodel.ReviewViewModel


@Composable
fun ReviewFormScreen(
    reviewId: Long,
    viewModel: ReviewViewModel,
    voltar: () -> Unit,
    // Preenchidos quando a review é criada a partir de uma música da busca (API)
    musicaInicial: String = "",
    artistaInicial: String = ""
) {

    LaunchedEffect(reviewId) {
        if (reviewId > 0) {
            viewModel.carregarParaEdicao(reviewId)
        } else {
            viewModel.preencher(musicaInicial, artistaInicial)
        }
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
            if (reviewId > 0) "Alterar review" else "Nova review",
            style = MaterialTheme.typography.headlineMedium
        )
        viewModel.mensagem?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (viewModel.carregando) CircularProgressIndicator()

        OutlinedTextField(
            value = viewModel.musica,
            onValueChange = viewModel::mudarMusica,
            label = { Text("Música") },
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

        Text("Nota", color = TextMuted)
        StarsInput(
            valor = viewModel.nota,
            onChange = viewModel::mudarNota,
            enabled = !viewModel.carregando
        )

        OutlinedTextField(
            value = viewModel.texto,
            onValueChange = viewModel::mudarTexto,
            label = { Text("Sua review (opcional)") },
            enabled = !viewModel.carregando,
            colors = campoColors(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.salvar(reviewId) },
            enabled = !viewModel.carregando && !viewModel.salvo,
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary, contentColor = Color.White)
        ) { Text("Salvar") }

        TextButton(onClick = voltar, enabled = !viewModel.carregando) {
            Text("Voltar para reviews")
        }
    }
}
