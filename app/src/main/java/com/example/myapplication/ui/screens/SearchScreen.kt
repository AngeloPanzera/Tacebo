package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.ResultadoBusca
import com.example.myapplication.ui.components.CoverAuto
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodel.BuscaViewModel
import kotlinx.coroutines.delay

// "termo" é o que é enviado para a API quando o usuário toca no gênero
private data class Genre(
    val label: String,
    val termo: String,
    val g1: Color,
    val g2: Color,
    val emoji: String
)

private val GENRES = listOf(
    Genre("Eletrônico", "electronic", GradientPurpleDark1, GradientPurpleDark2, "⚡"),
    Genre("Indie", "indie", GradientGreenDark1, GradientGreenDark2, "🎸"),
    Genre("Ambient", "ambient", GradientCyan1, GradientCyan2, "🌊"),
    Genre("Synthwave", "synthwave", GradientViolet1, GradientViolet2, "🔮"),
    Genre("Experimental", "experimental", GradientPink1, GradientPink2, "🧪"),
    Genre("Lo-fi", "lofi", GradientBrown1, GradientBrown2, "☕")
)

// Tela de busca: consome a API (Retrofit) pelo BuscaViewModel.
// Usa Scaffold (Snackbar), LaunchedEffect (busca automática) e LazyColumn (resultados).
@Composable
fun SearchScreen(
    viewModel: BuscaViewModel,
    irParaReview: (musica: String, artista: String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Busca automática: espera o usuário parar de digitar (0,6 s) e só então chama a API.
    // Se ele digitar de novo antes disso, o LaunchedEffect é reiniciado e o delay recomeça.
    LaunchedEffect(viewModel.consulta) {
        delay(600)
        viewModel.buscar()
    }

    // Mostra no Snackbar os avisos do ViewModel (erro de rede, música salva...)
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Buscar",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = viewModel.consulta,
                        onValueChange = viewModel::mudarConsulta,
                        placeholder = { Text("Buscar músicas, artistas ou álbuns...", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { viewModel.buscar() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardBg,
                            unfocusedContainerColor = CardBg,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = SurfaceLight
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )

                    Spacer(Modifier.height(24.dp))
                }
            }

            if (viewModel.consulta.isBlank()) {
                // ── Tela inicial: gêneros (tocar em um deles faz a busca na API) ──
                item {
                    Column {
                        SectionTitle("Descubra novas músicas")
                        GenreGrid(
                            onGenreClick = { genero ->
                                viewModel.mudarConsulta(genero.termo)
                                viewModel.buscar()
                            }
                        )
                        Text(
                            text = "Os resultados vêm da API do iTunes.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                        )
                    }
                }
            } else if (viewModel.carregando && viewModel.resultados.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (viewModel.buscou && viewModel.resultados.isEmpty()) {
                item {
                    Text(
                        text = "Nenhum resultado para \"${viewModel.consulta.trim()}\"",
                        color = TextMuted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    )
                }
            } else if (viewModel.resultados.isNotEmpty()) {
                // ── Resultados vindos da API ──
                item {
                    Row(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MÚSICAS",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (viewModel.carregando) {
                            Spacer(Modifier.width(10.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = PurpleLight
                            )
                        }
                    }
                }

                items(viewModel.resultados, key = { it.id }) { musica ->
                    ResultadoRow(
                        musica = musica,
                        onAvaliar = { irParaReview(musica.titulo, musica.artista) },
                        onSalvar = { viewModel.salvarNaBiblioteca(musica) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = TextPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun GenreGrid(onGenreClick: (Genre) -> Unit) {
    val rows = GENRES.chunked(2)
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { genre ->
                    GenreCard(genre, onClick = { onGenreClick(genre) }, modifier = Modifier.weight(1f))
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun GenreCard(genre: Genre, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(genre.g1, genre.g2)))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(genre.emoji, fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Text(genre.label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

// Uma música devolvida pela API, com as ações: avaliar (cria review) e salvar (CRUD de músicas)
@Composable
private fun ResultadoRow(
    musica: ResultadoBusca,
    onAvaliar: () -> Unit,
    onSalvar: () -> Unit
) {
    val detalhes = listOf(musica.artista, musica.album)
        .filter { it.isNotEmpty() }
        .joinToString(" · ")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CoverAuto(texto = musica.titulo, size = 44)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = musica.titulo,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = detalhes,
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row {
            TextButton(onClick = onAvaliar) { Text("Avaliar") }
            TextButton(onClick = onSalvar) { Text("Salvar na biblioteca") }
        }
    }
}
