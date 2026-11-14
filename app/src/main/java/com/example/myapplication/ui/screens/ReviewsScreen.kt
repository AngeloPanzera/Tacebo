package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.Stars
import com.example.myapplication.ui.theme.CardBg
import com.example.myapplication.ui.theme.PurplePrimary
import com.example.myapplication.ui.theme.TextMuted
import com.example.myapplication.ui.theme.TextSecondary
import com.example.myapplication.ui.viewmodel.ReviewViewModel


@Composable
fun ReviewsScreen(
    viewModel: ReviewViewModel,
    irParaNova: () -> Unit,
    irParaEdicao: (Long) -> Unit
) {
    if (viewModel.usuarioAtualId == null) {
        Text("Faça login pra continuar")
        return
    }

    var excluirId by rememberSaveable { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Minhas reviews", style = MaterialTheme.typography.headlineMedium)
                Button(
                    onClick = irParaNova,
                    enabled = !viewModel.carregando,
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary, contentColor = Color.White)
                ) {
                    Text("Nova review")
                }
                TextButton(onClick = viewModel::listar, enabled = !viewModel.carregando) {
                    Text("Atualizar lista")
                }
                viewModel.mensagem?.let { Text(it) }
                if (viewModel.carregando) CircularProgressIndicator()
                if (!viewModel.carregando && viewModel.reviews.isEmpty())
                    Text("Você ainda não fez nenhuma review")
            }
        }

        items(viewModel.reviews, key = { it.id }) { review ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(review.musica, style = MaterialTheme.typography.titleMedium)
                    Text(review.artista, color = TextMuted)
                    Stars(value = review.nota, size = 16)
                    if (review.texto.isNotEmpty()) Text(review.texto, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { irParaEdicao(review.id) },
                            enabled = !viewModel.carregando
                        ) { Text("Alterar") }
                        TextButton(
                            onClick = { excluirId = review.id },
                            enabled = !viewModel.carregando
                        ) { Text("Excluir") }
                    }
                }
            }
        }
    }

    val selecionada = viewModel.reviews.firstOrNull { it.id == excluirId }
    if (selecionada != null) {
        AlertDialog(
            onDismissRequest = { excluirId = null },
            title = { Text("Excluir review?") },
            text = { Text("Excluir a review de ${selecionada.musica}? Esta ação não pode ser desfeita") },
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
