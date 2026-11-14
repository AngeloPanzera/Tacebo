package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.campoColors
import com.example.myapplication.ui.theme.PurplePrimary
import com.example.myapplication.ui.viewmodel.UsuarioViewModel

@Composable
fun UsuarioEditarScreen(
    usuarioId: Long,
    viewModel: UsuarioViewModel,
    voltar: () -> Unit
) {
    LaunchedEffect(usuarioId) { viewModel.buscarPorId(usuarioId) }
    LaunchedEffect(viewModel.salvo) {
        if (viewModel.salvo) {
            viewModel.consumirSalvo()
            voltar()
        }
    }
    var novaSenha by remember { mutableStateOf("") }

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
        Text("Alterar Usuário", style = MaterialTheme.typography.headlineMedium)
        viewModel.mensagem?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (viewModel.carregando) CircularProgressIndicator()

        val usuario = viewModel.usuario
        if (usuario != null) {
            OutlinedTextField(
                value = viewModel.nome,
                onValueChange = viewModel::mudarNome,
                label = { Text("Nome") },
                singleLine = true,
                enabled = !viewModel.carregando,
                colors = campoColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = usuario.email,
                onValueChange = {},
                readOnly = true,
                label = { Text("E-mail (não pode ser alterado)") },
                singleLine = true,
                colors = campoColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = novaSenha,
                onValueChange = { if (it.length <= 128) novaSenha = it },
                label = { Text("Senha") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    autoCorrectEnabled = false
                ),
                singleLine = true,
                enabled = !viewModel.carregando,
                colors = campoColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Text("Digite uma senha entre 6 a 128 caracteres")
            Button(
                onClick = {
                    viewModel.salvar(novaSenha)
                    novaSenha = ""
                },
                enabled = !viewModel.carregando && !viewModel.salvo,
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary, contentColor = Color.White)
            ) { Text("Salvar") }
        } else if (!viewModel.carregando) {
            TextButton(onClick = { viewModel.buscarPorId(usuarioId) }) {
                Text("Tentar carregar novamente.")
            }
        }

        TextButton(onClick = voltar, enabled = !viewModel.carregando) {
            Text("Voltar para usuários")
        }
    }
}
