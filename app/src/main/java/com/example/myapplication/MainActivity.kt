package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigation.Amigos
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.navigation.Buscar
import com.example.myapplication.navigation.Perfil
import com.example.myapplication.navigation.Reviews
import com.example.myapplication.ui.theme.BgDark
import com.example.myapplication.ui.theme.CardBg
import com.example.myapplication.ui.theme.PurplePrimary
import com.example.myapplication.ui.theme.SoundAppTheme
import com.example.myapplication.ui.theme.TextMuted

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoundAppTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = navBackStackEntry?.destination?.route

    val mostrarMenu = rotaAtual != null &&
            !rotaAtual.contains("Login") &&
            !rotaAtual.contains("Cadastro")

    Surface(color = BgDark, modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = BgDark,
            bottomBar = {
                if (mostrarMenu) {
                    BottomBar(navController = navController, rotaAtual = rotaAtual)
                }
            }
        ) { innerPadding ->
            AppNavigation(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController, rotaAtual: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg)
            .navigationBarsPadding()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BottomBarItem(
            label = "Buscar",
            outlinedIcon = Icons.Outlined.Search,
            filledIcon = Icons.Filled.Search,
            selected = rotaAtual?.contains("Buscar") == true
        ) { navController.navigate(Buscar) { launchSingleTop = true } }

        BottomBarItem(
            label = "Reviews",
            outlinedIcon = Icons.Outlined.Star,
            filledIcon = Icons.Filled.Star,
            selected = rotaAtual?.contains("Reviews") == true
        ) { navController.navigate(Reviews) { launchSingleTop = true } }

        BottomBarItem(
            label = "Amigos",
            outlinedIcon = Icons.Outlined.Groups,
            filledIcon = Icons.Filled.Groups,
            selected = rotaAtual?.contains("Amigos") == true
        ) { navController.navigate(Amigos) { launchSingleTop = true } }

        BottomBarItem(
            label = "Perfil",
            outlinedIcon = Icons.Outlined.Person,
            filledIcon = Icons.Filled.Person,
            selected = rotaAtual?.contains("Perfil") == true
        ) { navController.navigate(Perfil) { launchSingleTop = true } }
    }
}

@Composable
private fun BottomBarItem(
    label: String,
    outlinedIcon: ImageVector,
    filledIcon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = if (selected) filledIcon else outlinedIcon,
            contentDescription = label,
            tint = if (selected) PurplePrimary else TextMuted
        )
        Text(
            text = label,
            color = if (selected) PurplePrimary else TextMuted,
            fontSize = 11.sp
        )
    }
}
