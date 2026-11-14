package com.example.myapplication.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.*

@Composable
fun campoColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = CardBg,
    unfocusedContainerColor = CardBg,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = PurplePrimary,
    unfocusedBorderColor = SurfaceLight,
    focusedLabelColor = PurpleLight,
    unfocusedLabelColor = TextMuted
)

@Composable
fun StarsInput(valor: Int, onChange: (Int) -> Unit, enabled: Boolean = true) {
    Row {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= valor) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "Nota $i",
                tint = if (i <= valor) GreenAccent else TextFaint,
                modifier = Modifier
                    .size(36.dp)
                    .clickable(enabled = enabled) { onChange(i) }
            )
        }
    }
}
