package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.Avi
import com.example.myapplication.ui.theme.CardBg
import com.example.myapplication.ui.theme.GreenAccent
import com.example.myapplication.ui.theme.PurplePrimary
import com.example.myapplication.ui.theme.TextMuted
import com.example.myapplication.ui.viewmodel.UsuarioViewModel

@Composable
fun FriendsScreen(
    viewModel: UsuarioViewModel,
    irParaCadastro: () -> Unit,
    irParaEdicao: (Long) -> Unit
) {
    val atual = viewModel.usuarioAtualId
    if (atual == null) {
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
                Text("Amigos", style = MaterialTheme.typography.headlineMedium)
                Text("${viewModel.usuarios.size} usuários cadastrados", color = TextMuted)
                Button(
                    onClick = irParaCadastro,
                    enabled = !viewModel.carregando,
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary, contentColor = Color.White)
                ) {
                    Text("Novo usuário")
                }
                TextButton(onClick = viewModel::listar, enabled = !viewModel.carregando) {
                    Text("Atualizar lista")
                }
                viewModel.mensagem?.let { Text(it) }
                if (viewModel.carregando) CircularProgressIndicator()
                if (!viewModel.carregando && viewModel.usuarios.isEmpty())
                    Text("Nenhum usuário encontrado")
            }
        }

        items(viewModel.usuarios, key = { it.id }) { usuario ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avi(name = usuario.nome, size = 44, foto = usuario.foto)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(usuario.nome, style = MaterialTheme.typography.titleMedium)
                            Text(usuario.email, color = TextMuted)
                        }
                    }
                    if (usuario.id == atual) Text("Você está conectado.", color = GreenAccent)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { irParaEdicao(usuario.id) },
                            enabled = !viewModel.carregando
                        ) { Text("Alterar") }
                        TextButton(
                            onClick = { excluirId = usuario.id },
                            enabled = !viewModel.carregando && usuario.id != atual
                        ) { Text("Excluir") }
                    }
                }
            }
        }
    }

    val selecionado = viewModel.usuarios.firstOrNull { it.id == excluirId }
    if (selecionado != null && selecionado.id != atual) {
        AlertDialog(
            onDismissRequest = { excluirId = null },
            title = { Text("Excluir usuário?") },
            text = { Text("Excluir ${selecionado.nome}? Esta ação não pode ser desfeita") },
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
