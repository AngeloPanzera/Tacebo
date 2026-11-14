package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.example.myapplication.auth.SessaoUsuario
import com.example.myapplication.ui.screens.AlbumFormScreen
import com.example.myapplication.ui.screens.AlbumsScreen
import com.example.myapplication.ui.screens.CadastroScreen
import com.example.myapplication.ui.screens.FriendsScreen
import com.example.myapplication.ui.screens.LoginScreen
import com.example.myapplication.ui.screens.MusicaFormScreen
import com.example.myapplication.ui.screens.MusicasScreen
import com.example.myapplication.ui.screens.ProfileScreen
import com.example.myapplication.ui.screens.ReviewFormScreen
import com.example.myapplication.ui.screens.ReviewsScreen
import com.example.myapplication.ui.screens.SearchScreen
import com.example.myapplication.ui.screens.UsuarioEditarScreen
import com.example.myapplication.ui.viewmodel.AlbumViewModel
import com.example.myapplication.ui.viewmodel.BuscaViewModel
import com.example.myapplication.ui.viewmodel.MusicaViewModel
import com.example.myapplication.ui.viewmodel.ReviewViewModel
import com.example.myapplication.ui.viewmodel.UsuarioViewModel

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier) {
    val entradaAtual by navController.currentBackStackEntryAsState()
    val destino = entradaAtual?.destination
    val usuarioAtual = SessaoUsuario.usuarioId

    LaunchedEffect(destino, usuarioAtual) {
        val publica = destino?.hasRoute<Login>() == true ||
                destino?.hasRoute<Cadastro>() == true
        if (destino != null && !publica && usuarioAtual == null) {
            navController.navigate(Login) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Login,
        modifier = modifier
    ) {
        composable<Login> {
            LoginScreen(
                irLogin = {
                    navController.navigate(Buscar) {
                        popUpTo(Login) { inclusive = true }
                    }
                },
                irCadastro = { navController.navigate(Cadastro) },
                authViewModel = viewModel()
            )
        }

        composable<Cadastro> {
            CadastroScreen(
                irLogin = { navController.popBackStack() },
                authViewModel = viewModel()
            )
        }

        composable<Buscar> {
            val buscaViewModel: BuscaViewModel = viewModel()
            SearchScreen(
                viewModel = buscaViewModel,
                irParaReview = { musica, artista ->
                    navController.navigate(ReviewForm(musica = musica, artista = artista))
                }
            )
        }

        composable<Amigos> { entrada ->
            val usuarioViewModel: UsuarioViewModel = viewModel()
            AoRetomar(entrada) { usuarioViewModel.listar() }
            FriendsScreen(
                viewModel = usuarioViewModel,
                irParaCadastro = { navController.navigate(CadastrarUsuario) },
                irParaEdicao = { navController.navigate(UsuarioEditar(it)) }
            )
        }

        composable<CadastrarUsuario> {
            CadastroScreen(
                irLogin = { navController.popBackStack() },
                authViewModel = viewModel(),
                textoVoltar = "Voltar para a lista"
            )
        }

        composable<UsuarioEditar> { entrada ->
            val rota = entrada.toRoute<UsuarioEditar>()
            UsuarioEditarScreen(
                usuarioId = rota.id,
                viewModel = viewModel(),
                voltar = { navController.popBackStack() }
            )
        }

        composable<Reviews> { entrada ->
            val reviewViewModel: ReviewViewModel = viewModel()
            AoRetomar(entrada) { reviewViewModel.listar() }
            ReviewsScreen(
                viewModel = reviewViewModel,
                irParaNova = { navController.navigate(ReviewForm()) },
                irParaEdicao = { navController.navigate(ReviewForm(it)) }
            )
        }

        composable<ReviewForm> { entrada ->
            val rota = entrada.toRoute<ReviewForm>()
            ReviewFormScreen(
                reviewId = rota.id,
                viewModel = viewModel(),
                voltar = { navController.popBackStack() },
                musicaInicial = rota.musica,
                artistaInicial = rota.artista
            )
        }

        // ── CRUD de Álbuns ──
        composable<Albuns> { entrada ->
            val albumViewModel: AlbumViewModel = viewModel()
            AoRetomar(entrada) { albumViewModel.listar() }
            AlbumsScreen(
                viewModel = albumViewModel,
                irParaNovo = { navController.navigate(AlbumForm()) },
                irParaEdicao = { navController.navigate(AlbumForm(it)) }
            )
        }

        composable<AlbumForm> { entrada ->
            val rota = entrada.toRoute<AlbumForm>()
            AlbumFormScreen(
                albumId = rota.id,
                viewModel = viewModel(),
                voltar = { navController.popBackStack() }
            )
        }

        // ── CRUD de Músicas ──
        composable<Musicas> { entrada ->
            val musicaViewModel: MusicaViewModel = viewModel()
            AoRetomar(entrada) { musicaViewModel.listar() }
            MusicasScreen(
                viewModel = musicaViewModel,
                irParaNova = { navController.navigate(MusicaForm()) },
                irParaEdicao = { navController.navigate(MusicaForm(it)) }
            )
        }

        composable<MusicaForm> { entrada ->
            val rota = entrada.toRoute<MusicaForm>()
            MusicaFormScreen(
                musicaId = rota.id,
                viewModel = viewModel(),
                voltar = { navController.popBackStack() }
            )
        }

        composable<Perfil> { entrada ->
            val usuarioViewModel: UsuarioViewModel = viewModel()
            val reviewViewModel: ReviewViewModel = viewModel()
            val albumViewModel: AlbumViewModel = viewModel()
            val musicaViewModel: MusicaViewModel = viewModel()
            AoRetomar(entrada) {
                usuarioViewModel.carregarAtual()
                reviewViewModel.listar()
                albumViewModel.listar()
                musicaViewModel.listar()
            }
            ProfileScreen(
                viewModel = usuarioViewModel,
                reviewViewModel = reviewViewModel,
                albumViewModel = albumViewModel,
                musicaViewModel = musicaViewModel,
                irParaEdicao = { navController.navigate(UsuarioEditar(it)) },
                irParaAlbuns = { navController.navigate(Albuns) },
                irParaMusicas = { navController.navigate(Musicas) },
                sair = {
                    SessaoUsuario.encerrar()
                    navController.navigate(Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun AoRetomar(entrada: NavBackStackEntry, acao: () -> Unit) {
    DisposableEffect(entrada) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) acao()
        }
        entrada.lifecycle.addObserver(observador)
        onDispose { entrada.lifecycle.removeObserver(observador) }
    }
}
