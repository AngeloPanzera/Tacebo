package com.example.myapplication.ui.screens

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Album
import com.example.myapplication.data.Musica
import com.example.myapplication.data.Review
import com.example.myapplication.ui.components.Avi
import com.example.myapplication.ui.components.Cover
import com.example.myapplication.ui.components.CoverAuto
import com.example.myapplication.ui.components.Stars
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodel.AlbumViewModel
import com.example.myapplication.ui.viewmodel.MusicaViewModel
import com.example.myapplication.ui.viewmodel.ReviewViewModel
import com.example.myapplication.ui.viewmodel.UsuarioViewModel
import kotlinx.coroutines.launch

private enum class ProfileTab(val label: String) {
    OVERVIEW("Visão Geral"), ALBUMS("Álbuns"), SONGS("Músicas"), REVIEWS("Reviews")
}

// Perfil do usuário logado. Usa Scaffold (Snackbar) e dois recursos nativos do Android:
// galeria de imagens e câmera, para trocar a foto de perfil.
@Composable
fun ProfileScreen(
    viewModel: UsuarioViewModel,
    reviewViewModel: ReviewViewModel,
    albumViewModel: AlbumViewModel,
    musicaViewModel: MusicaViewModel,
    irParaEdicao: (Long) -> Unit,
    irParaAlbuns: () -> Unit,
    irParaMusicas: () -> Unit,
    sair: () -> Unit
) {
    var tab by remember { mutableStateOf(ProfileTab.OVERVIEW) }
    val snackbarHostState = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()

    val usuario = viewModel.usuario
    val reviews = reviewViewModel.reviews
    val albuns = albumViewModel.albuns
    val musicas = musicaViewModel.musicas
    val media = if (reviews.isEmpty()) "-"
    else String.format("%.1f", reviews.map { it.nota }.average())

    // ── Recurso nativo 1: biblioteca de imagens (galeria) ──
    // O seletor do sistema devolve a Uri da imagem escolhida (null se o usuário cancelar)
    val galeria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.fotoDaGaleria(uri)
    }

    // ── Recurso nativo 2: câmera ──
    // Abre o app de câmera e devolve a foto tirada como Bitmap (null se cancelar)
    val camera = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { foto ->
        if (foto != null) viewModel.fotoDaCamera(foto)
    }

    // Mostra no Snackbar as mensagens do ViewModel (ex.: "Foto de perfil atualizada.")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Avi(name = usuario?.nome ?: "?", size = 80, foto = usuario?.foto)
                Spacer(Modifier.height(10.dp))
                Text(
                    usuario?.nome ?: "Carregando...",
                    color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
                Text(usuario?.email ?: "", color = PurplePrimary, fontSize = 13.sp)
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.usuarioAtualId?.let { id -> irParaEdicao(id) }
                        },
                        enabled = usuario != null,
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Editar perfil") }
                    TextButton(onClick = sair) { Text("Sair", color = TextMuted) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            try {
                                camera.launch(null)
                            } catch (erro: ActivityNotFoundException) {
                                escopo.launch {
                                    snackbarHostState.showSnackbar("Nenhum aplicativo de câmera encontrado.")
                                }
                            }
                        },
                        enabled = usuario != null,
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Tirar foto") }
                    OutlinedButton(
                        onClick = {
                            galeria.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = usuario != null,
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Escolher da galeria") }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(reviews.size.toString(), "Reviews")
                StatItem(albuns.size.toString(), "Álbuns")
                StatItem(musicas.size.toString(), "Músicas")
                StatItem(media, "Nota média")
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg)
                    .padding(4.dp)
            ) {
                ProfileTab.values().forEach { t ->
                    val selected = tab == t
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) PurplePrimary else CardBg)
                            .clickable { tab = t }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            t.label,
                            color = if (selected) Color.White else TextFaint,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            when (tab) {
                ProfileTab.OVERVIEW -> OverviewTab(reviews, musicas)
                ProfileTab.ALBUMS -> AlbumsTab(albuns, irParaAlbuns)
                ProfileTab.SONGS -> SongsTab(musicas, irParaMusicas)
                ProfileTab.REVIEWS -> ReviewsTab(reviews)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextFaint, fontSize = 10.sp)
    }
}

@Composable
private fun OverviewTab(reviews: List<Review>, musicas: List<Musica>) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        Text("MINHAS MÚSICAS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        if (musicas.isEmpty()) {
            Text("Nenhuma música salva ainda", color = TextMuted, fontSize = 13.sp)
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                musicas.take(8).forEach { musica ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(76.dp)) {
                        CoverAuto(texto = musica.titulo, size = 76, radius = 12)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            musica.titulo,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text("ÚLTIMAS REVIEWS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        if (reviews.isEmpty()) {
            Text("Nenhuma review ainda", color = TextMuted, fontSize = 13.sp)
        } else {
            reviews.take(2).forEach { review -> ReviewItem(review) }
        }
    }
}

@Composable
private fun AlbumsTab(albuns: List<Album>, irParaAlbuns: () -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        if (albuns.isEmpty()) {
            Text("Nenhum álbum cadastrado ainda", color = TextMuted, fontSize = 13.sp)
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                albuns.take(20).forEach { album ->
                    Column(modifier = Modifier.width(90.dp)) {
                        CoverAuto(texto = album.titulo, size = 90, radius = 12)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            album.titulo,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            album.artista,
                            color = TextFaint,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        // Leva para a tela do CRUD de álbuns (listar, cadastrar, alterar e excluir)
        OutlinedButton(onClick = irParaAlbuns, shape = RoundedCornerShape(12.dp)) {
            Text("Gerenciar álbuns")
        }
    }
}

@Composable
private fun SongsTab(musicas: List<Musica>, irParaMusicas: () -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        if (musicas.isEmpty()) {
            Text("Nenhuma música salva ainda", color = TextMuted, fontSize = 13.sp)
        } else {
            val visiveis = musicas.take(20)
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg)
            ) {
                visiveis.forEachIndexed { i, musica ->
                    val detalhes = listOf(musica.artista, musica.album)
                        .filter { it.isNotEmpty() }
                        .joinToString(" · ")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${i + 1}", color = TextFaint, fontSize = 12.sp, modifier = Modifier.width(20.dp))
                        CoverAuto(texto = musica.titulo, size = 44, radius = 10)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                musica.titulo,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                detalhes,
                                color = TextMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            if (musicas.size > visiveis.size) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "+ ${musicas.size - visiveis.size} músicas na sua biblioteca",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        // Leva para a tela do CRUD de músicas (listar, cadastrar, alterar e excluir)
        OutlinedButton(onClick = irParaMusicas, shape = RoundedCornerShape(12.dp)) {
            Text("Gerenciar músicas")
        }
    }
}

@Composable
private fun ReviewsTab(reviews: List<Review>) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        if (reviews.isEmpty()) {
            Text("Nenhuma review ainda", color = TextMuted, fontSize = 13.sp)
        } else {
            reviews.forEach { review -> ReviewItem(review) }
        }
    }
}

@Composable
private fun ReviewItem(review: Review) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .padding(14.dp)
    ) {
        Cover(g1 = GradientPurpleDark1, g2 = GradientPurpleDark2, size = 52, radius = 12)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(review.musica, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(review.artista, color = TextMuted, fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            Stars(value = review.nota, size = 13)
            if (review.texto.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(review.texto, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}
