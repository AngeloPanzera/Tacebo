package com.example.myapplication.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun Cover(g1: Color, g2: Color, size: Int = 48, radius: Int = 10) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(radius.dp))
            .background(Brush.linearGradient(listOf(g1, g2))),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.25f),
            modifier = Modifier.size((size / 2.2).dp)
        )
    }
}

// Pares de cores usados para "sortear" a capa de uma música/álbum a partir do título
private val paresDeCores = listOf(
    GradientPurpleDark1 to GradientPurpleDark2,
    GradientGreenDark1 to GradientGreenDark2,
    GradientCyan1 to GradientCyan2,
    GradientViolet1 to GradientViolet2,
    GradientPink1 to GradientPink2,
    GradientBrown1 to GradientBrown2
)

// Capa com cor escolhida pelo texto: o mesmo título sempre gera a mesma capa
@Composable
fun CoverAuto(texto: String, size: Int = 48, radius: Int = 10) {
    val (g1, g2) = paresDeCores[texto.hashCode().mod(paresDeCores.size)]
    Cover(g1 = g1, g2 = g2, size = size, radius = radius)
}

// Avatar redondo: mostra a foto de perfil (se existir) ou as iniciais do nome
@Composable
fun Avi(name: String, size: Int = 40, online: Boolean = false, foto: String? = null) {
    val initials = name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("")

    // Lê o arquivo da foto em segundo plano, para não travar a tela
    val imagem by produceState<ImageBitmap?>(null, foto) {
        value = if (foto == null) {
            null
        } else {
            withContext(Dispatchers.IO) { BitmapFactory.decodeFile(foto)?.asImageBitmap() }
        }
    }

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(PurplePrimary.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = imagem
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Foto de $name",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = initials.uppercase(),
                color = TextPrimary,
                fontSize = (size / 2.5).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun Stars(value: Int, size: Int = 14) {
    Row {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= value) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null,
                tint = if (i <= value) Color(0xFF22C55E) else Color(0xFF3D3D55),
                modifier = Modifier.size(size.dp)
            )
        }
    }
}
