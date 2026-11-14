package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.campoColors
import com.example.myapplication.ui.theme.PurpleLight
import com.example.myapplication.ui.theme.PurplePrimary
import com.example.myapplication.ui.viewmodel.AuthViewModel

@Composable
fun CadastroScreen(
    irLogin: () -> Unit,
    authViewModel: AuthViewModel,
    textoVoltar: String = "Já tem uma conta? Voltar para o Login"
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }

    LaunchedEffect(authViewModel.concluido) {
        if (authViewModel.concluido) {
            authViewModel.consumirResultado()
            irLogin()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Cadastro", style = MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = { if (it.length <= 100) nome = it },
            label = { Text("Nome") },
            singleLine = true,
            colors = campoColors(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { if (it.length <= 254) email = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            label = { Text("E-mail") },
            singleLine = true,
            colors = campoColors(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = { if (it.length <= 128) senha = it },
            label = { Text("Senha") },
            singleLine = true,
            visualTransformation = if (senhaVisivel)
                VisualTransformation.None
            else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (senhaVisivel)
                    Icons.Default.Visibility
                else Icons.Default.VisibilityOff
                val descricao = if (senhaVisivel) "Esconder Senha"
                else "Mostrar Senha"
                IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                    Icon(imageVector = image, contentDescription = descricao)
                }
            },
            colors = campoColors(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // Mostra o erro (ex.: e-mail já cadastrado, campos inválidos)
        authViewModel.mensagem?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                authViewModel.cadastrar(nome, email, senha)
                senha = ""
            },
            enabled = !authViewModel.carregando,
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary, contentColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cadastrar")
        }

        TextButton(onClick = irLogin, enabled = !authViewModel.carregando) {
            Text(textoVoltar, color = PurpleLight)
        }
    }
}
